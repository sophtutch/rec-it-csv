package com.github.recitcsv;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecItCsvReconcilier {

    private final RecItCsvConfiguration configuration;

    RecItCsvReconcilier(RecItCsvConfiguration configuration) {
        this.configuration = configuration;
    }

    List<RecItCsvResult> reconcile() {
        List<RecItCsvResult> results = new LinkedList<>();
        for (RecItCsvConfiguration.FileConfiguration fileConfiguration : configuration.getFiles()) {
            Path expectedFile = fileConfiguration.getExpectedDir().orElse(configuration.getExpectedDir().orElseThrow(() -> new RecItCsvException("The expected result directory configured for '" + fileConfiguration.getName() + "' does not exist or is not a directory"))).toAbsolutePath().resolve(fileConfiguration.getName());
            Path actualFile = fileConfiguration.getActualDir().orElse(configuration.getActualDir().orElseThrow(() -> new RecItCsvException("The actual result directory configured for '" + fileConfiguration.getName() + "' does not exist or is not a directory"))).toAbsolutePath().resolve(fileConfiguration.getName());

            List<RecItCsvConfiguration.FileField> fields = fileConfiguration.getFields();

            boolean expectedFilePresent = checkFileExists(expectedFile).isPresent();
            boolean actualFilePresent = checkFileExists(actualFile).isPresent();
            if (expectedFilePresent && actualFilePresent) {
                double toleranceDiffActual = fileConfiguration.getNumericFieldDiffToleranceActual().orElse(configuration.getNumericFieldDiffToleranceActual().orElse(0.0d));
                double toleranceDiffPercentage = fileConfiguration.getNumericFieldDiffTolerancePercent().orElse(configuration.getNumericFieldDiffTolerancePercent().orElse(0.0d));
                try {
                    Map<List<String>, RecItCsvRow> expectedRows = readRows(fileConfiguration, expectedFile);
                    Map<List<String>, RecItCsvRow> actualRows = readRows(fileConfiguration, actualFile);

                    List<String> missingFromExpected = getMissingRows(actualRows, expectedRows);
                    List<String> missingFromActual = getMissingRows(expectedRows, actualRows);

                    List<RecItCsvRowResult> rowResults = reconcileRows(fields, toleranceDiffActual, toleranceDiffPercentage, expectedRows, actualRows);

                    boolean matched = missingFromExpected.isEmpty() && missingFromActual.isEmpty() && rowResults.stream().allMatch(RecItCsvRowResult::isMatched);
                    results.add(new RecItCsvResult(matched, fileConfiguration, missingFromExpected, missingFromActual, rowResults));
                } catch (IOException e) {
                    results.add(new RecItCsvResult(false, fileConfiguration, "Failed to read files for reconciliation. " + e.getMessage()));
                }
            } else {
                results.add(new RecItCsvResult(false, fileConfiguration, "Expected file " + (expectedFilePresent ? "" : "not ") + "present. Actual file " + (actualFilePresent ? "" : "not ") + "present."));
            }
        }
        return results;
    }

    private Optional<Path> checkFileExists(Path file) {
        if (Files.notExists(file) || Files.isDirectory(file)) {
            return Optional.empty();
        }
        return Optional.of(file);
    }

    private Map<List<String>, RecItCsvRow> readRows(RecItCsvConfiguration.FileConfiguration fileConfiguration, Path file) throws IOException {
        String separator = fileConfiguration.getSeparator();

        List<RecItCsvConfiguration.FileField> fields = fileConfiguration.getFields();
        return Files.lines(file)
                .map(line -> {
                    List<String> rowKey = new LinkedList<>();
                    List<RecItCsvTuple> row = new LinkedList<>();

                    String[] split = line.split(separator, fields.size());
                    for (int index = 0; index < split.length; index++) {
                        RecItCsvConfiguration.FileField fileField = fields.get(index);

                        String string = split[index];
                        if (fileField.isKey()) {
                            rowKey.add(string);
                        }
                        row.add(new RecItCsvTuple(string, getObject(fileField.getType(), fileField.getFormat(), string)));
                    }
                    return new RecItCsvRow(rowKey, line, row);
                })
                .collect(
                        Collectors.toMap(RecItCsvRow::getKey, row -> row, (u, v) -> {
                            throw new RecItCsvException("A duplicate key has been found for file '" + file.toAbsolutePath() + "'. A key should uniquely identify a row.  Please check your configuration.");
                        }, LinkedHashMap::new)
                );
    }

    private Object getObject(RecItCsvFieldType type, String format, String string) {
        switch (type) {
            case BOOLEAN:
                return isNotNullOrEmpty(string) ? Boolean.valueOf(string) : null;
            case STRING:
                return isNotNullOrEmpty(string) ? string : null;
            case NUMERIC:
                return isNotNullOrEmpty(string) ? new BigDecimal(string) : null;
            case DATE:
                return isNotNullOrEmpty(string) ? LocalDate.parse(string, DateTimeFormatter.ofPattern(format)) : null;
            case TIME:
                return isNotNullOrEmpty(string) ? LocalTime.parse(string, DateTimeFormatter.ofPattern(format)) : null;
            case DATETIME:
                return isNotNullOrEmpty(string) ? LocalDateTime.parse(string, DateTimeFormatter.ofPattern(format)) : null;
            default:
                return null;
        }
    }

    private boolean isNotNullOrEmpty(String in) {
        return in != null && !in.isEmpty();
    }

    private List<String> getMissingRows(Map<List<String>, RecItCsvRow> first, Map<List<String>, RecItCsvRow> second) {
        return first.keySet().stream()
                .filter(key -> !second.containsKey(key))
                .map(key -> first.get(key).getLine())
                .collect(Collectors.toList());
    }

    private List<RecItCsvRowResult> reconcileRows(List<RecItCsvConfiguration.FileField> fields, double toleranceDiffActual, double toleranceDiffPercentage, Map<List<String>, RecItCsvRow> expectedRows, Map<List<String>, RecItCsvRow> actualRows) {
        List<RecItCsvRowResult> rowResults = new LinkedList<>();
        for (List<String> key : expectedRows.keySet()) {
            List<RecItCsvFieldResult> fieldResults = new LinkedList<>();

            RecItCsvRow expectedFields = expectedRows.get(key);
            if (actualRows.containsKey(key)) {
                RecItCsvRow actualFields = actualRows.get(key);

                List<RecItCsvTuple> expectedRow = expectedFields.getRow();
                List<RecItCsvTuple> actualRow = actualFields.getRow();

                boolean fieldsMatch = true;
                for (int index = 0; index < expectedRow.size(); index++) {
                    RecItCsvFieldResult fieldResult;
                    RecItCsvTuple expectedTuple = expectedRow.get(index);
                    if (index < actualRow.size()) {
                        RecItCsvTuple actualTuple = actualRow.get(index);
                        boolean equals = isEquals(fields.get(index).getType(), toleranceDiffActual, toleranceDiffPercentage, expectedTuple.getAfter(), actualTuple.getAfter());
                        fieldResult = new RecItCsvFieldResult(equals, expectedTuple.getBefore(), actualTuple.getBefore());
                    } else {
                        fieldResult = new RecItCsvFieldResult(false, expectedTuple.getBefore(), null);
                        fieldsMatch = false;
                    }
                    fieldResults.add(fieldResult);
                }
                rowResults.add(new RecItCsvRowResult(fieldsMatch, fieldResults));
            }
        }
        return rowResults;
    }

    private boolean isEquals(RecItCsvFieldType fieldType, double toleranceDiffActual, double toleranceDiffPercentage, Object expected, Object actual) {
        boolean equals = Objects.equals(expected, actual);
        if (!equals && RecItCsvFieldType.NUMERIC == fieldType) {
            equals = Objects.equals(expected, actual) || withinTolerance(toleranceDiffActual, toleranceDiffPercentage, (BigDecimal) expected, (BigDecimal) actual);
        }
        return equals;
    }

    private boolean withinTolerance(double toleranceDiffActual, double toleranceDiffPercentage, BigDecimal expected, BigDecimal actual) {
        return false;
    }
}