package org.example.ui.commons;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import javax.swing.*;
import java.util.function.Consumer;

public class CustomFrame extends JFrame {
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
        UiSizePreset preset = style.sizePreset != null ? style.sizePreset : UiSizePreset.MEDIUM;
        setDefaultCloseOperation(style.closeOperation != null ? style.closeOperation : DEFAULT_CLOSE_OPERATION);
        setSize(preset.width(), preset.height());
        setLocationRelativeTo(null);
    }

    public static class Style {
        private UiSizePreset sizePreset;
        private Integer closeOperation;

        public Style preset(UiSizePreset preset) {
            this.sizePreset = preset;
            return this;
        }

        public Style closeOperation(int closeOperation) {
            this.closeOperation = closeOperation;
            return this;
        }
    }
}

