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
    private static final int ROW_GAP = 20;
    // 헤더 좌우 패딩 (build()와 동일 값 유지)
    private static final int HEADER_PADDING_X = 16;
    private static final int HEADER_BORDER_WIDTH = 5;
    // 아이템 패널 높이
    private static final int FILTER_HEIGHT = 56;
    // 버튼 크기
    private static final int SEARCH_BUTTON_WIDTH = 100;
    private static final int COMPONENT_HEIGHT = 28;

    private final ViewContainer container;
    private final UiSizePreset sizePreset;
    // 앱 전체 폭 기준으로 계산된 아이템 패널 폭
    private final int rowSize;

    public HeaderFrame(ViewContainer container, UiSizePreset sizePreset) {
        this.container = container;
        this.sizePreset = sizePreset;

        int horizontalInsets = (HEADER_PADDING_X * 2) + (HEADER_BORDER_WIDTH * 2);
        int availableWidth = sizePreset.appWidth() - horizontalInsets;
        int totalGap = ROW_GAP * (ROW_ITEM_COUNT - 1);
        this.rowSize = Math.max(1, (int) Math.floor((availableWidth - totalGap) / (double) ROW_ITEM_COUNT));
    }

    public CustomPanel build() {
        CustomPanel header = CustomPanel.flexColumn(10)
                .style(s -> s.padding(14, HEADER_PADDING_X, 14, HEADER_PADDING_X).border(true).borderColor(new Color(224, 224, 224)));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(sizePreset.appWidth(), sizePreset.headerHeight()));

        CustomPanel row1 = createHeaderRow(12);
        row1.add(CustomLabel.of(APP_TITLE).style(s -> s.font(new Font("Malgun Gothic", Font.BOLD, 20))));
        row1.add(CustomLabel.of("공지: " + container.notice()).style(s -> s.font(new Font("Malgun Gothic", Font.PLAIN, 13))));
        header.add(row1);

        List<SearchField> fields = buildSearchFields();
        CustomPanel row = createHeaderRow(ROW_GAP);
        int remainSlots = ROW_ITEM_COUNT;

        for (SearchField field : fields) {
            int span = field.slotSpan();

            if (span > remainSlots) {
                header.add(row);
                row = createHeaderRow(ROW_GAP);
                remainSlots = ROW_ITEM_COUNT;
            }

            row.add(createFilterComponent(field));
            remainSlots -= span;

            if (remainSlots == 0) {
                header.add(row);
                row = createHeaderRow(ROW_GAP);
                remainSlots = ROW_ITEM_COUNT;
            }
        }

        if (row.getComponentCount() > 0) {
            header.add(row);
        }

        CustomPanel actionRow = createHeaderRow(ROW_GAP);
        CustomButton searchButton = CustomButton.of("검색")
                .style(s -> s.size(SEARCH_BUTTON_WIDTH, COMPONENT_HEIGHT));
        actionRow.add(searchButton);
        header.add(actionRow);

        int requiredHeaderHeight = header.getPreferredSize().height;
        int finalHeaderHeight = Math.max(sizePreset.headerHeight(), requiredHeaderHeight);
        header.setPreferredSize(new Dimension(sizePreset.appWidth(), finalHeaderHeight));

        return header;
    }


    private CustomPanel createHeaderRow(int gap) {
        CustomPanel row = CustomPanel.flexRow(gap).style(s -> s.padding(0));
        row.setBackground(Color.WHITE);
        return row;
    }

    private JComponent createFilterComponent(SearchField field) {
        CustomPanel wrapper = CustomPanel.flexColumn(4).style(s -> s.padding(0));
        wrapper.setBackground(Color.WHITE);
        int span = field.slotSpan();
        int width = rowSize * span + (ROW_GAP * (span - 1));

        wrapper.setPreferredSize(new Dimension(width, FILTER_HEIGHT));
        wrapper.add(CustomLabel.of(field.label));

        if (field.freeText) {
            wrapper.add(CustomTextField.of().style(s -> s.columns(0).size(width, COMPONENT_HEIGHT)));
        } else {
            List<String> options = new ArrayList<>();
            options.add("전체");
            options.addAll(field.options);
            wrapper.add(CustomSelectBox.of(options).style(s -> s.size(width, COMPONENT_HEIGHT)));
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
