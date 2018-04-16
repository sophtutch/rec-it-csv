package com.github.sophtutch.recitcsv;

import java.nio.file.Path;
import java.util.List;

class RecItCsvFileResult {

    private final boolean matched;
    private final Path expectedFile;
    private final Path actualFile;
    private final RecItCsvConfiguration.FileConfiguration fileConfiguration;

    private List<String> missingFromExpected;
    private List<String> missingFromActual;

    private List<RecItCsvRowResult> rowResults;

    private String errorMessage;

    RecItCsvFileResult(boolean matched, Path expectedFile, Path actualFile, RecItCsvConfiguration.FileConfiguration fileConfiguration, List<String> missingFromExpected, List<String> missingFromActual, List<RecItCsvRowResult> rowResults) {
        this.matched = matched;
        this.expectedFile = expectedFile;
        this.actualFile = actualFile;
        this.fileConfiguration = fileConfiguration;
        this.missingFromExpected = missingFromExpected;
        this.missingFromActual = missingFromActual;
        this.rowResults = rowResults;
    }

    RecItCsvFileResult(boolean matched, Path expectedFile, Path actualFile, RecItCsvConfiguration.FileConfiguration fileConfiguration, String errorMessage) {
        this.matched = matched;
        this.expectedFile = expectedFile;
        this.actualFile = actualFile;
        this.fileConfiguration = fileConfiguration;
        this.errorMessage = errorMessage;
    }

    public boolean isMatched() {
        return matched;
    }

    public Path getExpectedFile() {
        return expectedFile;
    }

    public Path getActualFile() {
        return actualFile;
    }

    public RecItCsvConfiguration.FileConfiguration getFileConfiguration() {
        return fileConfiguration;
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

    public String getErrorMessage() {
        return errorMessage;
    }
}