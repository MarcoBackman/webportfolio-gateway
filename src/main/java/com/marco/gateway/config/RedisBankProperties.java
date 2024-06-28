package com.marco.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "redis.bank")
public class RedisBankProperties {
    private Integer maxRequest;
    private Integer resetTime;
}
