package org.study.demo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * 在到服务器的连接已经建立之后将被调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client Active .....");

        for(int i=1; i<=100; i++){
            ByteBuf req = Unpooled.copiedBuffer("Hello_World_" + i + " ", CharsetUtil.UTF_8);
            ctx.writeAndFlush(req);//发送消息
        }
    }

    /**
     * 当从服务器接收到一个消息时被调用
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Client read......" );
        ByteBuf resp = (ByteBuf) msg;
        byte[] req = new byte[resp.readableBytes()];
        resp.readBytes(req);
        System.out.println("Client received: " + new String(req, CharsetUtil.UTF_8));
    }

    /**
     * 在处理过程中引发异常时被调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
