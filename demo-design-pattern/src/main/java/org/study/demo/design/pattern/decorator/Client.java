package org.study.demo.design.pattern.decorator;

/**
 * 客户端
 *
 * 场景：你现在准备打扮一下去参加一个聚会，刚出门觉得有点冷，又回去加了件毛衣，刚出门经过一扇窗，觉得不够帅，又回去加了顶帽子
 */
public class Client {

    public static void main(String[] args) {
        PersonComponent personComponent = new PersonComponent();//经过初步打扮的你

        Decorator sweaterDecorator = new SweaterDecorator(personComponent);//加了毛衣之后的你

        Decorator hatDecorator = new HatDecorator(sweaterDecorator);//加了帽子之后的你
        hatDecorator.decorate();

        //可以非常灵活的 增加、卸载 链节点
    }
}
