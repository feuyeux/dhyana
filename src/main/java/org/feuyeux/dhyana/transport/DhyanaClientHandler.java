package org.feuyeux.dhyana.transport;

import java.util.concurrent.BlockingQueue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/01
 */
@Slf4j
public class DhyanaClientHandler extends SimpleChannelInboundHandler {
    private final String payload;
    private final BlockingQueue<String> channelCache;

    public DhyanaClientHandler(String payload, BlockingQueue<String> channelCache) {
        this.payload = payload;
        this.channelCache = channelCache;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(payload);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        String payload = message.toString();
        channelCache.put(payload);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) {

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}

