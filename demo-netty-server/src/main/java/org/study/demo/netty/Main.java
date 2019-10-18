package org.study.demo.netty;

public class Main {

    public static void main(String[] args){
        NettyServer nettyServer = new NettyServer();
        nettyServer.start("127.0.0.1", 10010);
    }
}
