package com.marco.gateway.slack;

import com.marco.gateway.config.SlackConfig;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedbackSlackServiceTest {
    FeedbackSlackService feedbackSlackService;

    SlackConfig slackConfig;

    String testChanel = "testChanel";
    String title = "test title";
    String subTitle = "test subTitle";
    String content = "test content";

    public FeedbackSlackServiceTest() {
        slackConfig = mock(SlackConfig.class);
        feedbackSlackService = spy(new FeedbackSlackService(slackConfig));
    }

    @Test
    public void formSlackMessageTest() {

        List<LayoutBlock> actualMessage = feedbackSlackService.formSlackMessage(title, subTitle, content);
        assertThat(actualMessage.size()).isEqualTo(4);
        assertInstanceOf(SectionBlock.class, actualMessage.get(0));
        assertThat(((SectionBlock)actualMessage.get(0)).getText().getText())
                .isEqualTo("* :email:" + title + "* : ");

        assertInstanceOf(SectionBlock.class, actualMessage.get(1));
        assertThat(((SectionBlock)actualMessage.get(1)).getText().getText())
                .isEqualTo(subTitle);

        assertInstanceOf(DividerBlock.class, actualMessage.get(2));

        assertInstanceOf(SectionBlock.class, actualMessage.get(3));
        assertThat(((SectionBlock)actualMessage.get(3)).getText().getText())
                .isEqualTo(content);
    }

    @Test
    public void sendSlackMessageToServer_noNotificationSendOnNullContentTest() {
        feedbackSlackService.sendSlackMessageToServer(testChanel, title, subTitle, null);
        verify(feedbackSlackService, times(0)).formSlackMessage(any(), any(), any());
    }

    @Test
    public void sendSlackMessageToServer_noNotificationSendOnEmptyContentTest() {
        feedbackSlackService.sendSlackMessageToServer(testChanel, title, subTitle, "");
        verify(feedbackSlackService, times(0)).formSlackMessage(any(), any(), any());
    }

    @Test
    public void sendSlackMessageToServer_NotificationSentOnEmptyContentTest() throws SlackApiException, IOException {
        Slack mockSlack = mock(Slack.class);
        String testBotToken = "testBotToken";
        MethodsClient mockClient = mock(MethodsClient.class);
        ChatPostMessageResponse mockResponse = new ChatPostMessageResponse();
        mockResponse.setOk(true);

        doReturn(mockSlack).when(feedbackSlackService).getSlackInstance();
        doReturn(testBotToken).when(slackConfig).getBotToken();
        doReturn(mockClient).when(mockSlack).methods(testBotToken);
        doReturn(mockResponse).when(mockClient).chatPostMessage(any(ChatPostMessageRequest.class));

        feedbackSlackService.sendSlackMessageToServer(testChanel, title, subTitle, content);
        verify(feedbackSlackService, times(1)).formSlackMessage(any(), any(), any());
    }
}
