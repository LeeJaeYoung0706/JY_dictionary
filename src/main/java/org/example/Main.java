package org.example;

import org.example.utils.JsonCacheMap;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class Main {
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

        SwingUtilities.invokeLater(() -> {
            GlossarySwingViewer.show(JsonCacheMap.appTitle());
        });
//        System.out.println("[charset.default] " + java.nio.charset.Charset.defaultCharset());
//        System.out.println("[file.encoding] " + System.getProperty("file.encoding"));
//        System.out.println("[sun.stdout.encoding] " + System.getProperty("sun.stdout.encoding"));
//        System.out.println("[sun.stderr.encoding] " + System.getProperty("sun.stderr.encoding"));
    }
}