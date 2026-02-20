package org.example.ui;

import org.example.data.ViewContainer;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.UiSizePreset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BodyFrame {
    private static final int BODY_PADDING = 16;

    private final ViewContainer container;
    private final UiSizePreset sizePreset;
    private final Map<String, Integer> keyToColumnIndex = new HashMap<>();

    private JTable table;
    private TableRowSorter<TableModel> tableSorter;

    public BodyFrame(ViewContainer container, UiSizePreset sizePreset) {
        this.container = container;
        this.sizePreset = sizePreset;
    }

    public CustomPanel build() {
        CustomPanel body = CustomPanel.of(new BorderLayout())
                .style(s -> s.padding(BODY_PADDING));
        body.setBackground(Color.WHITE);

        table = createDataTable();
        tableSorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(tableSorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        int tableAreaWidth = Math.max(1, sizePreset.appWidth() - (BODY_PADDING * 2));
        int tableAreaHeight = Math.max(1, sizePreset.appHeight() - sizePreset.headerHeight() - (BODY_PADDING * 2));
        Dimension tableAreaSize = new Dimension(tableAreaWidth, tableAreaHeight);
        scrollPane.setPreferredSize(tableAreaSize);
        scrollPane.setMinimumSize(tableAreaSize);

        body.add(scrollPane, BorderLayout.CENTER);
        return body;
    }

    public void applySearchConditions(Map<String, String> conditionsByKey) {
        if (tableSorter == null || conditionsByKey == null || conditionsByKey.isEmpty()) {
            if (tableSorter != null) {
                tableSorter.setRowFilter(null);
            }
            return;
        }

        Map<String, SearchConditionGroup> activeConditionGroups = new HashMap<>();
        for (Map.Entry<String, String> condition : conditionsByKey.entrySet()) {
            String key = condition.getKey();
            Integer columnIndex = keyToColumnIndex.get(key);
            String value = condition.getValue();
            if (columnIndex == null || value == null || value.isBlank()) {
                continue;
            }

            String normalizedBaseKey = normalizeBaseKey(key);
            SearchConditionGroup conditionGroup = activeConditionGroups.computeIfAbsent(
                    normalizedBaseKey,
                    k -> new SearchConditionGroup(value.trim().toLowerCase(Locale.ROOT))
            );
            conditionGroup.columnIndexes.add(columnIndex);
        }

        if (activeConditionGroups.isEmpty()) {
            tableSorter.setRowFilter(null);
            return;
        }

        tableSorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                for (SearchConditionGroup group : activeConditionGroups.values()) {
                    boolean groupMatched = false;
                    for (Integer columnIndex : group.columnIndexes) {
                        Object rawValue = entry.getValue(columnIndex);
                        String rowValue = rawValue == null ? "" : String.valueOf(rawValue).toLowerCase(Locale.ROOT);
                        if (rowValue.contains(group.query)) {
                            groupMatched = true;
                            break;
                        }
                    }

                    if (!groupMatched) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private String normalizeBaseKey(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.startsWith("korean") && key.length() > 6) {
            return key.substring(6);
        }
        return key;
    }

    private static final class SearchConditionGroup {
        final String query;
        final List<Integer> columnIndexes = new ArrayList<>();

        SearchConditionGroup(String query) {
            this.query = query;
        }
    }

    private JTable createDataTable() {
        List<Map<String, String>> columns = container.columns();
        List<Map<String, String>> entries = container.entries();

        String[] columnNames = new String[columns.size()];
        keyToColumnIndex.clear();
        for (int i = 0; i < columns.size(); i++) {
            Map<String, String> column = columns.get(i);
            String key = column.getOrDefault("key", "");
            keyToColumnIndex.put(key, i);
            columnNames[i] = column.getOrDefault("view", key);
        }

        Object[][] rowData = new Object[entries.size()][columns.size()];
        for (int rowIndex = 0; rowIndex < entries.size(); rowIndex++) {
            Map<String, String> entry = entries.get(rowIndex);
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                String key = columns.get(colIndex).getOrDefault("key", "");
                rowData[rowIndex][colIndex] = entry.getOrDefault(key, "");
            }
        }

        DefaultTableModel model = new DefaultTableModel(rowData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable dataTable = new JTable(model);
        dataTable.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        dataTable.setRowHeight(26);
        dataTable.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        dataTable.setAutoCreateRowSorter(false);
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2 || !SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }

                int viewRow = dataTable.rowAtPoint(e.getPoint());
                if (viewRow < 0) {
                    return;
                }

                int modelRow = dataTable.convertRowIndexToModel(viewRow);
                new DetailFrame(model, modelRow).open();
            }
        });

        return dataTable;
    }


}
