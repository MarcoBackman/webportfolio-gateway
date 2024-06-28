package com.marco.gateway.aop;

import com.marco.gateway.config.RedisBankProperties;
import com.marco.gateway.config.RedisProperties;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

@Aspect
@Component
public class RateLimiterCustomAspect extends AbstractRateLimit<String, String> {
    private final static Logger logger = LoggerFactory.getLogger(RateLimiterCustomAspect.class);
    private final RedisProperties redisProperties;
    private final RedisBankProperties redisBankProperties;

    public RateLimiterCustomAspect(RedisTemplate<String, String> redisTemplate,
                                   RedisProperties redisProperties,
                                   RedisBankProperties redisBankProperties) {
        super(logger, redisTemplate, "0");
        this.redisProperties = redisProperties;
        this.redisBankProperties = redisBankProperties;
    }

    @PostConstruct
    public void init() {
        logger.info("RateLimiterAspect initialized");
    }

    @Primary
    @Around("execution(* com.marco.gateway.controller.FeedbackController.*(..))")
    public Object messageRateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.debug("contact me request found");
        String key = joinPoint.getSignature().toString();

        Long expirationTime = processRateLimit(
                key,
                redisProperties.getMaxRequest(),
                redisProperties.getResetTime());
        if (expirationTime != -1) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many request. Please try again after " + expirationTime + " minutes");
        } else {
            return joinPoint.proceed();
        }
    }

    // Router rate limit8
    @Before("execution(* org.springframework.cloud.gateway.handler.FilteringWebHandler.*(..))")
    public Object bankServiceRateLimiter(JoinPoint joinPoint) {

        ServerWebExchange exchange = (ServerWebExchange) joinPoint.getArgs()[0];
        URI url = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        if(url != null && url.getPath().startsWith("/bank-service")){
            // access the request object and log important info
            logger.info("Routing request to bank-service: {}", joinPoint.toShortString());
            logger.debug("bank request found");
            String key = joinPoint.getSignature().toString();
            Long expirationTime = processRateLimit(
                    key,
                    redisBankProperties.getMaxRequest(),
                    redisBankProperties.getResetTime());
            if (expirationTime != -1) {
                return ResponseEntity
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Too many request. Please try again after " + expirationTime + " minutes");
            }
        }
        return joinPoint.getTarget();
    }


}