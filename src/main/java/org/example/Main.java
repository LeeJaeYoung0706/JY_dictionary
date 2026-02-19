package org.example;

import org.example.data.JsonCacheMap;
import org.example.data.ViewContainer;
import org.example.ui.commons.CustomFrame;
import org.example.ui.commons.CustomLabel;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.UiSizePreset;

import javax.swing.*;
import java.awt.*;

public class Main {

    private static final String APP_TITLE = "용어사전";
    private static final UiSizePreset APP_SIZE = UiSizePreset.APP_FULL_SIZE;


    public static void main(String[] args) {

        JsonCacheMap.initializeOnce();

//        SwingUtilities.invokeLater(() -> {
//
//            try {
//                UIManager.setLookAndFeel(
//                        UIManager.getSystemLookAndFeelClassName()
//                );
//            } catch (Exception ignore) {}
//
//            System.out.println("Cache initialized: title=" + JsonCacheMap.appTitle() + ", entries=" + JsonCacheMap.entries().size());
//
//            List<Map<String, String>> entries = JsonCacheMap.entries();
//
//            for (Map<String, String> entry : entries) {
//                System.out.println(" entry = " + entry.toString());
//
//
//
//                System.out.println("RAW kc = " + entry.get("koreancustomer"));
//                System.out.println("RAW dept = " + entry.get("koreandept"));
//            }
//        });
//
//        SwingUtilities.invokeLater(() -> {
//            GlossarySwingViewer.show(JsonCacheMap.appTitle());
//        });
//        System.out.println("[charset.default] " + java.nio.charset.Charset.defaultCharset());
//        System.out.println("[file.encoding] " + System.getProperty("file.encoding"));
//        System.out.println("[sun.stdout.encoding] " + System.getProperty("sun.stdout.encoding"));
//        System.out.println("[sun.stderr.encoding] " + System.getProperty("sun.stderr.encoding"));

        ViewContainer container = ViewContainer.fromCache();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignore) {
            }

            CustomPanel root = CustomPanel.flexColumn(12)
                    .style(s -> s.padding(24));
            root.setBackground(new Color(245, 247, 250));

            root.add(CustomLabel.of(APP_TITLE)
                    .style(s -> s.font(new Font("Malgun Gothic", Font.BOLD, 22))));

            root.add(CustomLabel.of("테스트 타이틀" + APP_TITLE));
            root.add(CustomLabel.of("테스트 공지" + container.notice()));
            root.add(CustomLabel.of("테스트 엔트리 사이즈 " + container.entries().size()));

            CustomPanel row = CustomPanel.flexRow(8)
                    .style(s -> s.padding(8).border(true).borderColor(new Color(220, 220, 220)).topMargin(8));
            row.add(CustomLabel.of("테스트 라벨"));
            root.add(row);

            CustomFrame frame = CustomFrame.of(APP_TITLE)
                    .style(s -> s.size(1100, 760));
            frame.setContentPane(root);
            frame.setVisible(true);
        });
    }
}