package com.marco.gateway.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Use this to avoid test failure due to AOP check.
 */
@DisplayName("Bypassing AOP functionality")
public abstract class RateLimiterMockTest {
    @MockBean
    private RateLimiterCustomAspect rateLimiterAspect;  //Redis for AOP

    @BeforeEach
    public void bypassMessageRateLimiter() throws Throwable {
        ResponseEntity<String> response = ResponseEntity
                .status(HttpStatus.OK)
                .body("Request is valid");
        when(rateLimiterAspect.messageRateLimiter(any(ProceedingJoinPoint.class)))
                .thenReturn(response);
    }

    @BeforeEach
    public void bypassBankServiceRateLimiter() throws Throwable {
        ResponseEntity<String> response = ResponseEntity
                .status(HttpStatus.OK)
                .body("Request is valid");
        when(rateLimiterAspect.bankServiceRateLimiter(any(ProceedingJoinPoint.class)))
                .thenReturn(response);
    }
}
