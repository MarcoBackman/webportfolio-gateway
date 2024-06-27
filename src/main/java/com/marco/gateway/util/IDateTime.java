package com.marco.gateway.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IDateTime {

    static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

}
