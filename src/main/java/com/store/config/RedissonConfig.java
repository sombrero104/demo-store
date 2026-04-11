package com.store.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(buildAddress(redisProperties))
                .setDatabase(redisProperties.getDatabase());

        if (StringUtils.hasText(redisProperties.getUsername())) {
            singleServerConfig.setUsername(redisProperties.getUsername());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        if (redisProperties.getTimeout() != null) {
            singleServerConfig.setTimeout((int) redisProperties.getTimeout().toMillis());
        }
        if (redisProperties.getConnectTimeout() != null) {
            singleServerConfig.setConnectTimeout((int) redisProperties.getConnectTimeout().toMillis());
        }

        return Redisson.create(config);
    }

    private String buildAddress(RedisProperties redisProperties) {
        String host = StringUtils.hasText(redisProperties.getHost()) ? redisProperties.getHost() : "127.0.0.1";
        int port = redisProperties.getPort();
        return "redis://" + host + ":" + port;
    }
}
