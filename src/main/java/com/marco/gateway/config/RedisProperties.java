package com.marco.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "redis.webportfolio")
public class RedisProperties {
    private String server;
    private Integer port;
    private Integer maxRequest;
    private Integer resetTime;
}
