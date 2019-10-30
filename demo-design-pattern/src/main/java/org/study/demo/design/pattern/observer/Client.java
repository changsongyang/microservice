package org.study.demo.design.pattern.observer;

/**
 * 客户端
 *
 * 场景：明星周杰伦有很多粉丝关注，周杰伦发布一个动态，他的所有粉丝都会收到这个动态消息
 */
public class Client {

    public static void main(String[] args) {
        //第一步：有一个明星对象
        Idol idol = new JayIdol();

        //第二步：有很多粉丝
        Fan fanA = new JayFan("张三");
        Fan fanB = new JayFan("李四");
        Fan fanC = new JayFan("王五");

        //第三步：让粉丝去关注明星(亦或者反过来明星主动拉粉丝)
        idol.addFan(fanA);
        idol.addFan(fanB);
        idol.addFan(fanC);

        //第四步：明星发布动态，粉丝收到动态
        idol.notify("11月1号周杰伦要来广州开演唱会啦");
    }
}
