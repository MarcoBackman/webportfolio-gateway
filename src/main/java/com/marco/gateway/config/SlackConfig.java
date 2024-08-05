package com.marco.gateway.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class SlackConfig {

    private static final Logger logger = LoggerFactory.getLogger(SlackConfig.class);

    private final String botToken;
    private final String channelId;
    private final String channelName;

    public SlackConfig(SlackProperties slackProperties) {
        this.botToken = slackProperties.getBotToken();
        this.channelId = slackProperties.getChannelId();
        this.channelName = slackProperties.getChannelName();
        logger.info("Slack config init. Loaded token: {}, channelId: {}",
                botToken.substring(0, 5), channelId.substring(0, 5));
    }
}
