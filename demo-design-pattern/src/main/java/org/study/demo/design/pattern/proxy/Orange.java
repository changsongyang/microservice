package org.study.demo.design.pattern.proxy;

/**
 * 吃橙子实现类
 */
public class Orange implements IFruit {

    @Override
    public void eat() {
        System.out.println("---吃橙子---");
    }
}
