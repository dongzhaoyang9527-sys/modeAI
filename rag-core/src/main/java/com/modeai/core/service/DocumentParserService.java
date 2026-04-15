package com.modeai.core.service;

import java.util.List;

public interface DocumentParserService {
    String parsePdf(String filePath) throws Exception;
    String parseWord(String filePath) throws Exception;
    String parseMarkdown(String filePath) throws Exception;
    String parseDocument(String filePath, String fileType) throws Exception;
}
