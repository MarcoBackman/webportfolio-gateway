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

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
@EnableAspectJAutoProxy
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

        Long remainingResetTime = processRateLimit(
                key,
                redisProperties.getMaxRequest(),
                redisProperties.getResetTime());
        if (remainingResetTime > 0) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many request. Please try again after " + remainingResetTime + " minutes");
        } else {
            return joinPoint.proceed();
        }
    }

    // Router rate limit8
    @Before("execution(* com.marco.gateway.controller.EventController.*(..))")
    public Object bankServiceRateLimiter(JoinPoint joinPoint) {
        logger.info("Routing request to bank-service: {}", joinPoint.toShortString());
        String key = joinPoint.getSignature().toString();

        Long remainingResetTime = processRateLimit(
                key,
                redisBankProperties.getMaxRequest(),
                redisBankProperties.getResetTime());
        if (remainingResetTime > 0) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many request. Please try again after " + remainingResetTime + " minutes");
        }
        return joinPoint.getTarget();
    }
}


