package org.feuyeux.dhyana.transport;

import org.feuyeux.dhyana.transport.DhyanaNettyClient;
import org.feuyeux.dhyana.transport.DhyanaNettyServer;

import lombok.extern.slf4j.Slf4j;
import org.feuyeux.dhyana.transport.NetworkUtils;
import org.junit.Test;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/01
 */
@Slf4j
public class NettyTest {
    @Test
    public void test() {
        new DhyanaNettyServer(NetworkUtils.getNodeIp(), 12345, s -> {
            log.info(s);
            return s.equals("TEST");
        }, s -> s);

        DhyanaNettyClient.execute(NetworkUtils.getNodeIp(), 12345, "TEST");

    }
}
