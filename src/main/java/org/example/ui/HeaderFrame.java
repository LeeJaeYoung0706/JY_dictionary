package org.example.ui;

import org.example.data.ViewContainer;
import org.example.ui.commons.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class HeaderFrame {
    private static final String APP_TITLE = "용어사전";
    // 행에 포함되는 컬럼 ( 아이템 ) 갯수
    private static final int ROW_ITEM_COUNT = 4;
    // 아이템 간 간격
    private static final int ROW_GAP = 10;
    // 아이템 패널 사이즈
    private static final int ROW_SIZE = 380;
    // 아이템 패널 높이
    private static final int FILTER_HEIGHT = 56;
    // 버튼 크기
    private static final int SEARCH_BUTTON_WIDTH = 100;
    private static final int SEARCH_BUTTON_HEIGHT = 28;


    private final ViewContainer container;
    private final UiSizePreset sizePreset;

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
        CustomPanel row = CustomPanel.flexRow(ROW_GAP).style(s -> s.padding(0));

        int remainSlots = ROW_ITEM_COUNT;

        for (SearchField field : fields) {
            int span = field.slotSpan();

            if (span > remainSlots) {
                header.add(row);
                row = CustomPanel.flexRow(ROW_GAP).style(s -> s.padding(0));
                remainSlots = ROW_ITEM_COUNT;
            }

            row.add(createFilterComponent(field));
            remainSlots -= span;

            if (remainSlots == 0) {
                header.add(row);
                row = CustomPanel.flexRow(ROW_GAP).style(s -> s.padding(0));
                remainSlots = ROW_ITEM_COUNT;
            }
        }

        if (row.getComponentCount() > 0) {
            header.add(row);
        }

        CustomPanel actionRow = CustomPanel.flexRow(ROW_GAP).style(s -> s.padding(0));
        CustomButton searchButton = CustomButton.of("검색")
                .style(s -> s.size(SEARCH_BUTTON_WIDTH, SEARCH_BUTTON_HEIGHT));
        actionRow.add(searchButton);
        header.add(actionRow);

        return header;
    }

    private JComponent createFilterComponent(SearchField field) {
        CustomPanel wrapper = CustomPanel.flexColumn(4).style(s -> s.padding(0));
        wrapper.setBackground(Color.WHITE);
        int span = field.slotSpan();
        int width = ROW_SIZE * span + (ROW_GAP * (span - 1));
        int searchComponentWidth = (int) Math.floor(width * 0.95);
        wrapper.setPreferredSize(new Dimension(width, FILTER_HEIGHT));
        wrapper.add(CustomLabel.of(field.label));

        if (field.freeText) {
            CustomTextField meaningField = CustomTextField.of().style(s -> s.columns(span == 2 ? 42 : 18));
            meaningField.setPreferredSize(new Dimension(searchComponentWidth, 28));
            wrapper.add(meaningField);
        } else {
            List<String> options = new ArrayList<>();
            options.add("전체");
            options.addAll(field.options);
            wrapper.add(CustomSelectBox.of(options).style(s -> s.size(searchComponentWidth, 28)));
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

        return grouped.values().stream()
                .sorted(Comparator.comparing((SearchField field) -> field.freeText))
                .collect(Collectors.toList());
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

        int slotSpan() {
            return freeText ? 2 : 1;
        }
    }
}
