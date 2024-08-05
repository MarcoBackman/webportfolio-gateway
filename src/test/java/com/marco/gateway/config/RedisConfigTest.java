package com.marco.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class RedisConfigTest {

    private final int REDIS_TEST_PORT = 6969;
    private final String REDIS_TEST_SERVER = "localhost";
    private final int REDIS_TEST_MAX_REQUEST = 5;
    private final int REDIS_TEST_RESET_TIME = 2;

    @Autowired
    private RedisConfig redisConfig;

    @Autowired
    private RedisProperties redisProperties;

    @Test
    public void redisConfigValueTest() {
        assertNotNull(redisConfig);
        assertThat(redisConfig.getPort()).isEqualTo(REDIS_TEST_PORT);
        assertThat(redisConfig.getServer()).isEqualTo(REDIS_TEST_SERVER);
    }

    @Test
    public void redisPropertyTest() {
        assertNotNull(redisProperties);
        assertThat(redisProperties.getPort()).isEqualTo(REDIS_TEST_PORT);
        assertThat(redisProperties.getServer()).isEqualTo(REDIS_TEST_SERVER);
        assertThat(redisProperties.getMaxRequest()).isEqualTo(REDIS_TEST_MAX_REQUEST);
        assertThat(redisProperties.getResetTime()).isEqualTo(REDIS_TEST_RESET_TIME);
    }
}
