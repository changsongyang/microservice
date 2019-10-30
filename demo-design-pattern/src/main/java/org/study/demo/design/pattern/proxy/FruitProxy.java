package org.study.demo.design.pattern.proxy;

/**
 * 吃水果代理类
 */
public class FruitProxy implements IFruit {

    IFruit realSubject;

    public FruitProxy(IFruit fruit){
        realSubject = fruit;
    }

    @Override
    public void eat() {
        before();
        realSubject.eat();
        after();
    }

    private void before(){
        System.out.println("---清洗水果---");
    }

    private void after(){
        System.out.println("---清理果皮---");
    }
}
