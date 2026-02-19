package org.example.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonCacheMap {
    private static final Object LOCK = new Object();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static volatile boolean initialized = false;
    private static final ConcurrentHashMap<String, Object> CACHE_MAP = new ConcurrentHashMap<>();

    private JsonCacheMap() {
    }

    public static void initializeOnce() {
        if (initialized) {
            return;
        }

        synchronized (LOCK) {
            if (initialized) {
                return;
            }

            Map<String, Object> data = loadDictionaryData();
            CACHE_MAP.putAll(data);
            CACHE_MAP.put("initializedAt", System.currentTimeMillis());
            initialized = true;
        }
    }

    public static Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(CACHE_MAP));
    }

    public static String notice() {
        Object notice = CACHE_MAP.get("notice");
        return notice == null ? "" : notice.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> entries() {
        Object value = CACHE_MAP.get("entries");
        if (value instanceof List) {
            return (List<Map<String, String>>) value;
        }
        return List.of();
    }

    private static Map<String, Object> loadDictionaryData() {
        try {
            String json = readJson();
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("data.json 로딩 실패", e);
        }
    }

    private static String readJson() throws IOException {
        Path fileSystemPath = Paths.get("data", "data.json").toAbsolutePath().normalize();
        if (Files.exists(fileSystemPath)) {
            return decodeJsonBytes(Files.readAllBytes(fileSystemPath));
        }

        InputStream classpathStream = JsonCacheMap.class.getResourceAsStream("/data.json");
        if (classpathStream != null) {
            try (InputStream in = classpathStream) {
                return decodeJsonBytes(in.readAllBytes());
            }
        }

        throw new IOException("./data/data.json 또는 /data.json 파일을 찾을 수 없습니다.");
    }

    private static String decodeJsonBytes(byte[] data) throws IOException {
        try {
            return strictDecode(data, StandardCharsets.UTF_8);
        } catch (CharacterCodingException utf8Error) {
            Charset ms949 = Charset.forName("MS949");
            return strictDecode(data, ms949);
        }
    }

    private static String strictDecode(byte[] data, Charset charset) throws CharacterCodingException {
        CharsetDecoder decoder = charset
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        CharBuffer decoded = decoder.decode(ByteBuffer.wrap(data));
        return decoded.toString();
    }
}
