package org.example.ui;

import org.example.data.ViewContainer;
import org.example.ui.commons.CustomPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class BodyFrame {
    private final ViewContainer container;

    public BodyFrame(ViewContainer container) {
        this.container = container;
    }

    public CustomPanel build() {
        CustomPanel body = CustomPanel.flexColumn(8)
                .style(s -> s.padding(16));
        body.setBackground(Color.WHITE);

        JTable table = createDataTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        body.add(scrollPane);
        return body;
    }

    private JTable createDataTable() {
        List<Map<String, String>> columns = container.columns();
        List<Map<String, String>> entries = container.entries();

        String[] columnNames = columns.stream()
                .map(c -> c.getOrDefault("view", c.getOrDefault("key", "")))
                .toArray(String[]::new);

        Object[][] rowData = new Object[entries.size()][columns.size()];
        for (int rowIndex = 0; rowIndex < entries.size(); rowIndex++) {
            Map<String, String> entry = entries.get(rowIndex);
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                String key = columns.get(colIndex).getOrDefault("key", "");
                rowData[rowIndex][colIndex] = entry.getOrDefault(key, "");
            }
        }

        JTable table = new JTable(rowData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        table.setAutoCreateRowSorter(true);

        return table;
    }
}
