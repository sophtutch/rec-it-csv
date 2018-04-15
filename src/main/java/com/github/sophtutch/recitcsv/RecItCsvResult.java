package com.github.sophtutch.recitcsv;

import java.util.List;

public class RecItCsvResult {

    private final boolean matched;
    private final List<RecItCsvFileResult> fileResults;

    RecItCsvResult(boolean matched, List<RecItCsvFileResult> fileResults) {
        this.matched = matched;
        this.fileResults = fileResults;
    }

    public boolean isMatched() {
        return matched;
    }

    public List<RecItCsvFileResult> getFileResults() {
        return fileResults;
    }
}