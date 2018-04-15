package com.github.sophtutch.recitcsv;

import java.util.List;

class RecItCsvRowResult {

    private boolean matched;

    private List<RecItCsvFieldResult> fieldResults;

    RecItCsvRowResult(boolean matched, List<RecItCsvFieldResult> fieldResults) {
        this.matched = matched;
        this.fieldResults = fieldResults;
    }

    public boolean isMatched() {
        return matched;
    }

    public List<RecItCsvFieldResult> getFieldResults() {
        return fieldResults;
    }

    @Override
    public String toString() {
        return "matched=" + matched + " " + fieldResults;
    }
}