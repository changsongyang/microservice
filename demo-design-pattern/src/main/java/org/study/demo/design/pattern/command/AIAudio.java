package org.study.demo.design.pattern.command;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 智能音响 - 负责控制家里所有的电器
 */
public class AIAudio {
    private Queue<Command> commandQueue = new LinkedBlockingDeque();
    private Thread worker = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true){
                Command command = commandQueue.poll();
                if(command != null){
                    command.execute();
                    continue;
                }

                try{
                    Thread.sleep(1);
                }catch(InterruptedException e){
                    break;
                }
            }
        }
    });

    public void addCommand(Command command){
        commandQueue.add(command);
    }

    public void removeCommand(Command command){
        commandQueue.remove(command);
    }


    public AIAudio(){
        worker.start();
    }
}
