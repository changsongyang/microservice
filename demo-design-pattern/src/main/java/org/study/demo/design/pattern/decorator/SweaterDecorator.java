package org.study.demo.design.pattern.decorator;

/**
 * 毛衣装饰器
 */
public class SweaterDecorator extends Decorator {

    public SweaterDecorator(Component component) {
        super(component);
    }

    @Override
    public void decorate() {
        super.decorate();
        System.out.println("加了件毛衣");
    }
}
