package org.example.ui.commons;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class CustomSelectBox extends JComboBox<String> {
    public static final Font DEFAULT_FONT = new Font("Malgun Gothic", Font.PLAIN, 13);

    private final Style style = new Style();

    public CustomSelectBox(List<String> options) {
        super(options.toArray(String[]::new));
        applyStyle();
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
    }

    public static class Style {
        private Font font;
        private Integer width;
        private Integer height;

        public Style font(Font font) { this.font = font; return this; }
        public Style size(int width, int height) { this.width = width; this.height = height; return this; }
    }
}
