package com.github.recitcsv;

import org.junit.Test;

import java.nio.file.Paths;

public class RecItCsvTest {

    @Test
    public void run() throws Exception {
        RecItCsv recItCsv = new RecItCsv();
        recItCsv.reconcile(Paths.get(ClassLoader.getSystemResource("reconciliation.yaml").toURI()));
    }
}