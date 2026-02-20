package org.example.ui.commons;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CustomDialog extends JDialog {
    private final Style style = new Style();

    public CustomDialog(Window owner, String title) {
        super(owner, title);
        applyStyle();
    }

    public static CustomDialog of(Window owner, String title) {
        return new CustomDialog(owner, title);
    }

    public CustomDialog style(Consumer<Style> consumer) {
        consumer.accept(style);
        applyStyle();
        return this;
    }

    private void applyStyle() {
        setModalityType(style.modalityType != null ? style.modalityType : Dialog.ModalityType.MODELESS);
        if (style.width != null && style.height != null) {
            setSize(style.width, style.height);
        }
    }

    public static class Style {
        private Dialog.ModalityType modalityType;
        private Integer width;
        private Integer height;

        public Style modalityType(Dialog.ModalityType modalityType) {
            this.modalityType = modalityType;
            return this;
        }

        public Style size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }
    }
}
