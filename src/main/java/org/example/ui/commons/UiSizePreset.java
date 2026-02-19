package org.example.ui.commons;

public enum UiSizePreset {
    SMALL(900, 640, 0.20),
    MEDIUM(1100, 760, 0.20),
    APP_FULL_SIZE(1280, 860, 0.20);

    private final int width;
    private final int height;
    private final double headerRatio;

    UiSizePreset(int width, int height, double headerRatio) {
        this.width = width;
        this.height = height;
        this.headerRatio = headerRatio;
    }

    public int width() { return width; }
    public int height() { return height; }
    public double headerRatio() { return headerRatio; }
    public int headerHeightFrom(int frameHeight) { return (int) Math.max(120, frameHeight * headerRatio); }
}