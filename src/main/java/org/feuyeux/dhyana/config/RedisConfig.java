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
public class RedisConfig {
    @Value("${nls.redis.host:localhost}")
    private String redisHost;
    @Value("${nls.redis.port:6379}")
    private Integer port;
    @Value("${nls.redis.pw:}")
    private String pwd;
    @Value("${nls.redis.soTimeout:100000}")
    private int soTimeout;
    @Value("${nls.redis.connectionTimeout:100000}")
    private int connectionTimeout;

    @Value("${nls.redis.maxTotal:128}")
    private int maxTotal;
    @Value("${nls.redis.minIdle:16}")
    private int minIdle;
    @Value("${nls.redis.maxIdle:128}")
    private int maxIdle;
    @Value("${nls.redis.maxWaitMillis:3000}")
    private int maxWaitMillis;
}
