package com.github.recitcsv;

public enum RecItCsvFieldType {
    BOOLEAN, STRING, NUMERIC, DECIMAL, DATE, TIME, DATETIME;

    public static RecItCsvFieldType find(String string) {
        for (RecItCsvFieldType type : RecItCsvFieldType.values()) {
            if (type.name().equalsIgnoreCase(string)) {
                return type;
            }
        }
        throw new RecItCsvException("The configured type '" + string + "' cannot be processed");
    }
}