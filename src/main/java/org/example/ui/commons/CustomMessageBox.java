package org.example.ui.commons;

import javax.swing.*;
import java.awt.*;

public final class CustomMessageBox {
    private CustomMessageBox() {
    }

    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmOkCancel(Component parent, String message, String title) {
        int confirmed = JOptionPane.showConfirmDialog(
                parent,
                message,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return confirmed == JOptionPane.OK_OPTION;
    }
}
