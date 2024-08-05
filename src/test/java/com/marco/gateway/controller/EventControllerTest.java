package com.marco.gateway.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventControllerTest {
    //Check if bank core service is available
    //Check if kafka is online
    @InjectMocks
    private FeedbackController feedbackController;

    @Test
    public void rerouteEventToAuthenticationServerTest() {
        //Todo: implement test case
    }

    @Test
    public void rerouteEventToBankCoreServerTest() {
        //Todo: implement test case
    }
}
