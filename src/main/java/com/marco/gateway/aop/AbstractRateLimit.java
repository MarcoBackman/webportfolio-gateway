package com.marco.gateway.aop;

import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

/**
 * Has method that deals with incrementing, limiting, and resetting the rate
 */
public class AbstractRateLimit<K, V> {

    private final Logger logger;
    private final RedisTemplate<K, V> redisTemplate;
    private final V resetValue; //For initialization value
    AbstractRateLimit(Logger parentLogger,
                      RedisTemplate<K, V> redisTemplate,
                      V resetValue) {
        this.logger = parentLogger;
        this.redisTemplate = redisTemplate;
        this.resetValue = resetValue;
    }

    private Integer castVToInt(V value) {
        if (value == null) {
            return 0;
        } else if (value instanceof String) {
            return Integer.parseInt((String)value);
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long)value).intValue();
        } else if (value instanceof Character) {
            return Character.getNumericValue((Character) value);
        }
        throw new RuntimeException("Unsupported value type V for redis template: " + value.getClass());
    }

    /**
     * @param key key value for redis template - unique id
     * @param maxRequest number of max request during the given time
     * @param resetTime reset time
     * @return Long - returns the remaining reset time. -1 to represent no waiting time for the next request
     */
    protected Long processRateLimit(K key,
                                      Integer maxRequest,
                                      Integer resetTime) {
        //value - value of the requestedAmt .expire - reset time
        V value = redisTemplate.opsForValue().get(key);
        Long expire = redisTemplate.getExpire(key, TimeUnit.MINUTES);

        //Initial call - valid request
        if (value == null || expire == null || expire == -1) {
            logger.debug("No expiration time found. Resetting to 0. key={}", key);
            redisTemplate.opsForValue().set(key, resetValue);
            redisTemplate.opsForValue().increment(key);
            return (long) -1;
        }

        Integer valueInt = castVToInt(value);

        //Check reset time
        if (expire <= 0) { //expiration reached the reset cycle
            redisTemplate.expire(key, resetTime, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(key, resetValue);
            redisTemplate.opsForValue().increment(key);
            return (long) -1;
        } else if (valueInt > maxRequest) { //Exceeded request Amt return remaining time
            logger.debug("Request reached a limit key={}, valueInt={}", key, valueInt);
            return expire;
        } else { //Valid request - no waiting time -> -1
            logger.debug("Incrementing request count. key={}", key);
            redisTemplate.opsForValue().increment(key);
            return (long) -1;
        }
    }
}
