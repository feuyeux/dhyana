package org.feuyeux.dhyana.transport;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/01
 */
public class DhyanaNettyClient {
    public static String execute(String ip, int port, String payload) {
        BlockingQueue<String> channelCache = new ArrayBlockingQueue<>(1);
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                            new StringDecoder(CharsetUtil.UTF_8),
                            new StringEncoder(CharsetUtil.UTF_8),
                            new DhyanaClientHandler(payload, channelCache));
                    }
                });
            ChannelFuture f = b.connect(ip, port).sync();
            f.channel().closeFuture().sync();
            return channelCache.take();
        } catch (InterruptedException e) {
            return null;
        } finally {
            group.shutdownGracefully();
        }
    }
}
