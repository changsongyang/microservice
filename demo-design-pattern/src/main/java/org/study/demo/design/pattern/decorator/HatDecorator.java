package org.study.demo.design.pattern.decorator;

/**
 * 帽子装饰器
 */
public class HatDecorator extends Decorator {

    public HatDecorator(Component component) {
        super(component);
    }

    @Override
    public void decorate() {
        super.decorate();
        System.out.println("加了个帽子");
    }
}
