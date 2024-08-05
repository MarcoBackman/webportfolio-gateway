package com.marco.gateway.aop;

import com.marco.gateway.config.RedisBankProperties;
import com.marco.gateway.config.RedisProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.JoinPoint;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimiterCustomAspectTest {
    @InjectMocks
    RateLimiterCustomAspect rateLimiterCustomAspect;

    @Mock
    RedisTemplate<String, String> redisTemplate;
    RedisProperties redisProperties;
    RedisBankProperties redisBankProperties;
    ServerWebExchange serverWebExchange;

    RateLimiterCustomAspectTest () {
        serverWebExchange = mock(ServerWebExchange.class);
    }

    @BeforeEach
    public void init() {
        setRedisConfig();
        setRedisBankProperties();
        rateLimiterCustomAspect
                = spy(new RateLimiterCustomAspect(redisTemplate, redisProperties, redisBankProperties));
    }

    private void setRedisConfig() {
        redisProperties = new RedisProperties();
        redisProperties.setPort(1234);
        redisProperties.setServer("server");
        redisProperties.setMaxRequest(5);
        redisProperties.setResetTime(2);
    }

    private void setRedisBankProperties() {
        redisBankProperties = new RedisBankProperties();
        redisBankProperties.setResetTime(4);
        redisBankProperties.setMaxRequest(10);
    }

    private void mockRateLimitValues(String key,
                                     boolean isInitialSetup,
                                     boolean remainsWaitTime,
                                     boolean hasExceededRequestLimit,
                                     int maxExpirationTime,
                                     int maxRequestAmt) {
        ValueOperations<String, String> keyValue = mock(ValueOperations.class);
        doReturn(keyValue).when(redisTemplate).opsForValue();

        if (isInitialSetup) {
            doNothing().when(keyValue).set(eq(key), any());
            doReturn(null).when(redisTemplate).getExpire(key, TimeUnit.MINUTES);
            doReturn(null).when(keyValue).get(key);
            return;
        }

        if (remainsWaitTime) {
            doReturn((long)maxExpirationTime).when(redisTemplate).getExpire(key, TimeUnit.MINUTES);
        } else {
            doReturn((long)-1).when(redisTemplate).getExpire(key, TimeUnit.MINUTES);
        }

        if (hasExceededRequestLimit) {
            doReturn((long)(maxRequestAmt + 1)).when(keyValue).get(key);
        } else {
            doReturn((long)0).when(keyValue).get(key);
        }
    }

    @Nested
    class MessageServiceAOP {
        URI uri = new URI("/message-service/sendFeedbackMessage");
        private Signature signature;

        ProceedingJoinPoint joinPoint;
        //These values should match from test.properties
        private final int MAX_EXPIRATION_TIME = 2;
        private final int MAX_REQUEST_AMT = 5;

        MessageServiceAOP() throws Throwable {
            signature = mock(Signature.class);
            joinPoint = mock(ProceedingJoinPoint.class);
            doReturn(signature).when(joinPoint).getSignature();
            doReturn("Target").when(joinPoint).proceed();
            when(signature.toString()).thenReturn("key");
            when(joinPoint.getArgs()).thenReturn(new Object[]{serverWebExchange});
            when(serverWebExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR)).thenReturn(uri);
        }

        @Test
        public void messageRateLimiter_rateLimit_invalidateRequestOnMaxRequestTest() throws Throwable {
            mockRateLimitValues("key", false, true, true, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.messageRateLimiter(joinPoint);
            verify(rateLimiterCustomAspect, times(1))
                    .processRateLimit("key", MAX_REQUEST_AMT, MAX_EXPIRATION_TIME);
            assertThat(result).isInstanceOf(ResponseEntity.class);
            ResponseEntity<String> responseEntity = (ResponseEntity)result;
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }

        @Test
        public void messageRateLimiter_rateLimit_validRequestOnLowerThanMaxRequestTest() throws Throwable {
            mockRateLimitValues("key", false, false, false, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.messageRateLimiter(joinPoint);
            verify(rateLimiterCustomAspect, times(1))
                    .processRateLimit("key", MAX_REQUEST_AMT, MAX_EXPIRATION_TIME);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");
        }

        @Test
        public void messageRateLimiter_rateLimit_validRequest_onRemainingResetTime_withValidRequestAmtTest() throws Throwable {
            mockRateLimitValues("key", false, true, false, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.messageRateLimiter(joinPoint);
            verify(rateLimiterCustomAspect, times(1))
                    .processRateLimit("key", MAX_REQUEST_AMT, MAX_EXPIRATION_TIME);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");
        }

        @Test
        public void messageRateLimiter_rateLimit_validRequest_onWaitTimeReset_whenReachedRequestLimitTest() throws Throwable {
            mockRateLimitValues("key", false, false, true, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.messageRateLimiter(joinPoint);
            verify(rateLimiterCustomAspect, times(1))
                    .processRateLimit("key", MAX_REQUEST_AMT, MAX_EXPIRATION_TIME);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");
        }

        @Test
        public void messageRateLimiter_rateLimit_validRequest_onInitialRequestTest() throws Throwable {
            mockRateLimitValues("key", true, false, false, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.messageRateLimiter(joinPoint);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");

            mockRateLimitValues("key", true, true, false, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            result = rateLimiterCustomAspect.messageRateLimiter(joinPoint);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");

            mockRateLimitValues("key", true, false, true, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            result = rateLimiterCustomAspect.messageRateLimiter(joinPoint);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");

            mockRateLimitValues("key", true, true, true, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            result = rateLimiterCustomAspect.messageRateLimiter(joinPoint);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");
        }
    }


    @Nested
    class BankServiceAOP {
        URI uri = new URI("/bank/sendEvent");
        private Signature signature;
        JoinPoint joinPoint;
        //These values should match from test.properties
        private final int MAX_REQUEST_AMT = 10;
        private final int MAX_EXPIRATION_TIME = 4;
        public BankServiceAOP () throws URISyntaxException {
            signature = mock(Signature.class);
            joinPoint = mock(JoinPoint.class);
            doReturn(signature).when(joinPoint).getSignature();
            when(signature.toString()).thenReturn("key");
            doReturn("Target").when(joinPoint).getTarget();
            when(joinPoint.getArgs()).thenReturn(new Object[]{serverWebExchange});
            when(serverWebExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR)).thenReturn(uri);
        }

        @AfterEach
        public void verifyExecution() {
           verify(joinPoint, atLeast(1)).getSignature();
        }

        @Test
        public void bankServiceRateLimiter_rateLimit_invalidateRequestOnMaxRequestTest() {
            mockRateLimitValues("key", false, true, true, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.bankServiceRateLimiter(joinPoint);
            verify(rateLimiterCustomAspect, times(1))
                    .processRateLimit("key", MAX_REQUEST_AMT, MAX_EXPIRATION_TIME);
            assertThat(result).isInstanceOf(ResponseEntity.class);
            ResponseEntity<String> responseEntity = (ResponseEntity)result;
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }

        @Test
        public void bankServiceRateLimiter_rateLimit_validRequestOnLowerThanMaxRequestTest() {
            mockRateLimitValues("key", false, false, false, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.bankServiceRateLimiter(joinPoint);
            verify(rateLimiterCustomAspect, times(1))
                    .processRateLimit("key", MAX_REQUEST_AMT, MAX_EXPIRATION_TIME);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");
        }

        @Test
        public void bankServiceRateLimiter_rateLimit_validRequest_onRemainingResetTime_withValidRequestAmtTest() {
            mockRateLimitValues("key", false, true, false, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.bankServiceRateLimiter(joinPoint);
            verify(rateLimiterCustomAspect, times(1))
                    .processRateLimit("key", MAX_REQUEST_AMT, MAX_EXPIRATION_TIME);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");
        }

        @Test
        public void bankServiceRateLimiter_rateLimit_validRequest_onWaitTimeReset_whenReachedRequestLimitTest() {
            mockRateLimitValues("key", false, false, true, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.bankServiceRateLimiter(joinPoint);
            verify(rateLimiterCustomAspect, times(1))
                    .processRateLimit("key", MAX_REQUEST_AMT, MAX_EXPIRATION_TIME);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");
        }

        @Test
        public void bankServiceRateLimiter_rateLimit_validRequest_onInitialRequestTest() {
            mockRateLimitValues("key", true, false, false, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            Object result = rateLimiterCustomAspect.bankServiceRateLimiter(joinPoint);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");

            mockRateLimitValues("key", true, true, false, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            result = rateLimiterCustomAspect.bankServiceRateLimiter(joinPoint);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");

            mockRateLimitValues("key", true, false, true, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            result = rateLimiterCustomAspect.bankServiceRateLimiter(joinPoint);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");

            mockRateLimitValues("key", true, true, true, MAX_EXPIRATION_TIME, MAX_REQUEST_AMT);
            result = rateLimiterCustomAspect.bankServiceRateLimiter(joinPoint);
            assertThat(result).isInstanceOf(String.class);
            assertThat(result.toString()).isEqualTo("Target");
        }
    }
}
