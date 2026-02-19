package org.example.ui.commons;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CustomLabel extends JLabel {
    public static final Font DEFAULT_FONT = new Font("Malgun Gothic", Font.PLAIN, 14);
    public static final Color DEFAULT_COLOR = new Color(34, 34, 34);
    public static final int DEFAULT_ALIGN = SwingConstants.LEFT;

    private final Style style = new Style();

    public CustomLabel() {
        this("");
    }

    public CustomLabel(String text) {
        super(text);
        applyStyle();
    }

    public static CustomLabel of(String text) {
        return new CustomLabel(text);
    }

    public CustomLabel style(Consumer<Style> consumer) {
        consumer.accept(style);
        applyStyle();
        return this;
    }

    private void applyStyle() {
        setFont(style.font != null ? style.font : DEFAULT_FONT);
        setForeground(style.color != null ? style.color : DEFAULT_COLOR);
        setHorizontalAlignment(style.align != null ? style.align : DEFAULT_ALIGN);
    }

    public static class Style {
        private Font font;
        private Color color;
        private Integer align;

        public Style font(Font font) {
            this.font = font;
            return this;
        }

        public Style color(Color color) {
            this.color = color;
            return this;
        }

        public Style align(int align) {
            this.align = align;
            return this;
        }
    }
}
