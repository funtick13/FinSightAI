package com.finsightai.web.service.statement.parser;

import com.finsightai.web.exception.StatementParsingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PdfTextExtractor {

    public String extract(Path filePath) {
        if (filePath == null) {
            throw new StatementParsingException("Путь к PDF-файлу не указан");
        }

        if (!Files.exists(filePath)) {
            throw new StatementParsingException("Файл выписки не найден");
        }

        if (!Files.isReadable(filePath)) {
            throw new StatementParsingException("PDF-файл выписки недоступен для чтения");
        }

        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            String text = new PDFTextStripper().getText(document);

            if (text == null || text.isBlank()) {
                throw new StatementParsingException("Не удалось извлечь текст из PDF-выписки");
            }

            return text;
        } catch (StatementParsingException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new StatementParsingException("Невозможно прочитать PDF-выписку", exception);
        }
    }
}
