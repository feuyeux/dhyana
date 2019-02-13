package org.feuyeux.dhyana.transport;

import java.util.function.Function;

import org.feuyeux.dhyana.config.DhyanaConfig;
import org.feuyeux.dhyana.domain.DhyanaCandidate;
import org.feuyeux.dhyana.domain.DhyanaNode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author feuyeux@gmail.com
 * @date 2019/01/10
 */
@Service
@Slf4j
public class DhyanaTransport {
    @Autowired
    private DhyanaConfig dhyanaConfig;

    public void init(Function<String, Boolean> f, Function<String, String> f2) {
        String nodeIp = NetworkUtils.getNodeIp();
        int port = dhyanaConfig.getPort();
        log.debug("init dhyana server[{}:{}]", nodeIp, port);
        new DhyanaNettyServer(nodeIp, port, f, f2);
    }

    public String ask(DhyanaCandidate c, String masterCandidate) {
        return DhyanaNettyClient.execute(c.getIp(), c.getPort(), masterCandidate);
    }

    public String health(DhyanaNode n) {
        return DhyanaNettyClient.execute(n.getIp(), n.getPort(), "H");
    }
}