package org.study.demo.design.pattern.command;

/**
 * 电灯关闭命令
 */
public class LightOffCommand implements Command {
    private Light light;

    public LightOffCommand(Light light) {
        this.light = light;
    }

    public void execute() {
        light.off();
    }
}
