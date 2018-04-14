package com.github.recitcsv;

import java.util.List;

class RecItCsvResult {

    private final List<String> missingFromExpected;
    private final List<String> missingFromActual;

    private final List<RecItCsvRowResult> rowResults;

    RecItCsvResult(RecItCsvConfiguration.FileConfiguration fileConfiguration, List<String> missingFromExpected, List<String> missingFromActual, List<RecItCsvRowResult> rowResults) {
        this.missingFromExpected = missingFromExpected;
        this.missingFromActual = missingFromActual;
        this.rowResults = rowResults;
    }

    public List<String> getMissingFromExpected() {
        return missingFromExpected;
    }

    public List<String> getMissingFromActual() {
        return missingFromActual;
    }

    public List<RecItCsvRowResult> getRowResults() {
        return rowResults;
    }
}