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

class RecItCsvResultHtmlRenderer {

    private final RecItCsvConfiguration configuration;

    RecItCsvResultHtmlRenderer(RecItCsvConfiguration configuration) {
        this.configuration = configuration;
    }

    void render(List<RecItCsvResult> results) throws IOException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Path tmp = Paths.get("tmp");
        if (Files.notExists(tmp)) {
            tmp = Files.createDirectory(tmp);
        }

        List<String> resultFileNames = new LinkedList<>();
        for (RecItCsvResult result : results) {
            String fileName = result.getFileConfiguration().getName();
            resultFileNames.add(fileName);

            Context context = new Context();
            context.setVariable("fileName", fileName);

            StringWriter stringWriter = new StringWriter();
            templateEngine.process("templates/file.html", context, stringWriter);

            Path resultFile = tmp.resolve(fileName + ".html");
            Files.deleteIfExists(resultFile);
            Files.createFile(resultFile);

            Files.write(resultFile, stringWriter.toString().getBytes(), StandardOpenOption.APPEND);
        }

        Context context = new Context();
        context.setVariable("configuration", configuration);
        context.setVariable("fileNames", resultFileNames);

        StringWriter stringWriter = new StringWriter();
        templateEngine.process("templates/home.html", context, stringWriter);

        Path resultFile = tmp.resolve("home.html");
        Files.deleteIfExists(resultFile);
        Files.createFile(resultFile);

        Files.write(resultFile, stringWriter.toString().getBytes(), StandardOpenOption.APPEND);
    }
}