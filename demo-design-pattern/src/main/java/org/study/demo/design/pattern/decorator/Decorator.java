package org.study.demo.design.pattern.decorator;

/**
 * 装饰器，实现Component组件接口
 */
public class Decorator implements Component {

    private Component component;

    public Decorator(Component component) {
        this.component = component;
    }

    @Override
    public void decorate() {
        if (null != component) {
            component.decorate();
        }
    }
}
