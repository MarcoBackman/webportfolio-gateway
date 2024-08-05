package com.marco.gateway.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DateTimeUtilTest {

    @Test
    public void checkValidDateForm() {
        LocalDateTime testDateTime = LocalDateTime.of(2024, 6, 27, 11, 25, 12);

        try (MockedStatic<IDateTime> mockedStatic = Mockito.mockStatic(IDateTime.class)) {
            mockedStatic.when(IDateTime::getCurrentDateTime).thenReturn(testDateTime);

            String actualDateTime = DateTimeUtil.getCurrentDateTimeFormatted();
            assertThat(actualDateTime).isEqualTo("2024-06-27 11:25:12");
        }
    }

}
