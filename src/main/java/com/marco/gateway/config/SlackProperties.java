package com.marco.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "slack.webportfolio")
public class SlackProperties {
    private String botToken;
    private String channelId;
}
