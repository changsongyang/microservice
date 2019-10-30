package org.study.demo.design.pattern.command;

/**
 * 电视关闭命令
 */
public class TVOffCommand implements Command {
    private TV tv;

    public TVOffCommand(TV tv) {
        this.tv = tv;
    }

    public void execute() {
        tv.off();
    }
}
