package org.study.demo.design.pattern.adapter;

/**
 * 接口调用方(客户端)
 *
 * 场景：有一部 iphone手机和 type-c接口的数据线，现在想要给iphone手机充电
 */
public class Client {

    public static void main(String[] args) {
        IPhone iPhone = new IPhone();

        USBAdapter usbAdapter = new USBAdapter();
        iPhone.charge(usbAdapter);
    }
}
