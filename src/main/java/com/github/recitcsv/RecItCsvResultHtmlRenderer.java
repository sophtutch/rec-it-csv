package com.github.recitcsv;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class RecItCsvResultHtmlRenderer {

    private final RecItCsvConfiguration configuration;

    RecItCsvResultHtmlRenderer(RecItCsvConfiguration configuration) {
        this.configuration = configuration;
    }

    List<Path> render(Path outputPath, RecItCsvResult result) throws IOException {
        TemplateEngine templateEngine = createTemplateEngine();

        Path expectedDir = configuration.getExpectedDir().orElse(Paths.get("")).toAbsolutePath();
        Path actualDir = configuration.getActualDir().orElse(Paths.get("")).toAbsolutePath();
        Double numericFieldDiffToleranceActual = configuration.getNumericFieldDiffToleranceActual().orElse(null);
        Double numericFieldDiffTolerancePercent = configuration.getNumericFieldDiffTolerancePercent().orElse(null);

        List<RecItCsvTuple3<String, String, Boolean>> resultFileNames = new LinkedList<>();
        for (RecItCsvFileResult fileResult : result.getFileResults()) {
            boolean matched = fileResult.isMatched();

            List<String> missingFromExpected = nullIfNullOrEmpty(fileResult.getMissingFromExpected());
            List<String> missingFromActual = nullIfNullOrEmpty(fileResult.getMissingFromActual());

            List<RecItCsvRowResult> rowResults = nullIfNullOrEmpty(fileResult.getRowResults());
            if (rowResults != null) {
                rowResults = nullIfNullOrEmpty(rowResults.stream().filter(row -> !row.isMatched()).collect(Collectors.toList()));
            }

            String errorMessage = fileResult.getErrorMessage();

            RecItCsvConfiguration.FileConfiguration fileConfiguration = fileResult.getFileConfiguration();
            String name = fileConfiguration.getName();
            String separator = fileConfiguration.getSeparator();
            Path fileExpectedDir = fileConfiguration.getExpectedDir().orElse(expectedDir).toAbsolutePath();
            Path fileActualDir = fileConfiguration.getActualDir().orElse(actualDir).toAbsolutePath();
            List<RecItCsvConfiguration.FileField> fields = fileConfiguration.getFields();

            String filePath = fileActualDir.resolve(name).toString();

            Double diffToleranceActual = fileConfiguration.getNumericFieldDiffToleranceActual().orElse(numericFieldDiffToleranceActual);
            Double diffTolerancePercent = fileConfiguration.getNumericFieldDiffTolerancePercent().orElse(numericFieldDiffTolerancePercent);

            Context context = createFileContext(matched, filePath, separator, fields, fileExpectedDir, fileActualDir, diffToleranceActual, diffTolerancePercent, missingFromExpected, missingFromActual, rowResults, errorMessage);

            StringWriter stringWriter = new StringWriter();
            templateEngine.process("file", context, stringWriter);

            Path resultFile = writeResultFile(outputPath, stringWriter.toString());
            resultFileNames.add(new RecItCsvTuple3<>(filePath, resultFile.getFileName().toString(), fileResult.isMatched()));
        }
        writeHomeFile(outputPath, templateEngine, createContext(expectedDir, actualDir, numericFieldDiffToleranceActual, numericFieldDiffTolerancePercent, resultFileNames, result.isMatched()));

        return resultFileNames.stream().map(t -> Paths.get(t.getFirst())).collect(Collectors.toList());
    }

    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

    private void writeHomeFile(Path outputPath, TemplateEngine templateEngine, Context context) throws IOException {
        StringWriter stringWriter = new StringWriter();
        templateEngine.process("home", context, stringWriter);

        Path resultFile = outputPath.resolve("home.html");
        Files.deleteIfExists(resultFile);
        Files.createFile(resultFile);

        Files.write(resultFile, stringWriter.toString().getBytes(), StandardOpenOption.APPEND);
    }

    private <T> List<T> nullIfNullOrEmpty(List<T> orig) {
        return orig == null || orig.isEmpty() ? null : orig;
    }

    private Context createFileContext(boolean matched, String fileName, String separator, List<RecItCsvConfiguration.FileField> fields, Path fileExpectedDir, Path fileActualDir, Double diffToleranceActual, Double diffTolerancePercent, List<String> missingFromExpected, List<String> missingFromActual, List<RecItCsvRowResult> rowResults, String errorMessage) {
        Context context = new Context();
        context.setVariable("name", fileName);
        context.setVariable("separator", separator);
        context.setVariable("expectedDir", fileExpectedDir);
        context.setVariable("actualDir", fileActualDir);
        context.setVariable("numericFieldDiffToleranceActual", diffToleranceActual);
        context.setVariable("numericFieldDiffTolerancePercent", diffTolerancePercent);
        context.setVariable("fields", fields);
        context.setVariable("matched", matched);
        context.setVariable("missingFromExpected", missingFromExpected);
        context.setVariable("missingFromActual", missingFromActual);
        context.setVariable("rowResults", rowResults);
        context.setVariable("errorMessage", errorMessage);
        return context;
    }

    private Context createContext(Path expectedDir, Path actualDir, Double numericFieldDiffToleranceActual, Double numericFieldDiffTolerancePercent, List<RecItCsvTuple3<String, String, Boolean>> resultFileNames, boolean matched) {
        Context context = new Context();
        context.setVariable("matched", matched);
        context.setVariable("expectedDir", expectedDir);
        context.setVariable("actualDir", actualDir);
        context.setVariable("numericFieldDiffToleranceActual", numericFieldDiffToleranceActual);
        context.setVariable("numericFieldDiffTolerancePercent", numericFieldDiffTolerancePercent);
        context.setVariable("fileNames", resultFileNames);
        return context;
    }

    private Path writeResultFile(Path outputPath, String string) throws IOException {
        if (Files.notExists(outputPath)) {
            outputPath = Files.createDirectory(outputPath);
        }

        Path resultFile = outputPath.resolve(UUID.randomUUID().toString() + ".html");
        Files.deleteIfExists(resultFile);
        Files.createFile(resultFile);

        Files.write(resultFile, string.getBytes(), StandardOpenOption.APPEND);
        return resultFile;
    }
}