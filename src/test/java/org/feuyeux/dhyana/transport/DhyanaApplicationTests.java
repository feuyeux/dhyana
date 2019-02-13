package org.feuyeux.dhyana.transport;

import org.feuyeux.dhyana.config.DhyanaConfig;
import org.feuyeux.dhyana.domain.DhyanaCandidate;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DhyanaApplicationTests {
    @Autowired
    private DhyanaTransport dhyanaTransport;
    @Autowired
    private ConfigTransport configTransport;

    @Autowired
    private DhyanaConfig dhyanaConfig;

    @Autowired
    private RedisUtils redis;

    @Test
    public void testTransport() {
        String nodeIp = NetworkUtils.getNodeIp();
        DhyanaCandidate candidate = DhyanaCandidate.builder()
            .id(dhyanaConfig.getNodeName()).ip(nodeIp).port(dhyanaConfig.getPort()).build();
        String result = dhyanaTransport.ask(candidate, nodeIp);
        log.info("result={}", result);
    }
}

