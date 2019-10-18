package org.study.demo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.study.common.util.utils.DateUtil;

import java.util.Date;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 客户端连接会触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel active......" );
    }

    /**
     * 对每一个传入的消息都要调用；
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Channel read......" );
        try {
            ByteBuf in = (ByteBuf) msg;
            byte[] req = new byte[in.readableBytes()];
            in.readBytes(req);
            System.out.println("server received: " + new String(req, CharsetUtil.UTF_8));

            String currTime = DateUtil.formatDateTime(new Date());
            ByteBuf resp = Unpooled.copiedBuffer(currTime.getBytes());
            ctx.writeAndFlush(resp);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 在读取操作期间，有异常抛出时会调用。
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
