package org.example.ui;

import org.example.data.SearchHistoryStore;
import org.example.ui.commons.CustomPanel;

import javax.swing.*;
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
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "검색 히스토리", Dialog.ModalityType.MODELESS);
        dialog.setSize(1180, 520);
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

        JTable historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        historyTable.setRowHeight(24);
        historyTable.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(60);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        historyTable.getColumnModel().getColumn(1).setMinWidth(150);

        for (int i = 2; i < historyTable.getColumnModel().getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(150);
            historyTable.getColumnModel().getColumn(i).setMinWidth(120);
        }

        CustomPanel topPanel = CustomPanel.of(new BorderLayout()).style(s -> s.padding(8, 8, 8, 8));
        topPanel.setBackground(Color.WHITE);
        JButton reSearchButton = new JButton("다시 검색");
        reSearchButton.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        reSearchButton.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "다시 검색할 히스토리를 선택해주세요.", "안내", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirmed = JOptionPane.showConfirmDialog(
                    dialog,
                    "다시 검색하시겠습니까?",
                    "확인",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirmed != JOptionPane.OK_OPTION) {
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

        JScrollPane scrollPane = new JScrollPane(historyTable);

        JPanel root = new JPanel(new BorderLayout());
        root.add(topPanel, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);

        dialog.setContentPane(root);
        dialog.setVisible(true);
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
