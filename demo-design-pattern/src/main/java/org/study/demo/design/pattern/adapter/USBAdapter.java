package org.study.demo.design.pattern.adapter;

/**
 * 适配器
 */
public class USBAdapter extends USBTypeC implements USBLightning {

    public void lightning_charge_interface(){
        super.type_c_charge_interface();
    }
}
