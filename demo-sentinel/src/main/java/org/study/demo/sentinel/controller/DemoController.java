package org.study.demo.sentinel.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.study.demo.sentinel.service.TestService;
import org.study.starter.component.CircuitBreaker;
import org.study.starter.component.QpsCounter;
import org.study.starter.component.QpsLimiter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("demo")
public class DemoController {
    private static Map<String, Integer> qpsLimitMap = new HashMap<>();
    private static Map<String, CircuitBreaker.Config> breakerMap = new HashMap<>();
    static {
        qpsLimitMap.put("test_qpsLimiter", 5);

        breakerMap.put("test_circuitBreaker", CircuitBreaker.buildConfig(0, 200, 10));
    }

    @Autowired
    private TestService testService;
    private QpsCounter qpsCounter = new QpsCounter("test_qpsCounter");
    private QpsLimiter qpsLimiter = new QpsLimiter(qpsLimitMap);
    private CircuitBreaker breaker = new CircuitBreaker(breakerMap);

    @RequestMapping(value = "/qps")
    public boolean qps(int threadCount, String desc){
        if(threadCount <= 0){
            threadCount = 1;
        }else if(threadCount > 10){
            threadCount = 10;
        }
        // Assume we config: resource is `TestResource`, initial QPS threshold is 5.
        FlowQpsRunner runner = new FlowQpsRunner(null, threadCount, 100);
        runner.simulateTraffic();
        runner.tick();
        return true;
    }

    @RequestMapping(value = "/degrade")
    public boolean degrade(int threadCount, String desc){
        if(threadCount <= 0){
            threadCount = 1;
        }else if(threadCount > 10){
            threadCount = 10;
        }
        // Assume we config: resource is `TestResource`, initial QPS threshold is 5.
        FlowDegradeRunner runner = new FlowDegradeRunner(null, threadCount, 100);
        runner.simulateTraffic();
        runner.tick();
        return true;
    }

    @RequestMapping(value = "/qpsCount")
    public boolean qpsCount(int threadCount, int second){
        for (int i = 1; i <= threadCount; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    long now = System.currentTimeMillis();
                    long end = System.currentTimeMillis() + second * 1000;
                    boolean isSuccess = false;
                    while(now <= end){
//                        try{
//                            Thread.sleep(2);
//                        }catch (InterruptedException e){
//                        }

                        isSuccess = false;
                        double mod = now % 4;
                        if(mod <=1){
                            qpsCounter.incPass();
                            if(mod == 1){
                                isSuccess = true;
                            }
                        }else if(mod == 2){
                            qpsCounter.incBlock();
                        }else if(mod == 3){
                            qpsCounter.incException();
                        }

                        if(isSuccess){
                            qpsCounter.incRtAndSuccess(System.currentTimeMillis() - now);
                        }
                        System.out.println(TimeUtil.currentTimeMillis() + " " + qpsCounter.toString());
                        now = System.currentTimeMillis();
                    }
                }
            });
            t.setName("simulate-traffic-Task");
            t.start();
        }

        return true;
    }

    @RequestMapping(value = "/qpsLimit")
    public boolean qpsLimit(int threadCount, int second, String desc) {
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 1; i <= threadCount; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    long now = System.currentTimeMillis();
                    long end = System.currentTimeMillis() + second * 1000;

                    while(now <= end){
                        String result = qpsLimiter.execute("test_qpsLimiter", (param)->{
                            int count = counter.incrementAndGet();
                            System.out.println(TimeUtil.currentTimeMillis() + " count=" + count);
                            return "ok_" + count;
                            }, desc);

                        System.out.println(result);
                        now = System.currentTimeMillis();
                    }
                }
            });
            t.setName("simulate-traffic-Task");
            t.start();
        }

        return true;
    }

    @RequestMapping(value = "/circuitBreaker")
    public boolean circuitBreaker(int threadCount, int second, String desc) {
        QpsCounter qpsCounter = new QpsCounter("circuitBreakerCounter");

        for (int i = 1; i <= threadCount; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    long now = System.currentTimeMillis();
                    long end = System.currentTimeMillis() + second * 1000;

                    while(now <= end){
                        breaker.execute("test_circuitBreaker", (param)->{
                            qpsCounter.incPass();
                            long start = System.currentTimeMillis();
                            try {
                                TimeUnit.MILLISECONDS.sleep(3 * 100);//模拟业务处理时间
                            } catch (InterruptedException e) {
                                // ignore
                            }

                            qpsCounter.incRtAndSuccess(System.currentTimeMillis()-start, 1);
                            return null;
                        }, desc, (e) -> {
                            qpsCounter.incBlock();
                        });

                        now = System.currentTimeMillis();

                        System.out.println(qpsCounter.toString());
                    }
                }
            });
            t.setName("simulate-traffic-Task");
            t.start();
        }

        return true;
    }

    class FlowQpsRunner {
        private final String resourceName;
        private final int threadCount;
        private int seconds;

        public FlowQpsRunner(String resourceName, int threadCount, int seconds) {
            this.resourceName = resourceName;
            this.threadCount = threadCount;
            this.seconds = seconds;
        }

        private final AtomicInteger pass = new AtomicInteger();
        private final AtomicInteger block = new AtomicInteger();
        private final AtomicInteger total = new AtomicInteger();

        private volatile boolean stop = false;

        public void simulateTraffic() {
            for (int i = 1; i <= threadCount; i++) {
                Thread t = new Thread(new RunTask(i));
                t.setName("simulate-traffic-Task");
                t.start();
            }
        }

        public void tick() {
            Thread timer = new Thread(new TimerTask());
            timer.setName("sentinel-timer-task");
            timer.start();
        }

        final class RunTask implements Runnable {
            private int index;

            public RunTask(int index){
                this.index = index;
            }

            @Override
            public void run() {
                while (!stop) {
                    try {
                        testService.qps(index);

                        // token acquired, means pass
                        pass.addAndGet(1);
                    } catch (BlockException e1){
                        block.incrementAndGet();
                    } catch (Exception e2) {
                        // biz exception
                    } finally {
                        total.incrementAndGet();
                    }

                    Random random2 = new Random();
                    try {
                        TimeUnit.MILLISECONDS.sleep(random2.nextInt(50));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }

        final class TimerTask implements Runnable {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                System.out.println("begin to statistic!!!");

                long oldTotal = 0;
                long oldPass = 0;
                long oldBlock = 0;
                while (!stop) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                    long globalTotal = total.get();
                    long oneSecondTotal = globalTotal - oldTotal;
                    oldTotal = globalTotal;

                    long globalPass = pass.get();
                    long oneSecondPass = globalPass - oldPass;
                    oldPass = globalPass;

                    long globalBlock = block.get();
                    long oneSecondBlock = globalBlock - oldBlock;
                    oldBlock = globalBlock;

                    System.out.println(seconds + " send qps is: " + oneSecondTotal);
                    System.out.println(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                            + ", pass:" + oneSecondPass
                            + ", block:" + oneSecondBlock);

                    if (seconds-- <= 0) {
                        stop = true;
                    }
                }

                long cost = System.currentTimeMillis() - start;
                System.out.println("time cost: " + cost + " ms");
                System.out.println("total:" + total.get() + ", pass:" + pass.get()
                        + ", block:" + block.get());
//                System.exit(0);
            }
        }
    }

    class FlowDegradeRunner {
        private final String resourceName;
        private final int threadCount;
        private int seconds;

        public FlowDegradeRunner(String resourceName, int threadCount, int seconds) {
            this.resourceName = resourceName;
            this.threadCount = threadCount;
            this.seconds = seconds;
        }

        private final AtomicInteger pass = new AtomicInteger();
        private final AtomicInteger block = new AtomicInteger();
        private final AtomicInteger total = new AtomicInteger();

        private volatile boolean stop = false;

        public void simulateTraffic() {
            for (int i = 1; i <= threadCount; i++) {
                Thread t = new Thread(new RunTask(i));
                t.setName("simulate-traffic-Task");
                t.start();
            }
        }

        public void tick() {
            Thread timer = new Thread(new TimerTask());
            timer.setName("sentinel-timer-task");
            timer.start();
        }

        final class RunTask implements Runnable {
            private int index;

            public RunTask(int index){
                this.index = index;
            }

            @Override
            public void run() {
                int index = 0;
                while (!stop) {
                    try {
                        testService.degrade(index ++);

                        // token acquired, means pass
                        pass.addAndGet(1);
                    } catch (BlockException e1){
                        block.incrementAndGet();
                    } catch (Exception e2) {
                        // biz exception
                    } finally {
                        total.incrementAndGet();
                    }

                    Random random2 = new Random();
                    try {
                        TimeUnit.MILLISECONDS.sleep(random2.nextInt(50));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }

        final class TimerTask implements Runnable {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                System.out.println("begin to statistic!!!");

                long oldTotal = 0;
                long oldPass = 0;
                long oldBlock = 0;
                while (!stop) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                    long globalTotal = total.get();
                    long oneSecondTotal = globalTotal - oldTotal;
                    oldTotal = globalTotal;

                    long globalPass = pass.get();
                    long oneSecondPass = globalPass - oldPass;
                    oldPass = globalPass;

                    long globalBlock = block.get();
                    long oneSecondBlock = globalBlock - oldBlock;
                    oldBlock = globalBlock;

                    System.out.println(seconds + " send qps is: " + oneSecondTotal);
                    System.out.println(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                            + ", pass:" + oneSecondPass
                            + ", block:" + oneSecondBlock);

                    if (seconds-- <= 0) {
                        stop = true;
                    }
                }

                long cost = System.currentTimeMillis() - start;
                System.out.println("time cost: " + cost + " ms");
                System.out.println("total:" + total.get() + ", pass:" + pass.get()
                        + ", block:" + block.get());
//                System.exit(0);
            }
        }
    }
}
