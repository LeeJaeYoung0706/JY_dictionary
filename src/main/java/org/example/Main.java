package org.example;

import org.example.data.ViewContainer;
import org.example.ui.BodyFrame;
import org.example.ui.HeaderFrame;
import org.example.ui.commons.CustomFrame;
import org.example.ui.commons.CustomPanel;
import org.example.ui.commons.UiSizePreset;

import javax.swing.*;
import java.awt.*;

public class Main {

//    private static final String APP_TITLE = "용어사전";
//    private static final UiSizePreset APP_SIZE = UiSizePreset.APP_FULL_SIZE;
//
//
////    public static void main(String[] args) {
////
////        JsonCacheMap.initializeOnce();
////
//////        SwingUtilities.invokeLater(() -> {
//////
//////            try {
//////                UIManager.setLookAndFeel(
//////                        UIManager.getSystemLookAndFeelClassName()
//////                );
//////            } catch (Exception ignore) {}
//////
//////            System.out.println("Cache initialized: title=" + JsonCacheMap.appTitle() + ", entries=" + JsonCacheMap.entries().size());
//////
//////            List<Map<String, String>> entries = JsonCacheMap.entries();
//////
//////            for (Map<String, String> entry : entries) {
//////                System.out.println(" entry = " + entry.toString());
//////
//////
//////
//////                System.out.println("RAW kc = " + entry.get("koreancustomer"));
//////                System.out.println("RAW dept = " + entry.get("koreandept"));
//////            }
//////        });
//////
//////        SwingUtilities.invokeLater(() -> {
//////            GlossarySwingViewer.show(JsonCacheMap.appTitle());
//////        });
//////        System.out.println("[charset.default] " + java.nio.charset.Charset.defaultCharset());
//////        System.out.println("[file.encoding] " + System.getProperty("file.encoding"));
//////        System.out.println("[sun.stdout.encoding] " + System.getProperty("sun.stdout.encoding"));
//////        System.out.println("[sun.stderr.encoding] " + System.getProperty("sun.stderr.encoding"));
////
////        ViewContainer container = ViewContainer.fromCache();
////        SwingUtilities.invokeLater(() -> {
////            try {
////                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
////            } catch (Exception ignore) {
////            }
////
////            CustomPanel root = CustomPanel.of(new BorderLayout());
////            root.style(s -> s.padding(0));
////            root.setBackground(new Color(245, 247, 250));
////
////            CustomPanel header = buildHeader(container, APP_SIZE);
////            root.add(header, BorderLayout.NORTH);
////
////            CustomPanel body = CustomPanel.flexColumn(8)
////                    .style(s -> s.padding(16));
////            body.add(CustomLabel.of("결과 영역 (다음 단계에서 목록/테이블 구성)")
////                    .style(s -> s.font(new Font("Malgun Gothic", Font.PLAIN, 14))));
////            root.add(body, BorderLayout.CENTER);
////
////            CustomFrame frame = CustomFrame.of(APP_TITLE)
////                    .style(s -> s.preset(APP_SIZE));
////            frame.setContentPane(root);
////            frame.setVisible(true);
////        });
////    }
//
//
//    public static void main(String[] args) {
//        ViewContainer container = ViewContainer.fromCache();
//
//        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception ignore) {
//            }
//
//            CustomPanel root = CustomPanel.of(new BorderLayout());
//            root.style(s -> s.padding(0));
//            root.setBackground(new Color(245, 247, 250));
//
//            CustomPanel header = buildHeader(container, APP_SIZE);
//            root.add(header, BorderLayout.NORTH);
//
//            CustomPanel body = CustomPanel.flexColumn(8)
//                    .style(s -> s.padding(16));
//            body.add(CustomLabel.of("결과 영역 (다음 단계에서 목록/테이블 구성)")
//                    .style(s -> s.font(new Font("Malgun Gothic", Font.PLAIN, 14))));
//            root.add(body, BorderLayout.CENTER);
//
//            CustomFrame frame = CustomFrame.of(APP_TITLE)
//                    .style(s -> s.preset(APP_SIZE));
//            frame.setContentPane(root);
//            frame.setVisible(true);
//        });
//    }
//
//    private static final class SearchField {
//        final String baseKey;
//        String label;
//        boolean freeText;
//        final List<String> keys = new ArrayList<>();
//        final List<String> options = new ArrayList<>();
//
//        SearchField(String baseKey, String label) {
//            this.baseKey = baseKey;
//            this.label = label;
//        }
//    }
//
//    private static CustomPanel buildHeader(ViewContainer container, UiSizePreset sizePreset) {
//        CustomPanel header = CustomPanel.flexColumn(10)
//                .style(s -> s.padding(14, 16, 14, 16).border(true).borderColor(new Color(224, 224, 224)));
//        header.setBackground(Color.WHITE);
//        header.setPreferredSize(new Dimension(sizePreset.appWidth(), sizePreset.headerHeight()));
//
//        CustomPanel row1 = CustomPanel.flexRow(12).style(s -> s.padding(0));
//        row1.add(CustomLabel.of(APP_TITLE).style(s -> s.font(new Font("Malgun Gothic", Font.BOLD, 20))));
//        row1.add(CustomLabel.of("공지: " + container.notice()).style(s -> s.font(new Font("Malgun Gothic", Font.PLAIN, 13))));
//        header.add(row1);
//
//        List<SearchField> fields = buildSearchFields(container);
//        for (int i = 0; i < fields.size(); i += 3) {
//            CustomPanel row = CustomPanel.flexRow(10).style(s -> s.padding(0));
//            for (int j = i; j < Math.min(i + 3, fields.size()); j++) {
//                row.add(createFilterComponent(fields.get(j)));
//            }
//            header.add(row);
//        }
//
//        return header;
//    }
//
//    private static JComponent createFilterComponent(SearchField field) {
//        CustomPanel wrapper = CustomPanel.flexColumn(4).style(s -> s.padding(0));
//        wrapper.setPreferredSize(new Dimension(380, 56));
//        wrapper.add(CustomLabel.of(field.label));
//
//        if (field.freeText) {
//            wrapper.add(CustomTextField.of().style(s -> s.columns(18)));
//        } else {
//            List<String> options = new ArrayList<>();
//            options.add("전체");
//            options.addAll(field.options);
//            wrapper.add(CustomSelectBox.of(options).style(s -> s.size(250, 28)));
//        }
//        return wrapper;
//    }
//
//    private static List<SearchField> buildSearchFields(ViewContainer container) {
//        List<Map<String, String>> columns = container.columns();
//        List<Map<String, String>> entries = container.entries();
//
//        Map<String, SearchField> grouped = new LinkedHashMap<>();
//
//        for (Map<String, String> c : columns) {
//            String key = c.getOrDefault("key", "");
//            if (key.isBlank()) continue;
//
//            String normalized = normalizeBaseKey(key);
//            String label = c.getOrDefault("view", normalized);
//
//
//            String labelReplace = label.replace("한글", "").replace("영어", "").trim();
//            if (labelReplace.isBlank()) {
//                labelReplace = normalized;
//            }
//            String finalLabel = labelReplace;
//            SearchField field = grouped.computeIfAbsent(normalized, k -> new SearchField(normalized, finalLabel));
//            field.keys.add(key);
//        }
//
//        for (SearchField field : grouped.values()) {
//            boolean meaningLike = field.baseKey.toLowerCase(Locale.ROOT).contains("meaning");
//            field.freeText = meaningLike;
//
//            if (!field.freeText) {
//                Set<String> values = new TreeSet<>();
//                for (Map<String, String> e : entries) {
//                    for (String key : field.keys) {
//                        String v = e.getOrDefault(key, "").trim();
//                        if (!v.isBlank()) values.add(v);
//                    }
//                }
//                field.options.addAll(values);
//                field.label = field.label + " (영어/한글)";
//            }
//        }
//
//        return grouped.values().stream().collect(Collectors.toList());
//    }
//
//    private static String normalizeBaseKey(String key) {
//        String lower = key.toLowerCase(Locale.ROOT);
//        if (lower.startsWith("korean")) {
//            return key.substring(6);
//        }
//        return key;
//    }

    private static final String APP_TITLE = "용어사전";
    private static final UiSizePreset APP_SIZE = UiSizePreset.APP_FULL_SIZE;

    public static void main(String[] args) {
        ViewContainer container = ViewContainer.fromCache();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignore) {
            }

            CustomPanel root = CustomPanel.of(new BorderLayout());
            root.style(s -> s.padding(0));
            root.setBackground(new Color(245, 247, 250));

            BodyFrame bodyFrame = new BodyFrame(container, APP_SIZE);
            CustomPanel body = bodyFrame.build();
            root.add(body, BorderLayout.CENTER);

            CustomPanel header = new HeaderFrame(container, APP_SIZE, bodyFrame::applySearchConditions).build();
            root.add(header, BorderLayout.NORTH);

            CustomFrame frame = CustomFrame.of(APP_TITLE)
                    .style(s -> s.preset(APP_SIZE));
            frame.setContentPane(root);
            frame.setVisible(true);
        });
    }


}