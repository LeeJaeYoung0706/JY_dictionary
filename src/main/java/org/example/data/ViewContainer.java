package org.example.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ViewContainer {
    private final Map<String, Object> root;

    private ViewContainer(Map<String, Object> root) {
        this.root = root;
    }

    public static ViewContainer fromCache() {
        JsonCacheMap.initializeOnce();
        return new ViewContainer(new LinkedHashMap<>(JsonCacheMap.snapshot()));
    }
    // 공지사항
    public String notice() {
        Object value = root.get("notice");
        return value == null ? "" : value.toString();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> entries() {
        Object value = root.get("entries");
        if (value instanceof List<?>) {
            return (List<Map<String, String>>) value;
        }
        return List.of();
    }

    public Map<String, Object> raw() {
        return Collections.unmodifiableMap(root);
    }
}
