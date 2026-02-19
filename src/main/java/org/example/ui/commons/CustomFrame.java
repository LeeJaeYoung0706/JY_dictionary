package org.example.ui.commons;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CustomFrame extends JFrame {
    public static final int DEFAULT_WIDTH = 1100;
    public static final int DEFAULT_HEIGHT = 760;
    public static final int DEFAULT_CLOSE_OPERATION = WindowConstants.EXIT_ON_CLOSE;

    private final Style style = new Style();

    public CustomFrame(String title) {
        super(title);
        applyStyle();
    }

    public static CustomFrame of(String title) {
        return new CustomFrame(title);
    }

    public CustomFrame style(Consumer<Style> consumer) {
        consumer.accept(style);
        applyStyle();
        return this;
    }

    private void applyStyle() {
        int width = style.width != null ? style.width : DEFAULT_WIDTH;
        int height = style.height != null ? style.height : DEFAULT_HEIGHT;
        setDefaultCloseOperation(style.closeOperation != null ? style.closeOperation : DEFAULT_CLOSE_OPERATION);
        setSize(width, height);
        setLocationRelativeTo(null);
    }

    public static class Style {
        private Integer width;
        private Integer height;
        private Integer closeOperation;

        public Style size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Style closeOperation(int closeOperation) {
            this.closeOperation = closeOperation;
            return this;
        }
    }
}
