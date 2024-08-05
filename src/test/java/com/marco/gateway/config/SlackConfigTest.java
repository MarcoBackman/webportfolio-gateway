package com.marco.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class SlackConfigTest {

    private final String SLACK_TEST_TOKEN = "test_token";
    private final String SLACK_TEST_CHANNEL_ID = "test_channel";
    private final String SLACK_TEST_CHANNEL_NAME = "slack_channel_name";

    @Autowired
    private SlackConfig slackConfig;

    @Autowired
    private SlackProperties slackProperties;

    @Test
    public void slackConfigValueTest() {
        assertNotNull(slackConfig);
        assertThat(slackConfig.getBotToken()).isEqualTo(SLACK_TEST_TOKEN);
        assertThat(slackConfig.getChannelId()).isEqualTo(SLACK_TEST_CHANNEL_ID);
        assertThat(slackConfig.getChannelName()).isEqualTo(SLACK_TEST_CHANNEL_NAME);
    }

    @Test
    public void slackPropertyTest() {
        assertNotNull(slackProperties);
        assertThat(slackProperties.getBotToken()).isEqualTo(SLACK_TEST_TOKEN);
        assertThat(slackProperties.getChannelId()).isEqualTo(SLACK_TEST_CHANNEL_ID);
        assertThat(slackProperties.getChannelName()).isEqualTo(SLACK_TEST_CHANNEL_NAME);
    }
}