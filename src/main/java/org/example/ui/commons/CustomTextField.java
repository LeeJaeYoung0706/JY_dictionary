package org.example.ui.commons;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CustomTextField extends JTextField {
    public static final int DEFAULT_COLUMNS = 14;
    public static final Font DEFAULT_FONT = new Font("Malgun Gothic", Font.PLAIN, 13);

    private final Style style = new Style();

    public CustomTextField() {
        this("");
    }

    public CustomTextField(String text) {
        super(text, DEFAULT_COLUMNS);
        applyStyle();
    }

    public static CustomTextField of() {
        return new CustomTextField();
    }

    public CustomTextField style(Consumer<Style> consumer) {
        consumer.accept(style);
        applyStyle();
        return this;
    }

    private void applyStyle() {
        setFont(style.font != null ? style.font : DEFAULT_FONT);
        if (style.columns != null) {
            setColumns(style.columns);
        }
    }

    public static class Style {
        private Font font;
        private Integer columns;

        public Style font(Font font) { this.font = font; return this; }
        public Style columns(int columns) { this.columns = columns; return this; }
    }
}
