package org.example.ui;

import org.example.data.SearchHistoryStore;
import org.example.data.ViewContainer;
import org.example.ui.commons.CustomButton;
import org.example.ui.commons.CustomLabel;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.CustomSelectBox;
import org.example.ui.commons.CustomTextField;
import org.example.ui.commons.UiSizePreset;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.function.Supplier;

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
    private final SearchHistoryStore historyStore = new SearchHistoryStore();
    private final List<SearchInputBinding> searchInputBindings = new ArrayList<>();
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
        searchInputBindings.clear();

        CustomPanel row1 = createHeaderRow(12);
        row1.add(CustomLabel.of(APP_TITLE).style(s -> s.font(new Font("Malgun Gothic", Font.BOLD, 20))));
        row1.add(CustomLabel.of("공지사항: " + container.notice()).style(s -> s.font(new Font("Malgun Gothic", Font.PLAIN, 13))));
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
                .style(s -> s.size(SEARCH_BUTTON_WIDTH, COMPONENT_HEIGHT)
                        .font(new Font("Malgun Gothic", Font.BOLD, 17))
                        .background(new Color(189, 189, 189))
                        .foreground(Color.BLACK));
        searchButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        searchButton.addActionListener(e -> saveCurrentSearchHistory(header));

        CustomButton historyButton = CustomButton.of("히스토리")
                .style(s -> s.size(SEARCH_BUTTON_WIDTH, COMPONENT_HEIGHT)
                        .font(new Font("Malgun Gothic", Font.BOLD, 17))
                        .background(new Color(189, 189, 189))
                        .foreground(Color.BLACK));
        historyButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        historyButton.addActionListener(e -> openHistoryDialog(header));

        actionRow.add(searchButton);
        actionRow.add(historyButton);
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
        int componentWidth = (int) Math.floor(width * 0.98);
        wrapper.setPreferredSize(new Dimension(width, FILTER_HEIGHT));
        wrapper.add(CustomLabel.of(field.label));

        if (field.freeText) {
            CustomTextField meaningField = CustomTextField.of().style(s -> s.columns(0).size(componentWidth, COMPONENT_HEIGHT));
            searchInputBindings.add(new SearchInputBinding(field, meaningField::getText));
            wrapper.add(meaningField);
        } else {
            List<String> options = new ArrayList<>();
            options.add("전체");
            options.addAll(field.options);
            CustomSelectBox selectBox = CustomSelectBox.of(options).style(s -> s.size(componentWidth, COMPONENT_HEIGHT));
            searchInputBindings.add(new SearchInputBinding(field, () -> {
                Object selected = selectBox.getSelectedItem();
                return selected == null ? "" : String.valueOf(selected);
            }));
            wrapper.add(selectBox);
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

    private void saveCurrentSearchHistory(Component parent) {
        try {
            Map<String, String> conditions = new LinkedHashMap<>();
            for (SearchInputBinding binding : searchInputBindings) {
                String value = binding.valueSupplier.get();
                if (value == null || value.isBlank() || "전체".equals(value)) {
                    continue;
                }
                conditions.put(binding.field.label, value.trim());
            }
            historyStore.append(conditions);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openHistoryDialog(Component parent) {
        List<SearchHistoryStore.SearchHistoryEntry> items;
        try {
            items = historyStore.loadAll();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "검색 히스토리", Dialog.ModalityType.MODELESS);
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(parent);

        JTextArea historyText = new JTextArea();
        historyText.setEditable(false);
        historyText.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        historyText.setText(formatHistory(items));

        dialog.setContentPane(new JScrollPane(historyText));
        dialog.setVisible(true);
    }

    private String formatHistory(List<SearchHistoryStore.SearchHistoryEntry> items) {
        if (items.isEmpty()) {
            return "저장된 검색 히스토리가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            SearchHistoryStore.SearchHistoryEntry entry = items.get(i);
            sb.append(i + 1).append(". ").append(entry.searchedAt).append('\n');

            if (entry.conditions == null || entry.conditions.isEmpty()) {
                sb.append("   - 조건 없음").append('\n');
            } else {
                for (Map.Entry<String, String> condition : entry.conditions.entrySet()) {
                    sb.append("   - ").append(condition.getKey()).append(": ").append(condition.getValue()).append('\n');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String normalizeBaseKey(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.startsWith("korean")) {
            return key.substring(6);
        }
        return key;
    }

    private static final class SearchInputBinding {
        final SearchField field;
        final Supplier<String> valueSupplier;

        SearchInputBinding(SearchField field, Supplier<String> valueSupplier) {
            this.field = field;
            this.valueSupplier = valueSupplier;
        }
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