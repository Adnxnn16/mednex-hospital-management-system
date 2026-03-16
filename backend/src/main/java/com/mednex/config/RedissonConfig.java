package com.mednex.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    // Spring resolves these from .env / Docker env vars BEFORE
    // Hibernate boots — this is the key difference vs redisson.yml
    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    /**
     * Creates a RedissonClient using a fully-resolved Redis URL.
     * Spring injects REDIS_HOST and REDIS_PORT from environment
     * variables before this bean is constructed, so the address
     * is always a valid URL like "redis://redis:6379".
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        String address = "redis://" + redisHost + ":" + redisPort;

        Config config = new Config();
        config.useSingleServer()
              .setAddress(address)
              .setConnectionMinimumIdleSize(2)
              .setConnectionPoolSize(10)
              .setConnectTimeout(3000)
              .setRetryAttempts(3)
              .setRetryInterval(1500)
              .setKeepAlive(true);

        return Redisson.create(config);
    }

    /**
     * Registers the Spring-managed RedissonClient with Hibernate's
     * service registry. This is what allows RedissonRegionFactory
     * to use our bean instead of trying to read a config file.
     */
    @Bean
    public HibernatePropertiesCustomizer hibernateRedissonCustomizer(
            RedissonClient redissonClient) {
        return hibernateProperties -> {
            hibernateProperties.put("hibernate.cache.redisson.client", redissonClient);
            hibernateProperties.put("hibernate.redisson.client", redissonClient);
            hibernateProperties.put("hibernate.redisson.config", "");
        };
    }
}
