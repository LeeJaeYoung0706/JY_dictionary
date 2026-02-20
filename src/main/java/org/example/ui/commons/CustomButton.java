package org.example.ui.commons;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CustomButton extends JButton {
    public static final Font DEFAULT_FONT = new Font("Malgun Gothic", Font.BOLD, 13);
    public static final Color DEFAULT_BACKGROUND = new Color(66, 133, 244);
    public static final Color DEFAULT_FOREGROUND = Color.WHITE;

    private final Style style = new Style();

    public CustomButton(String text) {
        super(text);
        applyStyle();
    }

    public static CustomButton of(String text) {
        return new CustomButton(text);
    }

    public CustomButton style(Consumer<Style> consumer) {
        consumer.accept(style);
        applyStyle();
        return this;
    }

    private void applyStyle() {
        setFont(style.font != null ? style.font : DEFAULT_FONT);
        setBackground(style.background != null ? style.background : DEFAULT_BACKGROUND);
        setForeground(style.foreground != null ? style.foreground : DEFAULT_FOREGROUND);
        setFocusPainted(style.focusPainted != null ? style.focusPainted : false);

        if (style.width != null && style.height != null) {
            setPreferredSize(new Dimension(style.width, style.height));
        }
    }

    public static class Style {
        private Font font;
        private Color background;
        private Color foreground;
        private Integer width;
        private Integer height;
        private Boolean focusPainted;

        public Style font(Font font) {
            this.font = font;
            return this;
        }

        public Style background(Color background) {
            this.background = background;
            return this;
        }

        public Style foreground(Color foreground) {
            this.foreground = foreground;
            return this;
        }

        public Style size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Style focusPainted(boolean focusPainted) {
            this.focusPainted = focusPainted;
            return this;
        }
    }
}
