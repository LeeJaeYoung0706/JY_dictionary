package org.example.ui;

import org.example.data.SearchHistoryStore;
import org.example.ui.commons.CustomButton;
import org.example.ui.commons.CustomDialog;
import org.example.ui.commons.CustomMessageBox;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.CustomScrollPane;
import org.example.ui.commons.CustomTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HistoryFrame {
    private final List<SearchHistoryStore.SearchHistoryEntry> items;
    private final List<String> baseConditionColumns;
    private final Consumer<Map<String, String>> onReSearch;

    public HistoryFrame(List<SearchHistoryStore.SearchHistoryEntry> items,
                        List<String> baseConditionColumns,
                        Consumer<Map<String, String>> onReSearch) {
        this.items = items;
        this.baseConditionColumns = baseConditionColumns == null ? List.of() : baseConditionColumns;
        this.onReSearch = onReSearch;
    }

    public void open(Component parent) {
        CustomDialog dialog = CustomDialog.of(SwingUtilities.getWindowAncestor(parent), "검색 히스토리")
                .style(s -> s.modalityType(Dialog.ModalityType.MODELESS).size(1180, 520));
        dialog.setLocationRelativeTo(parent);

        List<String> conditionColumns = collectConditionColumns();
        List<String> allColumns = new ArrayList<>();
        allColumns.add("No");
        allColumns.add("검색 시간");
        allColumns.addAll(conditionColumns);

        Object[][] rowData = new Object[items.size()][allColumns.size()];
        for (int i = 0; i < items.size(); i++) {
            SearchHistoryStore.SearchHistoryEntry entry = items.get(i);
            rowData[i][0] = i + 1;
            rowData[i][1] = entry == null || entry.searchedAt == null ? "" : entry.searchedAt;

            Map<String, String> conditions = entry == null ? null : entry.conditions;
            for (int colIndex = 0; colIndex < conditionColumns.size(); colIndex++) {
                String conditionKey = conditionColumns.get(colIndex);
                String value = conditions == null ? "" : conditions.getOrDefault(conditionKey, "");
                rowData[i][colIndex + 2] = value;
            }
        }

        DefaultTableModel tableModel = new DefaultTableModel(rowData, allColumns.toArray()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        CustomTable historyTable = CustomTable.of(tableModel);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);


        CustomPanel topPanel = CustomPanel.of(new BorderLayout()).style(s -> s.padding(8, 8, 8, 8));
        topPanel.setBackground(Color.WHITE);
        CustomButton reSearchButton = CustomButton.of("다시 검색")
                .style(s -> s.font(new Font("Malgun Gothic", Font.BOLD, 13))
                        .background(new Color(189, 189, 189))
                        .foreground(Color.BLACK)
                        .size(100, 28)
                );
        reSearchButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        reSearchButton.setFocusable(false);
        reSearchButton.setRequestFocusEnabled(false);
        reSearchButton.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow < 0) {
                CustomMessageBox.showInfo(dialog, "다시 검색할 히스토리를 선택해주세요.", "안내");
                return;
            }

            boolean confirmed = CustomMessageBox.confirmOkCancel(dialog, "다시 검색하시겠습니까?", "확인");
            if (!confirmed) {
                return;
            }


            Map<String, String> selectedConditions = new LinkedHashMap<>();
            for (int i = 0; i < conditionColumns.size(); i++) {
                Object rawValue = tableModel.getValueAt(selectedRow, i + 2);
                String value = rawValue == null ? "" : String.valueOf(rawValue).trim();
                if (!value.isBlank()) {
                    selectedConditions.put(conditionColumns.get(i), value);
                }
            }

            if (onReSearch != null) {
                onReSearch.accept(selectedConditions);
            }
            dialog.dispose();
        });
        topPanel.add(reSearchButton, BorderLayout.EAST);

        CustomScrollPane scrollPane = CustomScrollPane.of(historyTable);
        historyTable.setFillsViewportHeight(true);
        applyDynamicColumnWidth(historyTable, scrollPane);
        scrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                applyDynamicColumnWidth(historyTable, scrollPane);
            }
        });

        CustomPanel tableContainer = CustomPanel.of(new BorderLayout()).style(s -> s.padding(0));
        tableContainer.setBorder(new EmptyBorder(12, 12, 12, 12));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        CustomPanel root = CustomPanel.of(new BorderLayout()).style(s -> s.padding(0));
        root.add(topPanel, BorderLayout.NORTH);
        root.add(tableContainer, BorderLayout.CENTER);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void applyDynamicColumnWidth(JTable historyTable, JScrollPane scrollPane) {
        int columnCount = historyTable.getColumnModel().getColumnCount();
        if (columnCount == 0) {
            return;
        }

        int viewportWidth = scrollPane.getViewport().getWidth();
        if (viewportWidth <= 0) {
            viewportWidth = scrollPane.getWidth();
        }
        if (viewportWidth <= 0) {
            return;
        }

        int numberColumnWidth = Math.max(50, (int) Math.floor(viewportWidth * 0.06));
        int searchedAtColumnWidth = Math.max(160, (int) Math.floor(viewportWidth * 0.18));
        int remainingWidth = Math.max(0, viewportWidth - numberColumnWidth - searchedAtColumnWidth);
        int conditionColumnCount = Math.max(0, columnCount - 2);
        int eachConditionWidth = conditionColumnCount == 0
                ? 0
                : Math.max(120, (int) Math.floor(remainingWidth / (double) conditionColumnCount));

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(numberColumnWidth);
        historyTable.getColumnModel().getColumn(0).setMinWidth(50);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(120);

        if (columnCount > 1) {
            historyTable.getColumnModel().getColumn(1).setPreferredWidth(searchedAtColumnWidth);
            historyTable.getColumnModel().getColumn(1).setMinWidth(150);
            historyTable.getColumnModel().getColumn(1).setMaxWidth(280);
        }

        for (int i = 2; i < columnCount; i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(eachConditionWidth);
            historyTable.getColumnModel().getColumn(i).setMinWidth(120);
        }
    }


    private List<String> collectConditionColumns() {
        LinkedHashSet<String> orderedKeys = new LinkedHashSet<>(baseConditionColumns);

        for (SearchHistoryStore.SearchHistoryEntry item : items) {
            if (item == null || item.conditions == null || item.conditions.isEmpty()) {
                continue;
            }
            orderedKeys.addAll(item.conditions.keySet());
        }

        return new ArrayList<>(orderedKeys);
    }
}
