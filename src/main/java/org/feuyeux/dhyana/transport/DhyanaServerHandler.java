package org.feuyeux.dhyana.transport;

import java.util.function.Function;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.dhyana.domain.DhyanaConstants;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/01
 */
@Slf4j
public class DhyanaServerHandler extends SimpleChannelInboundHandler {
    private final Function<String, Boolean> f;
    private final Function<String, String> f2;

    public DhyanaServerHandler(Function<String, Boolean> f, Function<String, String> f2) {
        this.f = f;
        this.f2 = f2;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        try {
            String payload = message.toString();
            if (DhyanaConstants.HEALTH.equals(payload)) {
                ctx.write(f2.apply(payload));
            } else {
                boolean result = f.apply(payload);
                ctx.write(result ? DhyanaConstants.MASTER_AGREE : DhyanaConstants.MASTER_DISAGREE);
            }
        } catch (Exception e) {
            log.error("", e);
        }
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
