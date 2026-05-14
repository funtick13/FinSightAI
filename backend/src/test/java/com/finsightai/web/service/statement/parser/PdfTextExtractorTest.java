package com.finsightai.web.service.statement.parser;

import com.finsightai.web.exception.StatementParsingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfTextExtractorTest {

    private final PdfTextExtractor extractor = new PdfTextExtractor();

    @TempDir
    Path tempDir;

    @Test
    void extractThrowsWhenFileDoesNotExist() {
        Path missingFile = tempDir.resolve("missing.pdf");

        assertThrows(StatementParsingException.class, () -> extractor.extract(missingFile));
    }
}
