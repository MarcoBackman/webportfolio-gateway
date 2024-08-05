package com.marco.gateway.security;

import com.marco.gateway.aop.RateLimiterMockTest;
import com.marco.gateway.config.SlackConfig;
import com.marco.gateway.domain.PortfolioFeedback;
import com.marco.gateway.slack.FeedbackSlackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSecurityConfigTest extends RateLimiterMockTest {
    @MockBean
    private FeedbackSlackService feedbackSlackService;
    @MockBean
    private SlackConfig slackConfig;

    @Autowired
    private WebTestClient webTestClient;

    @DisplayName("Smoke test if the context loads successfully")
    @Test
    @WithMockUser
    public void contextLoads() {
        assertNotNull(webTestClient);
    }

    @DisplayName("Test disabled CSRF on POST method.")
    @Test
    public void springSecurityFilterChainTest() {
        doReturn(true).when(feedbackSlackService).sendSlackMessageToServer(any(), anyString(), anyString(), anyString());

        PortfolioFeedback testBodyMsg = new PortfolioFeedback("Test name", "Test email", "Test content");
        webTestClient
                .post()
                .uri("/message-service/sendFeedbackMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(testBodyMsg))
                .exchange()
                .expectStatus().isOk();

    }
}