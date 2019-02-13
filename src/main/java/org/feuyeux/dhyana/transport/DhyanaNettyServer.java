package org.feuyeux.dhyana.transport;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/01
 */
@Slf4j
public class DhyanaNettyServer {
    final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final List<ChannelFuture> channels = Lists.newArrayList();

    public DhyanaNettyServer(String hostname, int port, Function<String, Boolean> f, Function<String, String> f2) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                            new StringDecoder(CharsetUtil.UTF_8),
                            new StringEncoder(CharsetUtil.UTF_8),
                            new DhyanaServerHandler(f, f2));
                    }
                });
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            channels.add(b.bind(address).sync());
            log.info("DhyanaServer[{}:{}] launched", address.getHostName(), address.getPort());
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        for (final ChannelFuture c : channels) {
            try {
                c.channel().closeFuture().sync();
            } catch (InterruptedException ignored) { }
        }
    }
}
