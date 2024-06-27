package com.marco.gateway.slack;

import com.slack.api.Slack;

public interface ISlackService {

    default Slack getSlackInstance() {
        return Slack.getInstance();
    }
}
