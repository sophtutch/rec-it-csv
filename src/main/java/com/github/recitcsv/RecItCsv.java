package com.github.recitcsv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecItCsv {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            Path configuration = Paths.get(args[0]);
            if (Files.exists(configuration) && Files.isReadable(configuration)) {
                new RecItCsv().reconcile(configuration);
            } else {
                throw new Exception("The supplied configuration file either does not exist or cannot be read.");
            }
        } else {
            throw new Exception("Path to configuration file must be passed as an argument, i.e. 'java " + RecItCsv.class.getName() + " /home/user/rec-configuration.yaml'");
        }
    }

    public void reconcile(Path configurationFilePath) throws IOException {
        RecItCsvConfiguration configuration = new RecItCsvConfigurationReader().read(configurationFilePath);
        Path outputDir = configuration.getOutputDir();

        RecItCsvResult result = new RecItCsvReconcilier(configuration).reconcile();
        new RecItCsvResultHtmlRenderer(configuration).render(outputDir, result);
    }
}