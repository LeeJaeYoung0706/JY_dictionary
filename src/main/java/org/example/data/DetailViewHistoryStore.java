package org.example.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DetailViewHistoryStore {
    private static final Path HISTORY_PATH = Paths.get("data", "detail-view-history.json");
    private static final int MAX_HISTORY = 200;
    private static final int RETENTION_DAYS = 7;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void append(Map<String, String> details) {
        try {
            List<DetailViewHistoryEntry> items = loadAll();
            items = pruneExpired(items);

            DetailViewHistoryEntry entry = new DetailViewHistoryEntry();
            entry.viewedAt = DATE_TIME_FORMATTER.format(LocalDateTime.now());
            entry.details = new LinkedHashMap<>(details);

            items.add(0, entry);
            if (items.size() > MAX_HISTORY) {
                items = new ArrayList<>(items.subList(0, MAX_HISTORY));
            }

            writeAll(items);
        } catch (IOException e) {
            throw new RuntimeException("상세보기 이력 저장 실패", e);
        }
    }

    public List<DetailViewHistoryEntry> loadAll() {
        if (!Files.exists(HISTORY_PATH)) {
            return new ArrayList<>();
        }

        try {
            List<DetailViewHistoryEntry> items = OBJECT_MAPPER.readValue(Files.readAllBytes(HISTORY_PATH), new TypeReference<>() {});
            List<DetailViewHistoryEntry> filtered = pruneExpired(items);
            if (filtered.size() != items.size()) {
                writeAll(filtered);
            }
            return filtered;
        } catch (IOException e) {
            throw new RuntimeException("상세보기 이력 로드 실패", e);
        }
    }

    private List<DetailViewHistoryEntry> pruneExpired(List<DetailViewHistoryEntry> items) {
        LocalDate cutoff = LocalDate.now().minusDays(RETENTION_DAYS);
        List<DetailViewHistoryEntry> filtered = new ArrayList<>();

        for (DetailViewHistoryEntry item : items) {
            if (item == null || item.viewedAt == null || item.viewedAt.isBlank()) {
                continue;
            }

            try {
                LocalDate viewedDate = LocalDateTime.parse(item.viewedAt, DATE_TIME_FORMATTER).toLocalDate();
                if (!viewedDate.isBefore(cutoff)) {
                    filtered.add(item);
                }
            } catch (Exception ignore) {
                // invalid format entries are dropped.
            }
        }

        return filtered;
    }

    private void writeAll(List<DetailViewHistoryEntry> items) throws IOException {
        Files.createDirectories(HISTORY_PATH.getParent());
        OBJECT_MAPPER.writeValue(HISTORY_PATH.toFile(), items);
    }

    public static class DetailViewHistoryEntry {
        public String viewedAt;
        public Map<String, String> details;
    }
}
