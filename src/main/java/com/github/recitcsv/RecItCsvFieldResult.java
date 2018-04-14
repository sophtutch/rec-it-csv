package com.github.recitcsv;

class RecItCsvFieldResult {

    private boolean matched;

    private String expected;
    private String actual;

    RecItCsvFieldResult(boolean matched, String expected, String actual) {
        this.matched = matched;
        this.expected = expected;
        this.actual = actual;
    }

    boolean isMatched() {
        return matched;
    }

    String getExpected() {
        return expected;
    }

    String getActual() {
        return actual;
    }

    @Override
    public String toString() {
        return "matched=" + matched + " expected=" + expected + " actual=" + actual;
    }
}