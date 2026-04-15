package com.modeai.core.service;

import java.util.List;

public interface TextSplitterService {
    List<String> splitText(String text, int chunkSize, int chunkOverlap);
    List<String> splitText(String text);
}
