package org.example.ui.commons;

import javax.swing.*;

public class CustomScrollPane extends JScrollPane {
    public CustomScrollPane(java.awt.Component view) {
        super(view);
    }

    public static CustomScrollPane of(java.awt.Component view) {
        return new CustomScrollPane(view);
    }
}
