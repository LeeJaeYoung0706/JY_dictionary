package org.example.ui;
import org.example.data.SearchHistoryStore;
import org.example.data.ViewContainer;
import org.example.ui.commons.CustomButton;
import org.example.ui.commons.CustomLabel;
import org.example.ui.commons.CustomMessageBox;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.CustomSelectBox;
import org.example.ui.commons.CustomTextField;
import org.example.ui.commons.UiSizePreset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HeaderFrame {
    private static final String APP_TITLE = "용어사전";
    private static final int ROW_ITEM_COUNT = 4;
    private static final int ROW_GAP = 20;
    private static final int HEADER_PADDING_X = 16;
    private static final int HEADER_BORDER_WIDTH = 1;
    private static final int FILTER_HEIGHT = 56;
    private static final int SEARCH_BUTTON_WIDTH = 100;
    private static final int COMPONENT_HEIGHT = 28;

    private final ViewContainer container;
    private final UiSizePreset sizePreset;
    private final Consumer<Map<String, String>> onSearch;
    private final SearchHistoryStore historyStore = new SearchHistoryStore();
    private final List<SearchInputBinding> searchInputBindings = new ArrayList<>();
    private final int rowSize;

    public HeaderFrame(ViewContainer container, UiSizePreset sizePreset, Consumer<Map<String, String>> onSearch) {
        this.container = container;
        this.sizePreset = sizePreset;
        this.onSearch = onSearch;

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
                .style(s -> s.size(SEARCH_BUTTON_WIDTH, COMPONENT_HEIGHT)
                        .font(new Font("Malgun Gothic", Font.BOLD, 17))
                        .background(new Color(189, 189, 189))
                        .foreground(Color.BLACK));
        searchButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        searchButton.setFocusable(false);
        searchButton.setRequestFocusEnabled(false);
        searchButton.addActionListener(e -> {
            Map<String, String> keyConditions = collectSearchConditionsByKey();
            Map<String, String> labelConditions = collectSearchConditionsByLabel();
            if (onSearch != null) {
                onSearch.accept(keyConditions);
            }
            saveCurrentSearchHistory(header, labelConditions);
        });

        CustomButton historyButton = CustomButton.of("검색 히스토리")
                .style(s -> s.size(SEARCH_BUTTON_WIDTH + 20, COMPONENT_HEIGHT)
                        .font(new Font("Malgun Gothic", Font.BOLD, 17))
                        .background(new Color(189, 189, 189))
                        .foreground(Color.BLACK));
        historyButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        historyButton.setFocusable(false);
        historyButton.setRequestFocusEnabled(false);
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
        int selectBoxWidth = (int) Math.floor(width * 0.95);
        wrapper.setPreferredSize(new Dimension(width, FILTER_HEIGHT));
        wrapper.add(CustomLabel.of(field.label));

        if (field.freeText) {
            CustomTextField meaningField = CustomTextField.of().style(s -> s.columns(0).size(selectBoxWidth, COMPONENT_HEIGHT));
            searchInputBindings.add(new SearchInputBinding(field, meaningField::getText, meaningField::setText));
            wrapper.add(meaningField);
        } else {
            List<String> options = new ArrayList<>();
            options.add("전체");
            options.addAll(field.options);
            CustomSelectBox selectBox = CustomSelectBox.of(options).style(s -> s.size(selectBoxWidth, COMPONENT_HEIGHT));
            searchInputBindings.add(new SearchInputBinding(field, () -> {
                Object selected = selectBox.getSelectedItem();
                return selected == null ? "" : String.valueOf(selected);
            }, value -> {
                String normalizedValue = (value == null || value.isBlank()) ? "전체" : value.trim();
                ComboBoxModel<String> model = selectBox.getModel();
                boolean exists = false;
                for (int i = 0; i < model.getSize(); i++) {
                    if (normalizedValue.equals(model.getElementAt(i))) {
                        exists = true;
                        break;
                    }
                }

                if (!exists && !"전체".equals(normalizedValue)) {
                    selectBox.addItem(normalizedValue);
                }
                selectBox.setSelectedItem(normalizedValue);
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

    private Map<String, String> collectSearchConditionsByLabel() {
        Map<String, String> conditions = new LinkedHashMap<>();
        for (SearchInputBinding binding : searchInputBindings) {
            String value = binding.valueSupplier.get();
            if (value == null || value.isBlank() || "전체".equals(value)) {
                continue;
            }
            conditions.put(binding.field.label, value.trim());
        }
        return conditions;
    }

    private Map<String, String> collectSearchConditionsByKey() {
        Map<String, String> conditions = new LinkedHashMap<>();
        for (SearchInputBinding binding : searchInputBindings) {
            String value = binding.valueSupplier.get();
            if (value == null || value.isBlank() || "전체".equals(value)) {
                continue;
            }
            String normalizedValue = value.trim();
            for (String key : binding.field.keys) {
                conditions.put(key, normalizedValue);
            }
        }
        return conditions;
    }

    private void saveCurrentSearchHistory(Component parent, Map<String, String> conditions) {
        try {
            historyStore.append(conditions);
        } catch (RuntimeException ex) {
            CustomMessageBox.showError(parent, ex.getMessage(), "오류");
        }
    }

    private void openHistoryDialog(Component parent) {
        List<SearchHistoryStore.SearchHistoryEntry> items;
        try {
            items = historyStore.loadAll();
        } catch (RuntimeException ex) {
            CustomMessageBox.showError(parent, ex.getMessage(), "오류");
            return;
        }

        List<SearchField> searchFields = buildSearchFields();
        List<String> historyColumns = searchFields.stream().map(field -> field.label).collect(Collectors.toList());
        new HistoryFrame(items, historyColumns, selectedConditions -> applyHistorySearch(selectedConditions, searchFields)).open(parent);
    }


    private void applyHistorySearch(Map<String, String> selectedConditions, List<SearchField> searchFields) {
        if (onSearch == null || selectedConditions == null || selectedConditions.isEmpty()) {
            return;
        }

        applySearchConditionsToInputs(selectedConditions);

        Map<String, String> keyConditions = new LinkedHashMap<>();
        for (SearchField field : searchFields) {
            String selectedValue = selectedConditions.get(field.label);
            if (selectedValue == null || selectedValue.isBlank()) {
                continue;
            }
            for (String key : field.keys) {
                keyConditions.put(key, selectedValue.trim());
            }
        }

        onSearch.accept(keyConditions);
    }

    private void applySearchConditionsToInputs(Map<String, String> selectedConditions) {
        for (SearchInputBinding binding : searchInputBindings) {
            String selectedValue = selectedConditions.get(binding.field.label);
            binding.valueApplier.accept(selectedValue == null ? "" : selectedValue.trim());
        }
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
        final Consumer<String> valueApplier;

        SearchInputBinding(SearchField field, Supplier<String> valueSupplier, Consumer<String> valueApplier) {
            this.field = field;
            this.valueSupplier = valueSupplier;
            this.valueApplier = valueApplier;
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