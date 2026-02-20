package org.example.ui;

import org.example.ui.commons.CustomFrame;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.UiSizePreset;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

public class DetailFrame {
    private final TableModel model;
    private final int modelRow;

    public DetailFrame(TableModel model, int modelRow) {
        this.model = model;
        this.modelRow = modelRow;
    }

    public void open() {
        CustomFrame detailFrame = CustomFrame.of("상세 정보")
                .style(s -> s.preset(UiSizePreset.APP_SMALL).closeOperation(WindowConstants.DISPOSE_ON_CLOSE));

        CustomPanel root = CustomPanel.of(new BorderLayout())
                .style(s -> s.padding(16));
        root.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("선택한 데이터 상세보기");
        title.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        root.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridBagLayout());
        details.setBackground(Color.WHITE);
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        for (int col = 0; col < model.getColumnCount(); col++) {
            JLabel keyLabel = new JLabel(String.valueOf(model.getColumnName(col)));
            keyLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
            keyLabel.setForeground(new Color(66, 66, 66));

            Object rawValue = model.getValueAt(modelRow, col);
            JTextArea valueArea = new JTextArea(rawValue == null ? "" : String.valueOf(rawValue));
            valueArea.setLineWrap(true);
            valueArea.setWrapStyleWord(true);
            valueArea.setEditable(false);
            valueArea.setOpaque(false);
            valueArea.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
            valueArea.setForeground(new Color(33, 33, 33));

            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            details.add(keyLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            details.add(valueArea, gbc);

            gbc.gridy++;
        }

        JScrollPane detailScrollPane = new JScrollPane(details);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        root.add(detailScrollPane, BorderLayout.CENTER);

        detailFrame.setContentPane(root);
        detailFrame.setVisible(true);
    }
}
