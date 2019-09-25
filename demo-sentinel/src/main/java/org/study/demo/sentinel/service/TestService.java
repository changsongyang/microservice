package org.study.demo.sentinel.service;

import com.alibaba.csp.sentinel.slots.block.BlockException;

public interface TestService {

    public boolean qps(int index) throws BlockException;

    public boolean degrade(int index) throws BlockException;
}
