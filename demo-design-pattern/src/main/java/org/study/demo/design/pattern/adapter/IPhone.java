package org.study.demo.design.pattern.adapter;

/**
 * iphone手机
 */
public class IPhone {

    /**
     * 充电，接口类型是iphone的Lightning接口
     * @param lightning
     */
    public void charge(USBLightning lightning){
        lightning.lightning_charge_interface();
    }
}
