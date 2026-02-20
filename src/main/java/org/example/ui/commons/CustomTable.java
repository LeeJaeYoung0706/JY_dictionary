package org.example.ui.commons;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

public class CustomTable extends JTable {
    public CustomTable(TableModel tableModel) {
        super(tableModel);
        setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        setRowHeight(24);
        getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 12));
    }

    public static CustomTable of(TableModel tableModel) {
        return new CustomTable(tableModel);
    }
}
