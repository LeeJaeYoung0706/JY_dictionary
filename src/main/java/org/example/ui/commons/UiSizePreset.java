package org.example.ui.commons;

public enum UiSizePreset {
    APP_SMALL(900, 640, 0.20),
    APP_MEDIUM(1100, 760, 0.20),
    APP_FULL_SIZE(1600, 960, 0.25);

    private static final int MIN_HEADER_HEIGHT = 120;

    private final int appWidth;
    private final int appHeight;
    private final double headerRatio;

    UiSizePreset(int appWidth, int appHeight, double headerRatio) {
        this.appWidth = appWidth;
        this.appHeight = appHeight;
        this.headerRatio = headerRatio;
    }

    public int appWidth() {
        return appWidth;
    }

    public int appHeight() {
        return appHeight;
    }

    public double headerRatio() {
        return headerRatio;
    }

    public int headerHeight() {
        return (int) Math.max(MIN_HEADER_HEIGHT, appHeight * headerRatio);
    }
}
