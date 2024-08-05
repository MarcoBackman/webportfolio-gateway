package com.marco.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {


    public EventController() {
        //Todo: implement dependent services
    }

    @GetMapping(value = "bank/sendEvent")
    public String sendEventToBankCore() {
        //Todo: implement
        return "";
    }
}
