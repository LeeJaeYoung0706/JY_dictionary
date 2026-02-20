package org.example.ui;

import org.example.data.DetailViewHistoryStore;
import org.example.ui.commons.CustomDialog;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.CustomScrollPane;
import org.example.ui.commons.CustomTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class DetailHistoryFrame {
    private final List<DetailViewHistoryStore.DetailViewHistoryEntry> items;

    public DetailHistoryFrame(List<DetailViewHistoryStore.DetailViewHistoryEntry> items) {
        this.items = items == null ? List.of() : items;
    }

    public void open(Component parent) {
        CustomDialog dialog = CustomDialog.of(SwingUtilities.getWindowAncestor(parent), "상세보기 이력")
                .style(s -> s.modalityType(Dialog.ModalityType.MODELESS).size(1180, 520));
        dialog.setLocationRelativeTo(parent);

        List<String> detailColumns = collectDetailColumns();
        List<String> allColumns = new ArrayList<>();
        allColumns.add("No");
        allColumns.add("조회 시간");
        allColumns.addAll(detailColumns);

        Object[][] rowData = new Object[items.size()][allColumns.size()];
        for (int i = 0; i < items.size(); i++) {
            DetailViewHistoryStore.DetailViewHistoryEntry entry = items.get(i);
            rowData[i][0] = i + 1;
            rowData[i][1] = entry == null || entry.viewedAt == null ? "" : entry.viewedAt;

            Map<String, String> details = entry == null ? null : entry.details;
            for (int colIndex = 0; colIndex < detailColumns.size(); colIndex++) {
                String key = detailColumns.get(colIndex);
                rowData[i][colIndex + 2] = details == null ? "" : details.getOrDefault(key, "");
            }
        }

        DefaultTableModel tableModel = new DefaultTableModel(rowData, allColumns.toArray()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        CustomTable historyTable = CustomTable.of(tableModel);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        historyTable.setFillsViewportHeight(true);
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2 || !SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }

                int row = historyTable.rowAtPoint(e.getPoint());
                if (row < 0) {
                    return;
                }

                new DetailFrame(tableModel, row).open();
            }
        });

        CustomScrollPane scrollPane = CustomScrollPane.of(historyTable);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
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
        int viewedAtColumnWidth = Math.max(160, (int) Math.floor(viewportWidth * 0.18));
        int remainingWidth = Math.max(0, viewportWidth - numberColumnWidth - viewedAtColumnWidth);
        int detailColumnCount = Math.max(0, columnCount - 2);
        int eachDetailWidth = detailColumnCount == 0 ? 0 : Math.max(180, (int) Math.floor(remainingWidth / (double) detailColumnCount));

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(numberColumnWidth);
        historyTable.getColumnModel().getColumn(0).setMinWidth(50);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(120);

        if (columnCount > 1) {
            historyTable.getColumnModel().getColumn(1).setPreferredWidth(viewedAtColumnWidth);
            historyTable.getColumnModel().getColumn(1).setMinWidth(150);
            historyTable.getColumnModel().getColumn(1).setMaxWidth(280);
        }

        for (int i = 2; i < columnCount; i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(eachDetailWidth);
            historyTable.getColumnModel().getColumn(i).setMinWidth(120);
        }
    }

    private List<String> collectDetailColumns() {
        LinkedHashSet<String> keys = new LinkedHashSet<>();

        for (DetailViewHistoryStore.DetailViewHistoryEntry item : items) {
            if (item == null || item.details == null || item.details.isEmpty()) {
                continue;
            }
            keys.addAll(item.details.keySet());
        }

        return new ArrayList<>(keys);
    }
}
