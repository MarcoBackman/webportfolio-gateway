package com.marco.gateway.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PortfolioFeedback {
    String name;
    String email;
    String content;
}
