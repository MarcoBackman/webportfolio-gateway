package com.marco.gateway.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Getter
@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(SlackConfig.class);

    private final String server;
    private final Integer port;

    public RedisConfig(RedisProperties redisProperties) {
        this.server = redisProperties.getServer();
        this.port = redisProperties.getPort();
        logger.info("Redis config init. Loaded properties - server: {}, port: {}", server, port);
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(server, port));
    }

    @Bean
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    @Bean
    public CommandLineRunner checkRedisConnection(RedisTemplate<String, String> redisTemplate) {
        return args -> {
            try {
                redisTemplate.opsForValue().set("testKey", "testValue");
                String testValue = redisTemplate.opsForValue().get("testKey");
                logger.info("Successfully connected to Redis. Set key 'testKey' with value: {}", testValue);
            } catch (Exception e) {
                logger.info("Failed to connect to Redis: {}", e.getMessage());
            }
        };
    }
}
