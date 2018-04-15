package com.github.recitcsv;

import java.util.List;
import java.util.Objects;

class RecItCsvRow {

    private final List<String> key;
    private final String line;
    private final List<RecItCsvTuple2<String, Object>> row;

    RecItCsvRow(List<String> key, String line, List<RecItCsvTuple2<String, Object>> row) {
        this.key = key;
        this.line = line;
        this.row = row;
    }

    List<String> getKey() {
        return key;
    }

    String getLine() {
        return line;
    }

    List<RecItCsvTuple2<String, Object>> getRow() {
        return row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecItCsvRow recItCsvRow = (RecItCsvRow) o;
        return Objects.equals(key, recItCsvRow.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}