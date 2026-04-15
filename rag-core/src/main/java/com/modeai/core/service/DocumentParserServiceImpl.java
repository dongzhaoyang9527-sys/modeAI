package com.modeai.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class DocumentParserServiceImpl implements DocumentParserService {

    @Override
    public String parsePdf(String filePath) throws Exception {
        log.info("Parsing PDF document: {}", filePath);
        try (PDDocument document = PDDocument.load(new FileInputStream(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("PDF parsed, length: {}", text.length());
            return text;
        }
    }

    @Override
    public String parseWord(String filePath) throws Exception {
        log.info("Parsing Word document: {}", filePath);
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            // Extract paragraphs
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    content.append(text).append("\n");
                }
            }

            // Extract tables
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    StringBuilder rowContent = new StringBuilder();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        rowContent.append(cell.getText().trim()).append(" | ");
                    }
                    content.append(rowContent.toString().trim()).append("\n");
                }
                content.append("\n");
            }

            log.info("Word parsed, length: {}", content.length());
            return content.toString();
        }
    }

    @Override
    public String parseMarkdown(String filePath) throws Exception {
        log.info("Parsing Markdown document: {}", filePath);
        String text = Files.readString(Path.of(filePath));
        // Remove markdown syntax for plain text extraction
        text = text.replaceAll("```[\\s\\S]*?```", "")
                   .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                   .replaceAll("\\[.*?\\]\\(.*?\\)", "")
                   .replaceAll("#{1,6}\\s*", "")
                   .replaceAll("[*_]{1,2}", "")
                   .replaceAll(">\\s*", "")
                   .replaceAll("-\\s+", "- ")
                   .trim();
        log.info("Markdown parsed, length: {}", text.length());
        return text;
    }

    @Override
    public String parseDocument(String filePath, String fileType) throws Exception {
        String normalizedType = fileType.toLowerCase();
        return switch (normalizedType) {
            case "pdf" -> parsePdf(filePath);
            case "docx", "doc" -> parseWord(filePath);
            case "md", "markdown" -> parseMarkdown(filePath);
            case "txt" -> Files.readString(Path.of(filePath));
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }
}
