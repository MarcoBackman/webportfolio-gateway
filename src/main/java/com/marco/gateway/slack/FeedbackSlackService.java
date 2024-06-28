package com.marco.gateway.slack;

import com.marco.gateway.config.SlackConfig;
import com.marco.gateway.util.DateTimeUtil;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedbackSlackService implements ISlackService {

    SlackConfig slackConfig;

    private static final Logger logger = LoggerFactory.getLogger(FeedbackSlackService.class);

    public FeedbackSlackService(SlackConfig slackConfig) {
        this.slackConfig = slackConfig;
    }

    protected List<LayoutBlock> formSlackMessage(String title,
                                       String subTitle,
                                       String content) {
        List<LayoutBlock> blocks = new ArrayList<>();

        //Title
        if (title != null && !title.isBlank()) {
            blocks.add(SectionBlock.builder()
                    .text(MarkdownTextObject.builder().text("* :email:" + title + "* : ").build())
                    .build());
        }
        //Subtitle
        if (subTitle != null && !subTitle.isBlank()) {
            blocks.add(SectionBlock.builder()
                    .text(MarkdownTextObject.builder().text(subTitle).build())
                    .build());
            blocks.add(DividerBlock.builder().build());
        }

        //content
        blocks.add(SectionBlock.builder()
                .text(MarkdownTextObject.builder().text(content).build())
                .build());
        return blocks;
    }

    private ChatPostMessageRequest formRequest(String channel,
                             @Nullable String title,
                             @Nullable String subtitle,
                             @NonNull String content) {

        List<LayoutBlock> blocks = formSlackMessage(title, subtitle, content);
        String text = title + " : " + subtitle + " : " + content;

        return ChatPostMessageRequest.builder()
                .channel(channel)
                .blocks(blocks)
                .text(text)
                .build();
    }

    //Send Slack message
    public boolean sendSlackMessageToServer(String channel,
                                        @Nullable String title,
                                        @Nullable String subtitle,
                                        String content) {
        if (content == null || content.isBlank()) {
            logger.warn("Content is empty. Slack message requires some content.");
            return false;
        }

        Slack slack = getSlackInstance();

        ChatPostMessageRequest request = formRequest(channel, title, subtitle, content);

        try {
            ChatPostMessageResponse response = slack.methods(slackConfig.getBotToken()).chatPostMessage(request);
            if(response.isOk()) {
                logger.info("Message successfully sent to channel {} at timestamp {}", channel, DateTimeUtil.getCurrentDateTimeFormatted());
                return true;
            } else {
                logger.warn("Failed to send message to channel {} due to error: {}", channel, response.getError());
                return false;
            }
        } catch (IOException | SlackApiException e) {
            logger.error("Error sending message to channel {}", channel, e);
            return false;
        }
    }

    //Todo: Send Slack message with file

    //Todo: Send Slack message to designated user
}
