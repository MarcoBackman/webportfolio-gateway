package com.marco.gateway.controller;

import com.marco.gateway.domain.PortfolioFeedback;
import com.marco.gateway.slack.FeedbackSlackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeedbackControllerTest {
    @InjectMocks
    private FeedbackController feedbackController;

    @Mock
    private FeedbackSlackService feedbackSlackService;

    @Test
    void when_feedbackSuccessfullySent_then_statusOk() {
        PortfolioFeedback portfolioFeedback = new PortfolioFeedback("Name", "Email", "Message");
        when(feedbackSlackService.sendSlackMessageToServer(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        ResponseEntity<String> response = feedbackController.sendUserFeedbackToSlack(portfolioFeedback);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Message sent", response.getBody());
    }

    @Test
    void when_feedbackNotSent_then_statusExpectationFailed() {
        PortfolioFeedback portfolioFeedback = new PortfolioFeedback("Name", "Email", "Message");
        when(feedbackSlackService.sendSlackMessageToServer(anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        ResponseEntity<String> response = feedbackController.sendUserFeedbackToSlack(portfolioFeedback);

        assertEquals(HttpStatus.EXPECTATION_FAILED, response.getStatusCode());
        assertEquals("Message dropped", response.getBody());
    }
}
