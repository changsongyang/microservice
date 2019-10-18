package org.study.demo.netty;

public class Main {

    public static void main(String[] args){
        new NettyClient("127.0.0.1",10010).start();
    }
}
