package org.study.demo.design.pattern.proxy;

/**
 * 吃水果的客户端
 *
 * 场景：准备吃个苹果和橙子
 *
 */
public class Client {

    public static void main(String[] args) {
        IFruit fruit = new FruitProxy(new Apple());
        fruit.eat();

        System.out.println("");

        IFruit fruit2 = new FruitProxy(new Orange());
        fruit2.eat();
    }
}
