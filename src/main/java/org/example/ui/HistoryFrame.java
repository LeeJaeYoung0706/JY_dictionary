package org.example.ui;

import org.example.data.SearchHistoryStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryFrame {
    private final List<SearchHistoryStore.SearchHistoryEntry> items;

    public HistoryFrame(List<SearchHistoryStore.SearchHistoryEntry> items) {
        this.items = items;
    }

    public void open(Component parent) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "검색 히스토리", Dialog.ModalityType.MODELESS);
        dialog.setSize(760, 420);
        dialog.setLocationRelativeTo(parent);

        String[] columns = {"No", "검색 시간", "검색 조건"};
        Object[][] rowData = new Object[items.size()][columns.length];

        for (int i = 0; i < items.size(); i++) {
            SearchHistoryStore.SearchHistoryEntry entry = items.get(i);
            rowData[i][0] = i + 1;
            rowData[i][1] = entry == null || entry.searchedAt == null ? "" : entry.searchedAt;
            rowData[i][2] = formatHistoryConditions(entry);
        }

        DefaultTableModel tableModel = new DefaultTableModel(rowData, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        historyTable.setRowHeight(24);
        historyTable.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(60);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        historyTable.getColumnModel().getColumn(1).setMinWidth(150);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(520);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        dialog.setContentPane(scrollPane);
        dialog.setVisible(true);
    }

    private String formatHistoryConditions(SearchHistoryStore.SearchHistoryEntry entry) {
        if (entry == null || entry.conditions == null || entry.conditions.isEmpty()) {
            return "조건 없음";
        }

        return entry.conditions.entrySet().stream()
                .map(condition -> condition.getKey() + ": " + condition.getValue())
                .collect(Collectors.joining(" | "));
    }
}
