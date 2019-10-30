package org.study.demo.design.pattern.decorator;

/**
 * 人组件
 *
 * 这个组件已经是完整的实现了,是可以独立运行的，但是如果我们这是要增加他的功能呢？来看抽象装饰者角色：Decorator
 */
public class PersonComponent implements Component {

    @Override
    public void decorate() {
        System.out.println("我穿了一件T恤，一条牛仔，一双奥康皮鞋");
    }
}
