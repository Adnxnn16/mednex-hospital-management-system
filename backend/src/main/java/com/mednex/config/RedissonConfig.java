package com.mednex.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${REDIS_HOST:127.0.0.1}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(
        name = "spring.data.redis.enabled",
        havingValue = "true",
        matchIfMissing = true)
    public RedissonClient redissonClient() {
        String address = "redis://" + redisHost + ":" + redisPort;
        System.out.println("Connecting to Redisson at: " + address);

        Config config = new Config();
        config.useSingleServer()
              .setAddress(address)
              .setConnectionMinimumIdleSize(2)
              .setConnectionPoolSize(10)
              .setConnectTimeout(3000)
              .setRetryAttempts(3)
              .setRetryInterval(1500)
              .setKeepAlive(true);

        try {
            return Redisson.create(config);
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to create RedissonClient: " + e.getMessage());
            return null;
        }
    }

    @Bean
    @ConditionalOnProperty(
        name = "spring.data.redis.enabled",
        havingValue = "true",
        matchIfMissing = true)
    public HibernatePropertiesCustomizer hibernateRedissonCustomizer(
            RedissonClient redissonClient) {
        return hibernateProperties -> {
            if (redissonClient != null) {
                hibernateProperties.put(
                    "hibernate.cache.redisson.client",
                    redissonClient
                );
            } else {
                System.err.println("WARNING: RedissonClient is null, Hibernate cache will not use Redisson.");
            }
        };
    }
}
