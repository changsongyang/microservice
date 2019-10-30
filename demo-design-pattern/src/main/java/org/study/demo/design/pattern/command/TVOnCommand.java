package org.study.demo.design.pattern.command;

/**
 * 电视开启命令
 */
public class TVOnCommand implements Command {
    private TV tv;

    public TVOnCommand(TV tv) {
        this.tv = tv;
    }

    public void execute() {
        tv.on();
    }
}
