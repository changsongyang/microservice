package org.study.demo.design.pattern.proxy;

/**
 * 吃苹果实现类
 */
public class Apple implements IFruit {

    @Override
    public void eat() {
        System.out.println("---吃苹果---");
    }
}
