package com.github.recitcsv;

import java.util.List;

class RecItCsvRowResult {

    private boolean matched;

    private List<RecItCsvFieldResult> fieldResults;

    RecItCsvRowResult(boolean matched, List<RecItCsvFieldResult> fieldResults) {
        this.matched = matched;
        this.fieldResults = fieldResults;
    }

    boolean isMatched() {
        return matched;
    }

    List<RecItCsvFieldResult> getFieldResults() {
        return fieldResults;
    }

    @Override
    public String toString() {
        return "matched=" + matched + " " + fieldResults;
    }
}