package org.study.demo.sentinel.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.study.demo.sentinel.service.TestService;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Autowired
    private TestService testService;

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
                System.exit(0);
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
                System.exit(0);
            }
        }
    }
}
