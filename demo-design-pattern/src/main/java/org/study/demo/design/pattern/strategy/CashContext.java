package org.study.demo.design.pattern.strategy;

/**
 * 如：商场现在正在搞活动，有打折的、有满减的、有返利的等等，收银时根据不同活动来决定怎么收银
 * 上下文类
 */
public class CashContext {
    private CashSuper cashSuper;

    public CashContext(CashSuper cashSuper) {
        this.cashSuper = cashSuper;
    }

    public double getResult(double money) {
        return cashSuper.acceptCash(money);
    }
}
