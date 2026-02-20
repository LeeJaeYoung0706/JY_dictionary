package org.example.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchHistoryStore {
    private static final Path HISTORY_PATH = Paths.get("data", "search-history.json");
    private static final int MAX_HISTORY = 200;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void append(Map<String, String> conditions) {
        try {
            List<SearchHistoryEntry> items = loadAll();

            SearchHistoryEntry entry = new SearchHistoryEntry();
            entry.searchedAt = DATE_TIME_FORMATTER.format(LocalDateTime.now());
            entry.conditions = new LinkedHashMap<>(conditions);

            items.add(0, entry);
            if (items.size() > MAX_HISTORY) {
                items = new ArrayList<>(items.subList(0, MAX_HISTORY));
            }

            writeAll(items);
        } catch (IOException e) {
            throw new RuntimeException("검색 히스토리 저장 실패", e);
        }
    }

    public List<SearchHistoryEntry> loadAll() {
        if (!Files.exists(HISTORY_PATH)) {
            return new ArrayList<>();
        }

        try {
            return OBJECT_MAPPER.readValue(Files.readAllBytes(HISTORY_PATH), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("검색 히스토리 로드 실패", e);
        }
    }

    private void writeAll(List<SearchHistoryEntry> items) throws IOException {
        Files.createDirectories(HISTORY_PATH.getParent());
        OBJECT_MAPPER.writeValue(HISTORY_PATH.toFile(), items);
    }

    public static class SearchHistoryEntry {
        public String searchedAt;
        public Map<String, String> conditions;
    }
}
