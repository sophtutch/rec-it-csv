package com.github.sophtutch.recitcsv;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RecItCsvTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void run() throws Exception {
        Path configurationFilePath = Paths.get(ClassLoader.getSystemResource("reconciliation.yaml").toURI());
        RecItCsvConfiguration configuration = new RecItCsvConfigurationReader().read(configurationFilePath);

        Path outputDir = Paths.get(tmp.getRoot().getAbsolutePath());

        RecItCsvResult result = new RecItCsvReconcilier(configuration).reconcile();
        List<Path> resultFiles = new RecItCsvResultHtmlRenderer(configuration).render(outputDir, result);
        assertEquals(7, resultFiles.size());
    }
}