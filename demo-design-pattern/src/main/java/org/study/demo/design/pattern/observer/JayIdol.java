package org.study.demo.design.pattern.observer;

import java.util.HashSet;
import java.util.Set;

/**
 * 具体的明星：这里是周杰伦
 */
public class JayIdol implements Idol {
    Set<Fan> fanSet = new HashSet<>();

    @Override
    public void addFan(Fan fan) {
        fanSet.add(fan);
    }

    @Override
    public void delFan(Fan fan) {
        fanSet.remove(fan);
    }

    @Override
    public void notify(String message) {
        for(Fan fan : fanSet){
            fan.update(message);
        }
    }
}
