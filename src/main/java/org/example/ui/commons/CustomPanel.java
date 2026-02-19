package org.example.ui.commons;

import org.example.ui.layout.FlexLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

public class CustomPanel extends JPanel {
    public static final Insets DEFAULT_PADDING = new Insets(5, 5, 5, 5);
    public static final Insets DEFAULT_MARGIN = new Insets(0, 0, 0, 0);
    public static final boolean DEFAULT_BORDER_ENABLED = false;
    public static final Color DEFAULT_BORDER_COLOR = Color.BLACK;

    private final Style style = new Style();

    public CustomPanel() {
        super();
        applyStyle();
    }

    public CustomPanel(LayoutManager layout) {
        super(layout);
        applyStyle();
    }

    public static CustomPanel of() {
        return new CustomPanel();
    }

    public static CustomPanel of(LayoutManager layout) {
        return new CustomPanel(layout);
    }

    public static CustomPanel flexColumn(int gap) {
        return new CustomPanel(new FlexLayout(FlexLayout.Direction.COLUMN, gap, FlexLayout.Justify.START, FlexLayout.Align.START));
    }

    public static CustomPanel flexRow(int gap) {
        return new CustomPanel(new FlexLayout(FlexLayout.Direction.ROW, gap, FlexLayout.Justify.START, FlexLayout.Align.CENTER));
    }

    public CustomPanel style(Consumer<Style> consumer) {
        consumer.accept(style);
        applyStyle();
        return this;
    }

    private void applyStyle() {
        Insets padding = merge(DEFAULT_PADDING, style.padding);
        Insets margin = merge(DEFAULT_MARGIN, style.margin);

        var paddingBorder = new EmptyBorder(padding);

        var inner = (style.borderEnabled != null ? style.borderEnabled : DEFAULT_BORDER_ENABLED)
                ? BorderFactory.createCompoundBorder(
                new LineBorder(style.borderColor != null ? style.borderColor : DEFAULT_BORDER_COLOR),
                paddingBorder
        )
                : paddingBorder;

        setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(margin), inner));
    }

    private Insets merge(Insets base, Insets override) {
        return new Insets(
                override.top >= 0 ? override.top : base.top,
                override.left >= 0 ? override.left : base.left,
                override.bottom >= 0 ? override.bottom : base.bottom,
                override.right >= 0 ? override.right : base.right
        );
    }

    public static class Style {
        private Insets padding = new Insets(-1, -1, -1, -1);
        private Insets margin = new Insets(-1, -1, -1, -1);
        private Boolean borderEnabled;
        private Color borderColor;

        public Style padding(int v) {
            this.padding = new Insets(v, v, v, v);
            return this;
        }

        public Style padding(int t, int l, int b, int r) {
            this.padding = new Insets(t, l, b, r);
            return this;
        }

        public Style paddingX(int v) {
            padding.left = v;
            padding.right = v;
            return this;
        }

        public Style paddingY(int v) {
            padding.top = v;
            padding.bottom = v;
            return this;
        }

        public Style margin(int v) {
            this.margin = new Insets(v, v, v, v);
            return this;
        }

        public Style leftMargin(int v) {
            margin.left = v;
            return this;
        }

        public Style rightMargin(int v) {
            margin.right = v;
            return this;
        }

        public Style topMargin(int v) {
            margin.top = v;
            return this;
        }

        public Style bottomMargin(int v) {
            margin.bottom = v;
            return this;
        }

        public Style border(boolean enabled) {
            this.borderEnabled = enabled;
            return this;
        }

        public Style borderColor(Color c) {
            this.borderColor = c;
            return this;
        }
    }
}
