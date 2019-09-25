package org.study.demo.sentinel.service;

import com.alibaba.csp.sentinel.slots.block.BlockException;

public class BlockHandler {

    public static boolean qpsBlock(int index, BlockException e){
        System.out.println("qpsBlock index=" + index + " Rule=" + e.getRule().getClass().getSimpleName() + " resource=" + e.getRule().getResource());
        return false;
    }

    public static boolean degradeBlock(int index, BlockException e){
        System.out.println("degradeBlock index=" + index + " Rule=" + e.getRule().getClass().getSimpleName() + " resource=" + e.getRule().getResource());
        return false;
    }
}
