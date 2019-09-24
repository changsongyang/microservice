package org.study.demo.sentinel.service;

import com.alibaba.csp.sentinel.slots.block.BlockException;

public class BlockHandler {

    public static String qpsBlock(int index, String desc, BlockException e){
        System.out.println("==>qpsBlock index="+index+",desc = " + desc + "Exception = "+e.getMessage());
        return "fail";
    }
}
