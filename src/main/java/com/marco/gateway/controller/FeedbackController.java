package com.marco.gateway.controller;

import com.marco.gateway.domain.PortfolioFeedback;
import com.marco.gateway.slack.FeedbackSlackService;
import com.marco.gateway.util.DateTimeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message-service")
public class FeedbackController {

    private final FeedbackSlackService feedbackSlackService;

    private final String FEEDBACK_CHANNEL = "web-portfolio-messages";

    public FeedbackController(FeedbackSlackService feedbackSlackService) {
        this.feedbackSlackService = feedbackSlackService;
    }

    @PostMapping("/sendFeedbackMessage")
    public ResponseEntity<String> sendUserFeedbackToSlack(@RequestBody PortfolioFeedback message) {

        String title = "Webportfolio Feedback.";
        String subTitle = DateTimeUtil.getCurrentDateTimeFormatted();
        String content = String.format("User: %s \nEmail: %s\nMessage: %s",
                message.getName(),
                message.getEmail(),
                message.getContent());

        boolean isMsgSent = feedbackSlackService.sendSlackMessageToServer(FEEDBACK_CHANNEL, title, subTitle, content);
        if (isMsgSent) {
            return new ResponseEntity<>("Message sent", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Message dropped", HttpStatus.EXPECTATION_FAILED);
        }
    }
}
