package org.study.demo.design.pattern.observer;

/**
 * 具体的观察者：这里是周杰伦的粉丝
 */
public class JayFan implements Fan {
    private String fanName;

    public JayFan(String fanName){
        this.fanName = fanName;
    }

    public void update(String message){
        System.out.println(fanName + " 知道了 '" + message + "' 这个消息 ");
    }
}
