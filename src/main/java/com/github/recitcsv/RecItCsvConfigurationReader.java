package com.github.recitcsv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;

class RecItCsvConfigurationReader {
    RecItCsvConfiguration read(Path configurationFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(configurationFilePath.toFile(), RecItCsvConfiguration.class);
    }
}