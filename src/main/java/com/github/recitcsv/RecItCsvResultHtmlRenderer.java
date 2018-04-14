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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

class RecItCsvResultHtmlRenderer {

    private final RecItCsvConfiguration configuration;

    RecItCsvResultHtmlRenderer(RecItCsvConfiguration configuration) {
        this.configuration = configuration;
    }

    void render(List<RecItCsvResult> results) throws IOException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Path tmp = Paths.get("tmp");
        if (Files.notExists(tmp)) {
            tmp = Files.createDirectory(tmp);
        }

        Path expectedDir = configuration.getExpectedDir().orElse(Paths.get("")).toAbsolutePath();
        Path actualDir = configuration.getActualDir().orElse(Paths.get("")).toAbsolutePath();
        Double numericFieldDiffToleranceActual = configuration.getNumericFieldDiffToleranceActual().orElse(null);
        Double numericFieldDiffTolerancePercent = configuration.getNumericFieldDiffTolerancePercent().orElse(null);

        Map<String, String> resultFileNames = new LinkedHashMap<>();
        for (RecItCsvResult result : results) {
            boolean matched = result.isMatched();
            List<String> missingFromExpected = result.getMissingFromExpected();
            List<String> missingFromActual = result.getMissingFromActual();
            List<RecItCsvRowResult> rowResults = result.getRowResults() == null ? null : result.getRowResults().stream().filter(row -> !row.isMatched()).collect(Collectors.toList());

            RecItCsvConfiguration.FileConfiguration fileConfiguration = result.getFileConfiguration();

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
            context.setVariable("errorMessage", result.getErrorMessage());

            StringWriter stringWriter = new StringWriter();
            templateEngine.process("file", context, stringWriter);

            Path resultFile = tmp.resolve(UUID.randomUUID().toString() + ".html");
            Files.deleteIfExists(resultFile);
            Files.createFile(resultFile);

            Files.write(resultFile, stringWriter.toString().getBytes(), StandardOpenOption.APPEND);

            resultFileNames.put(filePath, resultFile.getFileName().toString());
        }

        Context context = new Context();
        context.setVariable("expectedDir", expectedDir);
        context.setVariable("actualDir", actualDir);
        context.setVariable("numericFieldDiffToleranceActual", numericFieldDiffToleranceActual);
        context.setVariable("numericFieldDiffTolerancePercent", numericFieldDiffTolerancePercent);
        context.setVariable("fileNames", resultFileNames);

        StringWriter stringWriter = new StringWriter();
        templateEngine.process("home", context, stringWriter);

        Path resultFile = tmp.resolve("home.html");
        Files.deleteIfExists(resultFile);
        Files.createFile(resultFile);

        Files.write(resultFile, stringWriter.toString().getBytes(), StandardOpenOption.APPEND);
    }
}