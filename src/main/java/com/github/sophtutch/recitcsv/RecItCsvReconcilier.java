package com.github.sophtutch.recitcsv;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecItCsvReconcilier {

    private final RecItCsvConfiguration configuration;

    RecItCsvReconcilier(RecItCsvConfiguration configuration) {
        this.configuration = configuration;
    }

    RecItCsvResult reconcile() {
        boolean matched = true;
        List<RecItCsvFileResult> results = new LinkedList<>();
        List<RecItCsvTuple3<RecItCsvConfiguration.FileConfiguration, Path, Path>> expectedFiles = buildExpectedFileList();
        for (RecItCsvTuple3<RecItCsvConfiguration.FileConfiguration, Path, Path> tuple : expectedFiles) {
            RecItCsvConfiguration.FileConfiguration fileConfiguration = tuple.getFirst();

            Path expectedFile = tuple.getSecond();
            Path actualFile = tuple.getThird();

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

                    boolean fileMatched = missingFromExpected.isEmpty() && missingFromActual.isEmpty() && rowResults.stream().allMatch(RecItCsvRowResult::isMatched);
                    matched &= fileMatched;
                    results.add(new RecItCsvFileResult(fileMatched, expectedFile, actualFile, fileConfiguration, missingFromExpected, missingFromActual, rowResults));
                } catch (IOException e) {
                    matched = false;
                    results.add(new RecItCsvFileResult(false, expectedFile, actualFile, fileConfiguration, "Failed to read files for reconciliation. " + e.getMessage()));
                }
            } else {
                matched = false;
                results.add(new RecItCsvFileResult(false, expectedFile, actualFile, fileConfiguration, "Expected file " + (expectedFilePresent ? "" : "not ") + "present. Actual file " + (actualFilePresent ? "" : "not ") + "present."));
            }
        }
        return new RecItCsvResult(matched, results);
    }

    private List<RecItCsvTuple3<RecItCsvConfiguration.FileConfiguration, Path, Path>> buildExpectedFileList() {
        return configuration.getFiles().stream()
                .flatMap(fileConfiguration -> {
                    Path expectedDirRoot = fileConfiguration.getExpectedDir().orElse(configuration.getExpectedDir().orElseThrow(() -> new RecItCsvException("The expected result directory configured for '" + fileConfiguration.getName() + "' does not exist or is not a directory"))).toAbsolutePath();
                    Path actualDirRoot = fileConfiguration.getActualDir().orElse(configuration.getActualDir().orElseThrow(() -> new RecItCsvException("The actual result directory configured for '" + fileConfiguration.getName() + "' does not exist or is not a directory"))).toAbsolutePath();

                    String name = fileConfiguration.getName();
                    try {
                        return Stream.concat(Files.walk(expectedDirRoot).filter(file -> Files.isRegularFile(file)).map(expectedDirRoot::relativize), Files.walk(actualDirRoot).filter(file -> Files.isRegularFile(file)).map(actualDirRoot::relativize)).distinct()
                                .filter(file -> {
                                    if (name.startsWith("glob:") || name.startsWith("regex:")) {
                                        return FileSystems.getDefault().getPathMatcher(name).matches(file);
                                    } else {
                                        return file.getFileName().toString().endsWith(fileConfiguration.getName());
                                    }
                                })
                                .map(file -> {
                                    Path expectedFile = expectedDirRoot.resolve(file);
                                    Path actualFile = actualDirRoot.resolve(file);

                                    return new RecItCsvTuple3<>(fileConfiguration, expectedFile, actualFile);
                                });
                    } catch (IOException e) {
                        throw new RecItCsvException("Failed to build expected file list. ", e);
                    }
                })
                .collect(Collectors.toList());
    }

    private Optional<Path> checkFileExists(Path file) {
        if (Files.notExists(file) || Files.isDirectory(file)) {
            return Optional.empty();
        }
        return Optional.of(file);
    }

    private Map<List<String>, RecItCsvRow> readRows(RecItCsvConfiguration.FileConfiguration fileConfiguration, Path file) throws IOException {
        String separator = fileConfiguration.getSeparator();

        AtomicInteger count = new AtomicInteger();
        List<RecItCsvConfiguration.FileField> fields = fileConfiguration.getFields();
        return Files.lines(file)
                .map(line -> {
                    int lineNunmber = count.incrementAndGet();

                    List<String> rowKey = new LinkedList<>();
                    List<RecItCsvTuple2<String, Object>> row = new LinkedList<>();

                    String[] split = line.split(Pattern.quote(separator), fields.size());
                    for (int index = 0; index < split.length; index++) {
                        RecItCsvConfiguration.FileField fileField = fields.get(index);

                        String string = split[index];
                        if (fileField.isKey()) {
                            rowKey.add(string);
                        }

                        Object object;
                        try {
                            object = getObject(fileField.getType(), fileField.getFormat(), string);
                        } catch (Exception e) {
                            throw new RecItCsvException(MessageFormat.format("Failed to parse {0} to type {1} using format {2} on line number {3} in file {4}", string, fileField.getType(), fileField.getFormat(), lineNunmber, file.toAbsolutePath()), e);
                        }
                        row.add(new RecItCsvTuple2<>(string, object));
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

                List<RecItCsvTuple2<String, Object>> expectedRow = expectedFields.getRow();
                List<RecItCsvTuple2<String, Object>> actualRow = actualFields.getRow();

                boolean fieldsMatch = true;
                for (int index = 0; index < expectedRow.size(); index++) {
                    RecItCsvFieldResult fieldResult;
                    RecItCsvTuple2<String, Object> expectedTuple = expectedRow.get(index);
                    if (index < actualRow.size()) {
                        RecItCsvTuple2<String, Object> actualTuple = actualRow.get(index);
                        boolean equals = isEquals(fields.get(index).getType(), toleranceDiffActual, toleranceDiffPercentage, expectedTuple.getSecond(), actualTuple.getSecond());
                        fieldResult = new RecItCsvFieldResult(equals, expectedTuple.getFirst(), actualTuple.getFirst());
                        fieldsMatch &= equals;
                    } else {
                        fieldResult = new RecItCsvFieldResult(false, expectedTuple.getFirst(), null);
                        fieldsMatch &= false;
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