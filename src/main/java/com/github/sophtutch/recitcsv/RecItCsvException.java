package com.github.sophtutch.recitcsv;

public class RecItCsvException extends RuntimeException {

    RecItCsvException(String message) {
        super(message);
    }

    RecItCsvException(String message, Throwable t) {
        super(message, t);
    }
}