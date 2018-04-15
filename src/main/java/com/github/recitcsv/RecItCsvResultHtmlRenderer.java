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
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        if (Files.notExists(outputPath)) {
            outputPath = Files.createDirectory(outputPath);
        }

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

            RecItCsvConfiguration.FileConfiguration fileConfiguration = fileResult.getFileConfiguration();

            Path fileExpectedDir = fileConfiguration.getExpectedDir().orElse(expectedDir).toAbsolutePath();
            Path fileActualDir = fileConfiguration.getActualDir().orElse(actualDir).toAbsolutePath();

            String filePath = fileActualDir.resolve(fileConfiguration.getName()).toString();

            Double diffToleranceActual = fileConfiguration.getNumericFieldDiffToleranceActual().orElse(numericFieldDiffToleranceActual);
            Double diffTolerancePercent = fileConfiguration.getNumericFieldDiffTolerancePercent().orElse(numericFieldDiffTolerancePercent);

            Context context = new Context();
            context.setVariable("name", filePath);
            context.setVariable("separator", fileConfiguration.getSeparator());
            context.setVariable("expectedDir", fileExpectedDir);
            context.setVariable("actualDir", fileActualDir);
            context.setVariable("numericFieldDiffToleranceActual", diffToleranceActual);
            context.setVariable("numericFieldDiffTolerancePercent", diffTolerancePercent);
            context.setVariable("fields", fileConfiguration.getFields());

            context.setVariable("matched", matched);
            context.setVariable("missingFromExpected", missingFromExpected);
            context.setVariable("missingFromActual", missingFromActual);
            context.setVariable("rowResults", rowResults);
            context.setVariable("errorMessage", fileResult.getErrorMessage());

            StringWriter stringWriter = new StringWriter();
            templateEngine.process("file", context, stringWriter);

            Path resultFile = outputPath.resolve(UUID.randomUUID().toString() + ".html");
            Files.deleteIfExists(resultFile);
            Files.createFile(resultFile);

            Files.write(resultFile, stringWriter.toString().getBytes(), StandardOpenOption.APPEND);
            resultFileNames.add(new RecItCsvTuple3<>(filePath, resultFile.getFileName().toString(), matched));
        }

        Context context = new Context();
        context.setVariable("matched", result.isMatched());
        context.setVariable("expectedDir", expectedDir);
        context.setVariable("actualDir", actualDir);
        context.setVariable("numericFieldDiffToleranceActual", numericFieldDiffToleranceActual);
        context.setVariable("numericFieldDiffTolerancePercent", numericFieldDiffTolerancePercent);
        context.setVariable("fileNames", resultFileNames);

        StringWriter stringWriter = new StringWriter();
        templateEngine.process("home", context, stringWriter);

        Path resultFile = outputPath.resolve("home.html");
        Files.deleteIfExists(resultFile);
        Files.createFile(resultFile);

        Files.write(resultFile, stringWriter.toString().getBytes(), StandardOpenOption.APPEND);

        return resultFileNames.stream().map(t -> Paths.get(t.getFirst())).collect(Collectors.toList());
    }

    private <T> List<T> nullIfNullOrEmpty(List<T> orig) {
        return orig == null || orig.isEmpty() ? null : orig;
    }
}