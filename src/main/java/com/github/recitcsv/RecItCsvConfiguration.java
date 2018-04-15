package com.github.recitcsv;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class RecItCsvConfiguration {

    private String expectedDir;
    private String actualDir;
    private String outputDir;

    private Double numericFieldDiffToleranceActual;
    private Double numericFieldDiffTolerancePercent;

    private Collection<FileConfiguration> files;

    public Optional<Path> getExpectedDir() {
        return Objects.nonNull(expectedDir) ? Optional.of(Paths.get(expectedDir)) : Optional.empty();
    }

    void setExpectedDir(String expectedDir) {
        this.expectedDir = expectedDir;
    }

    public Optional<Path> getActualDir() {
        return Objects.nonNull(actualDir) ? Optional.of(Paths.get(actualDir)) : Optional.empty();
    }

    void setActualDir(String actualDir) {
        this.actualDir = actualDir;
    }

    public Optional<Double> getNumericFieldDiffToleranceActual() {
        return Optional.ofNullable(numericFieldDiffToleranceActual);
    }

    void setNumericFieldDiffToleranceActual(double numericFieldDiffToleranceActual) {
        this.numericFieldDiffToleranceActual = numericFieldDiffToleranceActual;
    }

    public Optional<Double> getNumericFieldDiffTolerancePercent() {
        return Optional.ofNullable(numericFieldDiffTolerancePercent);
    }

    void setNumericFieldDiffTolerancePercent(double numericFieldDiffTolerancePercent) {
        this.numericFieldDiffTolerancePercent = numericFieldDiffTolerancePercent;
    }

    public Collection<FileConfiguration> getFiles() {
        return files;
    }

    void setFiles(Collection<FileConfiguration> files) {
        this.files = files;
    }

    public Path getOutputDir() {
        return Paths.get(outputDir);
    }

    static class FileConfiguration {
        private String name;
        private String expectedDir;
        private String actualDir;

        private String separator;
        private Double numericFieldDiffToleranceActual;
        private Double numericFieldDiffTolerancePercent;

        private List<FileField> fields;

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        Optional<Path> getExpectedDir() {
            return Objects.nonNull(expectedDir) ? Optional.of(Paths.get(expectedDir)) : Optional.empty();
        }

        void setExpectedDir(String expectedDir) {
            this.expectedDir = expectedDir;
        }

        Optional<Path> getActualDir() {
            return Objects.nonNull(actualDir) ? Optional.of(Paths.get(actualDir)) : Optional.empty();
        }

        void setActualDir(String actualDir) {
            this.actualDir = actualDir;
        }

        String getSeparator() {
            return separator;
        }

        void setSeparator(String separator) {
            this.separator = separator;
        }

        Optional<Double> getNumericFieldDiffToleranceActual() {
            return Optional.ofNullable(numericFieldDiffToleranceActual);
        }

        void setNumericFieldDiffToleranceActual(double numericFieldDiffToleranceActual) {
            this.numericFieldDiffToleranceActual = numericFieldDiffToleranceActual;
        }

        Optional<Double> getNumericFieldDiffTolerancePercent() {
            return Optional.ofNullable(numericFieldDiffTolerancePercent);
        }

        void setNumericFieldDiffTolerancePercent(double numericFieldDiffTolerancePercent) {
            this.numericFieldDiffTolerancePercent = numericFieldDiffTolerancePercent;
        }

        List<FileField> getFields() {
            return fields;
        }

        void setFields(List<FileField> fields) {
            this.fields = fields;
        }
    }

    static class FileField {
        private String name;
        private String type;

        private String format;

        private boolean key;

        public String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public RecItCsvFieldType getType() {
            return RecItCsvFieldType.find(type);
        }

        void setType(String type) {
            this.type = type;
        }

        public String getFormat() {
            return format;
        }

        void setFormat(String format) {
            this.format = format;
        }

        public boolean isKey() {
            return key;
        }

        void setKey(boolean key) {
            this.key = key;
        }
    }
}