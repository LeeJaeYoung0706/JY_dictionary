package org.example.ui;

import org.example.data.ViewContainer;
import org.example.ui.commons.CustomLabel;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.CustomSelectBox;
import org.example.ui.commons.CustomTextField;
import org.example.ui.commons.UiSizePreset;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class HeaderFrame {
    private static final String APP_TITLE = "용어사전";

    private final ViewContainer container;
    private final UiSizePreset sizePreset;
    // 검색조건 행 갯수 사이즈
    private final int SEARCH_ROW_SIZE = 4;

    public HeaderFrame(ViewContainer container, UiSizePreset sizePreset) {
        this.container = container;
        this.sizePreset = sizePreset;
    }

    public CustomPanel build() {
        CustomPanel header = CustomPanel.flexColumn(10)
                .style(s -> s.padding(14, 16, 14, 16).border(true).borderColor(new Color(224, 224, 224)));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(sizePreset.appWidth(), sizePreset.headerHeight()));

        CustomPanel row1 = CustomPanel.flexRow(12).style(s -> s.padding(0));
        row1.add(CustomLabel.of(APP_TITLE).style(s -> s.font(new Font("Malgun Gothic", Font.BOLD, 20))));
        row1.add(CustomLabel.of("공지: " + container.notice()).style(s -> s.font(new Font("Malgun Gothic", Font.PLAIN, 13))));
        header.add(row1);

        List<SearchField> fields = buildSearchFields();

        for (int i = 0; i < fields.size(); i += SEARCH_ROW_SIZE) {
            CustomPanel row = CustomPanel.flexRow(10).style(s -> s.padding(0));
            for (int j = i; j < Math.min(i + SEARCH_ROW_SIZE, fields.size()); j++) {
                row.add(createFilterComponent(fields.get(j)));
            }
            header.add(row);
        }

        return header;
    }

    private JComponent createFilterComponent(SearchField field) {
        CustomPanel wrapper = CustomPanel.flexColumn(4).style(s -> s.padding(0));
        wrapper.setPreferredSize(new Dimension(380, 56));
        wrapper.add(CustomLabel.of(field.label));

        if (field.freeText) {
            wrapper.add(CustomTextField.of().style(s -> s.columns(18)));
        } else {
            List<String> options = new ArrayList<>();
            options.add("전체");
            options.addAll(field.options);
            wrapper.add(CustomSelectBox.of(options).style(s -> s.size(250, 28)));
        }
        return wrapper;
    }

    private List<SearchField> buildSearchFields() {
        List<Map<String, String>> columns = container.columns();
        List<Map<String, String>> entries = container.entries();

        Map<String, SearchField> grouped = new LinkedHashMap<>();

        for (Map<String, String> c : columns) {
            String key = c.getOrDefault("key", "");
            if (key.isBlank()) continue;

            String normalized = normalizeBaseKey(key);
            String label = c.getOrDefault("view", normalized);

            String labelReplace = label.replace("한글", "").replace("영어", "").trim();
            if (labelReplace.isBlank()) {
                labelReplace = normalized;
            }
            String finalLabel = labelReplace;
            SearchField field = grouped.computeIfAbsent(normalized, k -> new SearchField(normalized, finalLabel));
            field.keys.add(key);
        }

        for (SearchField field : grouped.values()) {
            boolean meaningLike = field.baseKey.toLowerCase(Locale.ROOT).contains("meaning");
            field.freeText = meaningLike;

            if (!field.freeText) {
                Set<String> values = new TreeSet<>();
                for (Map<String, String> e : entries) {
                    for (String key : field.keys) {
                        String v = e.getOrDefault(key, "").trim();
                        if (!v.isBlank()) values.add(v);
                    }
                }
                field.options.addAll(values);
                field.label = field.label + " (영어/한글)";
            }
        }

        return grouped.values().stream().collect(Collectors.toList());
    }

    private String normalizeBaseKey(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.startsWith("korean")) {
            return key.substring(6);
        }
        return key;
    }

    private static final class SearchField {
        final String baseKey;
        String label;
        boolean freeText;
        final List<String> keys = new ArrayList<>();
        final List<String> options = new ArrayList<>();

        SearchField(String baseKey, String label) {
            this.baseKey = baseKey;
            this.label = label;
        }
    }
}
