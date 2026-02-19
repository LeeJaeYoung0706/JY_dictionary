package org.example.ui.layout;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class FlexLayout implements LayoutManager2 {
    public enum Direction { ROW, COLUMN }
    public enum Justify { START, CENTER, END }
    public enum Align { START, CENTER, END, STRETCH }

    private final Direction direction;
    private final int gap;
    private final Justify justify;
    private final Align align;

    private final Map<Component, Object> constraints = new HashMap<>();

    public FlexLayout(Direction direction, int gap, Justify justify, Align align) {
        this.direction = direction == null ? Direction.ROW : direction;
        this.gap = Math.max(gap, 0);
        this.justify = justify == null ? Justify.START : justify;
        this.align = align == null ? Align.START : align;
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraint) {
        constraints.put(comp, constraint);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) { return 0.5f; }

    @Override
    public float getLayoutAlignmentY(Container target) { return 0.5f; }

    @Override
    public void invalidateLayout(Container target) { }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        constraints.put(comp, null);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        constraints.remove(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Insets in = parent.getInsets();
        int count = parent.getComponentCount();
        int main = 0;
        int cross = 0;

        for (int i = 0; i < count; i++) {
            Component c = parent.getComponent(i);
            if (!c.isVisible()) continue;
            Dimension d = c.getPreferredSize();
            if (direction == Direction.ROW) {
                main += d.width;
                cross = Math.max(cross, d.height);
            } else {
                main += d.height;
                cross = Math.max(cross, d.width);
            }
        }
        if (count > 1) main += gap * (count - 1);

        if (direction == Direction.ROW) {
            return new Dimension(in.left + in.right + main, in.top + in.bottom + cross);
        }
        return new Dimension(in.left + in.right + cross, in.top + in.bottom + main);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        Insets in = parent.getInsets();
        int availableW = parent.getWidth() - in.left - in.right;
        int availableH = parent.getHeight() - in.top - in.bottom;

        Component[] visible = parent.getComponents();
        int count = 0;
        for (Component c : visible) {
            if (c.isVisible()) count++;
        }
        if (count == 0) return;

        int[] mains = new int[visible.length];
        int totalMain = 0;
        for (int i = 0; i < visible.length; i++) {
            if (!visible[i].isVisible()) continue;
            Dimension d = visible[i].getPreferredSize();
            mains[i] = direction == Direction.ROW ? d.width : d.height;
            totalMain += mains[i];
        }
        totalMain += gap * (count - 1);

        int startMain = 0;
        int availableMain = direction == Direction.ROW ? availableW : availableH;
        if (justify == Justify.CENTER) {
            startMain = Math.max(0, (availableMain - totalMain) / 2);
        } else if (justify == Justify.END) {
            startMain = Math.max(0, availableMain - totalMain);
        }

        int cursor = startMain;
        for (int i = 0; i < visible.length; i++) {
            Component c = visible[i];
            if (!c.isVisible()) continue;
            Dimension d = c.getPreferredSize();

            if (direction == Direction.ROW) {
                int h = align == Align.STRETCH ? availableH : d.height;
                int y = in.top + alignOffset(availableH, h);
                c.setBounds(in.left + cursor, y, mains[i], h);
            } else {
                int w = align == Align.STRETCH ? availableW : d.width;
                int x = in.left + alignOffset(availableW, w);
                c.setBounds(x, in.top + cursor, w, mains[i]);
            }
            cursor += mains[i] + gap;
        }
    }

    private int alignOffset(int availableCross, int componentCross) {
        return switch (align) {
            case START, STRETCH -> 0;
            case CENTER -> Math.max(0, (availableCross - componentCross) / 2);
            case END -> Math.max(0, availableCross - componentCross);
        };
    }
}
