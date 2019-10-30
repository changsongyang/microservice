package org.study.demo.design.pattern.command;

/**
 * 发起命令的客户端
 *
 * 场景：智能家居，由智能音响控制所有家用电器
 */
public class Client {

    public static void main(String[] args) throws InterruptedException {
        //安装好电灯
        Light light = new Light();
        //准备好电视
        TV tv = new TV();
        //准备好智能音响
        AIAudio aiAudio = new AIAudio();


        System.out.println("------打开电灯");
        LightOnCommand lightOnCommand = new LightOnCommand(light);//发出打开电灯的命令
        aiAudio.addCommand(lightOnCommand);
        Thread.sleep(10);//等待响应

        System.out.println("------关闭电灯");
        LightOffCommand lightOffCommand = new LightOffCommand(light);//发出关闭电灯的命令
        aiAudio.addCommand(lightOffCommand);
        Thread.sleep(10);//等待响应

        System.out.println("------打开电视");
        TVOnCommand tvOnCommand = new TVOnCommand(tv);//发出打开电视的命令
        aiAudio.addCommand(tvOnCommand);
        Thread.sleep(10);//等待响应

        System.out.println("------关闭电视");
        TVOffCommand tvOffCommand = new TVOffCommand(tv);//发出关闭电视的命令
        aiAudio.addCommand(tvOffCommand);
        Thread.sleep(10);//等待响应

        System.exit(0);
    }
}
