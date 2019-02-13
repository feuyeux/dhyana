package org.feuyeux.dhyana.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author feuyeux@gmail.com
 * @date 2019/01/09
 */
@Slf4j
@Data
@Configuration
public class DhyanaConfig {

    @Value("${nls.dhyana.port}")
    private int port;

    @Value("${nls.dhyana.clusterName}")
    private String clusterName;

    @Value("${nls.dhyana.nodeName}")
    private String nodeName;

    /**
     * 最小合法人数量(Quorum)
     * 为避免脑裂(Brain Split)，该值应为(总节点数/2)+1
     */
    @Value("${nls.dhyana.minimumNodes}")
    private int minimumNodes;
}
