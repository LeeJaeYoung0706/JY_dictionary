package org.example;

import org.example.utils.JsonCacheMap;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public final class GlossarySwingViewer {

    private GlossarySwingViewer() {}

    public static void show(String title) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // columns/entries
        JsonCacheMap.Data data = JsonCacheMap.data();
        List<JsonCacheMap.Column> cols = data.columns;
        List<Map<String, String>> entries = data.entries;

        // table model
        GlossaryTableModel model = new GlossaryTableModel(cols, entries);
        JTable table = new JTable(model);

        // 기본 가독성
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(22);

        // 정렬/필터
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // 컬럼 폭 자동 설정(헤더 기준 + 샘플)
        packColumns(table, 240);

        JScrollPane scroll = new JScrollPane(table);

        // 상단 검색바
        JTextField search = new JTextField();
        search.setToolTipText("전체 검색 (모든 컬럼 대상)");

        JLabel count = new JLabel();
        updateCountLabel(count, entries.size(), entries.size());

        search.getDocument().addDocumentListener(new DocumentListener() {
            void apply() {
                String q = search.getText();
                if (q == null || q.isBlank()) {
                    sorter.setRowFilter(null);
                    updateCountLabel(count, inclination(model, sorter), model.getRowCount());
                    return;
                }
                String qq = q.trim().toLowerCase(Locale.ROOT);

                sorter.setRowFilter(new RowFilter<>() {
                    @Override
                    public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                        int row = entry.getIdentifier();
                        for (int c = 0; c < model.getColumnCount(); c++) {
                            Object v = model.getValueAt(row, c);
                            if (v != null && v.toString().toLowerCase(Locale.ROOT).contains(qq)) return true;
                        }
                        return false;
                    }
                });
                updateCountLabel(count, inclination(model, sorter), model.getRowCount());
            }
            @Override public void insertUpdate(DocumentEvent e) { apply(); }
            @Override public void removeUpdate(DocumentEvent e) { apply(); }
            @Override public void changedUpdate(DocumentEvent e) { apply(); }
        });

        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> search.setText(""));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(new JLabel("Search:"), BorderLayout.WEST);
        left.add(search, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(count);
        right.add(reset);

        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        top.add(left, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(top, BorderLayout.NORTH);
        f.getContentPane().add(scroll, BorderLayout.CENTER);

        f.setSize(1200, 750);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static void updateCountLabel(JLabel label, int visible, int total) {
        label.setText("Rows: " + visible + " / " + total);
    }

    private static int inclination(GlossaryTableModel model, TableRowSorter<TableModel> sorter) {
        return sorter.getViewRowCount();
    }

    private static void packColumns(JTable table, int maxWidth) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn column = columnModel.getColumn(col);

            int width = 60;
            TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(
                    table, column.getHeaderValue(), false, false, 0, col);
            width = Math.max(width, headerComp.getPreferredSize().width + 16);

            // 샘플 30행까지만 보고 폭 추정
            int rows = Math.min(30, table.getRowCount());
            TableCellRenderer cellRenderer = table.getDefaultRenderer(Object.class);
            for (int row = 0; row < rows; row++) {
                Object value = table.getValueAt(row, col);
                Component c = cellRenderer.getTableCellRendererComponent(table, value, false, false, row, col);
                width = Math.max(width, c.getPreferredSize().width + 16);
                if (width >= maxWidth) { width = maxWidth; break; }
            }

            column.setPreferredWidth(width);
        }
    }

    // ===== Table Model =====
    static final class GlossaryTableModel extends AbstractTableModel {
        private final List<JsonCacheMap.Column> columns;
        private final List<Map<String, String>> rows;
        private final List<String> columnKeys;
        private final List<String> columnViews;

        GlossaryTableModel(List<JsonCacheMap.Column> columns, List<Map<String, String>> rows) {
            this.columns = columns;
            this.rows = rows;

            this.columnKeys = columns.stream().map(c -> c.key).collect(Collectors.toList());
            this.columnViews = columns.stream().map(c -> c.view).collect(Collectors.toList());
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.size(); }
        @Override public String getColumnName(int column) { return columnViews.get(column); }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Map<String, String> row = rows.get(rowIndex);
            String key = columnKeys.get(columnIndex);
            return row.getOrDefault(key, "");
        }
    }
}