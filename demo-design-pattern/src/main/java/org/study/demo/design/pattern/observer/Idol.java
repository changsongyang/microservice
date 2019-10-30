package org.study.demo.design.pattern.observer;

/**
 * 抽象的主题：这里是明星
 */
public interface Idol {
    public void addFan(Fan fan);

    public void delFan(Fan fan);

    public void notify(String message);
}
