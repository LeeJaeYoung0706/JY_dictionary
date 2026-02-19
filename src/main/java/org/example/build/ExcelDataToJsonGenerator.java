package org.example.build;

import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ExcelDataToJsonGenerator {

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }


    private static void run(String[] args) throws Exception {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("Usage: <inputXlsx> <outputJson>");
        }

        Path input = Paths.get(args[0]).toAbsolutePath().normalize();
        Path output = Paths.get(args[1]).toAbsolutePath().normalize();

        if (!Files.exists(input)) throw new NoSuchFileException("Input xlsx not found: " + input);
        if (Files.size(input) == 0) throw new IllegalStateException("data.xlsx is 0 bytes: " + input);

        if (output.getParent() != null) Files.createDirectories(output.getParent());

        System.out.println("input =" + input);
        System.out.println("output=" + output);

        DataFormatter fmt = new DataFormatter();

        try (InputStream in = Files.newInputStream(input);
             Workbook wb = WorkbookFactory.create(in)) {

            if (wb.getNumberOfSheets() <= 0) throw new IllegalStateException("엑셀에 시트가 없습니다.");
            Sheet sh = wb.getSheetAt(0);

            // B1 = app title
            String appTitle = getString(sh, 0, 1, fmt);
            if (appTitle.isBlank()) appTitle = "용어사전";

            // Row 2: key headers (internal)
            // Row 3: view headers (display, Korean)
            int keyHeaderRowIdx = 1;   // row2
            int viewHeaderRowIdx = 2;  // row3
            Row keyHeaderRow = sh.getRow(keyHeaderRowIdx);
            Row viewHeaderRow = sh.getRow(viewHeaderRowIdx);

            if (keyHeaderRow == null) throw new IllegalStateException("2행(키 헤더)이 없습니다.");
            if (viewHeaderRow == null) throw new IllegalStateException("3행(뷰 헤더)이 없습니다.");

            List<Column> columns = readColumns(keyHeaderRow, viewHeaderRow, fmt);
            if (columns.isEmpty()) throw new IllegalStateException("유효한 헤더(2행/3행 매핑)가 없습니다.");

            // Data starts at Row 4 (index 3)
            int dataStartRowIdx = 3;

            List<Map<String, String>> entries = new ArrayList<>();
            int lastRow = sh.getLastRowNum();

            for (int r = dataStartRowIdx; r <= lastRow; r++) {
                Row row = sh.getRow(r);
                if (row == null) continue;

                Map<String, String> m = new LinkedHashMap<>();
                boolean anyValue = false;

                for (Column col : columns) {
                    String val = getString(row, col.colIndex, fmt);
                    if (!val.isBlank()) anyValue = true;
                    m.put(col.key, val);
                }

                // 내용 없어도 보여주기.
                //if (m.getOrDefault("term", "").isBlank()) continue;

                if (!anyValue) continue;
                entries.add(m);
            }

            String json = toJson(appTitle, columns, entries);

            Files.writeString(output, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Generated: " + output);
            System.out.println("columns=" + columns.size());
            System.out.println("entries=" + entries.size());
        }
    }

    // ===== Excel helpers =====

    private static String getString(Sheet sh, int rowIdx, int colIdx, DataFormatter fmt) {
        Row r = sh.getRow(rowIdx);
        if (r == null) return "";
        return getString(r, colIdx, fmt);
    }

    private static String getString(Row row, int colIdx, DataFormatter fmt) {
        Cell c = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (c == null) return "";
        return fmt.formatCellValue(c).trim();
    }

    private static List<Column> readColumns(Row keyHeaderRow, Row viewHeaderRow, DataFormatter fmt) {
        int last = Math.max(keyHeaderRow.getLastCellNum(), viewHeaderRow.getLastCellNum());
        if (last <= 0) return List.of();

        List<Column> cols = new ArrayList<>(last);
        Map<String, Integer> dup = new HashMap<>();

        for (int c = 0; c < last; c++) {
            String key = normalizeKey(getString(keyHeaderRow, c, fmt)).toLowerCase(Locale.ROOT);
            String view = normalizeView(getString(viewHeaderRow, c, fmt));

            if (key.isBlank()) continue;
            if (view.isBlank()) view = key;

            int n = dup.getOrDefault(key, 0) + 1;
            dup.put(key, n);
            String finalKey = (n == 1) ? key : key + "__" + n;

            cols.add(new Column(c, finalKey, view));
        }

        return cols;
    }

    private static String normalizeKey(String s) {
        if (s == null) return "";
        String x = s.trim().replace('\u00A0', ' ');
        x = x.replaceAll("[\\t\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
        return x;
    }

    private static String normalizeView(String s) {
        if (s == null) return "";
        String x = s.trim().replace('\u00A0', ' ');
        x = x.replaceAll("[\\t\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
        return x;
    }

    // ===== JSON =====

    private static String toJson(String appTitle, List<Column> columns, List<Map<String, String>> entries) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("{");
        sb.append("\"appTitle\":\"").append(escapeJson(appTitle)).append("\",");

        sb.append("\"columns\":[");
        for (int i = 0; i < columns.size(); i++) {
            Column c = columns.get(i);
            sb.append("{")
                    .append("\"key\":\"").append(escapeJson(c.key)).append("\",")
                    .append("\"view\":\"").append(escapeJson(c.view)).append("\"")
                    .append("}");
            if (i < columns.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"entries\":[");
        for (int i = 0; i < entries.size(); i++) {
            Map<String, String> e = entries.get(i);
            sb.append("{");

            int k = 0;
            for (Column col : columns) {
                if (k++ > 0) sb.append(",");
                String val = e.getOrDefault(col.key, "");
                sb.append("\"").append(escapeJson(col.key)).append("\":")
                        .append("\"").append(escapeJson(val)).append("\"");
            }

            sb.append("}");
            if (i < entries.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (ch <= 0x1F) out.append(String.format("\\u%04x", (int) ch));
                    else out.append(ch);
            }
        }
        return out.toString();
    }

    private static final class Column {
        final int colIndex;
        final String key;
        final String view;

        Column(int colIndex, String key, String view) {
            this.colIndex = colIndex;
            this.key = key;
            this.view = view;
        }
    }
}
