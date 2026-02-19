package org.example.utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonCacheMap {
    private static final Object LOCK = new Object();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static volatile boolean initialized = false;
    private static final ConcurrentHashMap<String, Object> CACHE_MAP = new ConcurrentHashMap<>();

    private JsonCacheMap() {}

    public static void initializeOnce() {
        if (initialized) return;

        synchronized (LOCK) {
            if (initialized) return;

            Map<String, Object> data = loadDictionaryData();
            CACHE_MAP.putAll(data);
            CACHE_MAP.put("initializedAt", System.currentTimeMillis());
            initialized = true;

            // 디버그(원하면 제거)
            System.out.println("Cache initialized: title=" + appTitle() + ", entries=" + entries().size());
        }
    }

    public static String appTitle() {
        Object title = CACHE_MAP.get("appTitle");
        return title == null ? "용어사전" : title.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> entries() {
        Object value = CACHE_MAP.get("entries");
        if (value instanceof List) return (List<Map<String, String>>) value;
        return List.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Column> columns() {
        Object value = CACHE_MAP.get("columns");
        if (!(value instanceof List<?> list)) return List.of();

        List<Column> out = new ArrayList<>(list.size());
        for (Object o : list) {
            if (o instanceof Map<?, ?> m) {
                Object k = m.get("key");
                Object v = m.get("view");
                String key = k == null ? "" : k.toString();
                String view = v == null ? key : v.toString();
                if (!key.isBlank()) out.add(new Column(key, view));
            }
        }
        return out;
    }

    public static Data data() {
        return new Data(appTitle(), columns(), entries());
    }

    private static Map<String, Object> loadDictionaryData() {
        try {
            byte[] bytes = readJsonBytes();
            return OBJECT_MAPPER.readValue(bytes, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("data.json 로딩 실패", e);
        }
    }

    private static byte[] readJsonBytes() throws IOException {
        Path baseDir = resolveBaseDir();
        Path fs = baseDir.resolve("data").resolve("data.json").normalize();

        if (Files.exists(fs)) return Files.readAllBytes(fs);

        if (isStrictFs()) {
            throw new IOException("FS data.json not found: " + fs);
        }

        try (InputStream in = JsonCacheMap.class.getResourceAsStream("/data.json")) {
            if (in != null) return in.readAllBytes();
        }
        throw new IOException("classpath:/data.json not found");
    }

    private static Path resolveBaseDir() {
        try {
            Path codeSource = Paths.get(
                    JsonCacheMap.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).toAbsolutePath().normalize();

            if (Files.isRegularFile(codeSource)) return codeSource.getParent();
            return Paths.get("").toAbsolutePath().normalize();
        } catch (Exception e) {
            return Paths.get("").toAbsolutePath().normalize();
        }
    }

    public static final class Column {
        public final String key;
        public final String view;

        public Column(String key, String view) {
            this.key = key;
            this.view = view;
        }
    }

    public static final class Data {
        public final String appTitle;
        public final List<Column> columns;
        public final List<Map<String, String>> entries;

        public Data(String appTitle, List<Column> columns, List<Map<String, String>> entries) {
            this.appTitle = appTitle;
            this.columns = columns;
            this.entries = entries;
        }
    }

    private static boolean isStrictFs() {
        // 운영: -Ddictionary.strictFs=true 주면 classpath fallback 막음
        return "true".equalsIgnoreCase(System.getProperty("dictionary.strictFs", "false"));
    }
}