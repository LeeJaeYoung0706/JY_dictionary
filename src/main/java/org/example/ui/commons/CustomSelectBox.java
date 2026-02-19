package org.example.ui.commons;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class CustomSelectBox extends JComboBox<String> {
    public static final Font DEFAULT_FONT = new Font("Malgun Gothic", Font.PLAIN, 13);

    private final Style style = new Style();
    private final List<String> allOptions = new ArrayList<>();
    private boolean updating = false;

    public CustomSelectBox(List<String> options) {
        super(options.toArray(String[]::new));
        this.allOptions.addAll(options);

        applyStyle();
        installEnterSearch();
    }

    public static CustomSelectBox of(List<String> options) {
        return new CustomSelectBox(options);
    }

    public CustomSelectBox style(Consumer<Style> consumer) {
        consumer.accept(style);
        applyStyle();
        return this;
    }

    private void applyStyle() {
        setFont(style.font != null ? style.font : DEFAULT_FONT);
        if (style.width != null && style.height != null) {
            setPreferredSize(new Dimension(style.width, style.height));
        }
        setEditable(style.searchable == null || style.searchable);
    }

    private void installEnterSearch() {
        JTextComponent editor = getEditorComponent();
        if (editor == null) {
            return;
        }

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && isEditable() && !updating) {
                    e.consume();
                    filterAndOpen(editor.getText());
                }
            }
        });
    }

    private JTextComponent getEditorComponent() {
        if (getEditor().getEditorComponent() instanceof JTextComponent editor) {
            return editor;
        }
        return null;
    }

    private void filterAndOpen(String keyword) {
        String raw = keyword == null ? "" : keyword;
        String query = raw.trim().toLowerCase(Locale.ROOT);

        List<String> filtered = new ArrayList<>();
        for (String option : allOptions) {
            if (query.isEmpty() || option.toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(option);
            }
        }

        updating = true;
        try {
            setModel(new DefaultComboBoxModel<>(filtered.toArray(String[]::new)));
            setSelectedIndex(-1);

            JTextComponent editor = getEditorComponent();
            if (editor != null) {
                editor.setText(raw);
                editor.setCaretPosition(raw.length());
            }

            if (!filtered.isEmpty()) {
                showPopup();
            } else {
                hidePopup();
            }
        } finally {
            updating = false;
        }
    }

    public static class Style {
        private Font font;
        private Integer width;
        private Integer height;
        private Boolean searchable;

        public Style font(Font font) {
            this.font = font;
            return this;
        }

        public Style size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Style searchable(boolean searchable) {
            this.searchable = searchable;
            return this;
        }
    }
}
