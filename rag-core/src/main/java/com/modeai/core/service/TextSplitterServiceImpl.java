package com.modeai.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TextSplitterServiceImpl implements TextSplitterService {

    @Value("${rag.chunk-size:512}")
    private int defaultChunkSize;

    @Value("${rag.chunk-overlap:64}")
    private int defaultChunkOverlap;

    @Override
    public List<String> splitText(String text) {
        return splitText(text, defaultChunkSize, defaultChunkOverlap);
    }

    @Override
    public List<String> splitText(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        // Normalize whitespace
        text = text.replaceAll("\\s+", " ").trim();

        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        // Split by paragraphs first, then by sentences if needed
        String[] paragraphs = text.split("\\n\\n+");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            if (currentChunk.length() + paragraph.length() + 1 > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                // Keep overlap
                String overlapText = getOverlapText(currentChunk.toString(), chunkOverlap);
                currentChunk = new StringBuilder(overlapText);
            }

            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(paragraph);
        }

        // Add remaining text
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        // Handle chunks that are still too long (split by sentences)
        List<String> finalChunks = new ArrayList<>();
        for (String chunk : chunks) {
            if (chunk.length() > chunkSize * 1.5) {
                finalChunks.addAll(splitBySentence(chunk, chunkSize, chunkOverlap));
            } else {
                finalChunks.add(chunk);
            }
        }

        log.info("Text split into {} chunks (chunkSize={}, overlap={})", finalChunks.size(), chunkSize, chunkOverlap);
        return finalChunks;
    }

    private String getOverlapText(String text, int overlapSize) {
        if (text.length() <= overlapSize) {
            return text;
        }
        // Find a good break point near the overlap size
        int breakPoint = text.lastIndexOf(' ', overlapSize);
        if (breakPoint <= 0) {
            breakPoint = overlapSize;
        }
        return text.substring(breakPoint).trim();
    }

    private List<String> splitBySentence(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[。！？.!?])");

        StringBuilder current = new StringBuilder();
        for (String sentence : sentences) {
            if (current.length() + sentence.length() > chunkSize && current.length() > 0) {
                chunks.add(current.toString().trim());
                String overlap = getOverlapText(current.toString(), chunkOverlap);
                current = new StringBuilder(overlap);
            }
            current.append(sentence);
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }
}
