package com.schedulix.utils;

import com.schedulix.models.TimetableSlot;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;

/**
 * Handles PDF and Excel export of the timetable.
 * Uses Apache POI for Excel and iTextPDF for PDF.
 * Fallback: plain CSV if libraries not present.
 */
public class ExportUtils {

    // ───────────────────────────────────────────
    //  EXCEL EXPORT  (Apache POI)
    // ───────────────────────────────────────────
    public static void exportExcel(List<TimetableSlot> slots,
                                   List<String> classNames,
                                   Stage ownerStage) {
        if (slots == null || slots.isEmpty()) {
            showInfo("Export Excel", "No timetable data to export. Please generate a timetable first.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Excel File");
        fc.setInitialFileName("Schedulix_Timetable.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showSaveDialog(ownerStage);
        if (file == null) return;

        try {
            // Try Apache POI via reflection (graceful fallback)
            exportExcelPOI(slots, classNames, file);
            showInfo("Export Successful", "Timetable exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            // Fallback to CSV
            try {
                File csvFile = new File(file.getAbsolutePath().replace(".xlsx", ".csv"));
                exportCSV(slots, classNames, csvFile);
                showInfo("Exported as CSV",
                    "Apache POI not found. Exported as CSV instead:\n" + csvFile.getAbsolutePath() +
                    "\n\nTo enable .xlsx export, add Apache POI to your pom.xml dependencies.");
            } catch (IOException ioEx) {
                showError("Export Failed", ioEx.getMessage());
            }
        }
    }

    private static void exportExcelPOI(List<TimetableSlot> slots,
                                        List<String> classNames,
                                        File file) throws Exception {
        // Dynamic POI loading — works when poi-ooxml jar is on classpath
        Class<?> workbookClass  = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
        Object   workbook       = workbookClass.getDeclaredConstructor().newInstance();

        Class<?> sheetClass     = Class.forName("org.apache.poi.ss.usermodel.Sheet");
        Object   sheet          = workbookClass.getMethod("createSheet", String.class)
                                               .invoke(workbook, "Timetable");

        Class<?> rowClass       = Class.forName("org.apache.poi.ss.usermodel.Row");
        Class<?> cellClass      = Class.forName("org.apache.poi.ss.usermodel.Cell");

        // Header row
        Object headerRow = sheetClass.getMethod("createRow", int.class).invoke(sheet, 0);
        String[] headers = new String[classNames.size() + 2];
        headers[0] = "Period"; headers[1] = "Time";
        for (int i = 0; i < classNames.size(); i++) headers[i + 2] = classNames.get(i);
        for (int i = 0; i < headers.length; i++) {
            Object cell = rowClass.getMethod("createCell", int.class).invoke(headerRow, i);
            cellClass.getMethod("setCellValue", String.class).invoke(cell, headers[i]);
        }

        // Data rows
        for (int r = 0; r < slots.size(); r++) {
            TimetableSlot slot = slots.get(r);
            Object row = sheetClass.getMethod("createRow", int.class).invoke(sheet, r + 1);
            Object c0 = rowClass.getMethod("createCell", int.class).invoke(row, 0);
            cellClass.getMethod("setCellValue", String.class).invoke(c0, slot.getPeriod());
            Object c1 = rowClass.getMethod("createCell", int.class).invoke(row, 1);
            cellClass.getMethod("setCellValue", String.class).invoke(c1, slot.getTime());
            for (int c = 0; c < slot.getClassCount(); c++) {
                Object cell = rowClass.getMethod("createCell", int.class).invoke(row, c + 2);
                cellClass.getMethod("setCellValue", String.class).invoke(cell, slot.getClassSlot(c));
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbookClass.getMethod("write", OutputStream.class).invoke(workbook, fos);
        }
        workbookClass.getMethod("close").invoke(workbook);
    }

    private static void exportCSV(List<TimetableSlot> slots,
                                   List<String> classNames,
                                   File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Header
            StringBuilder header = new StringBuilder("Period,Time");
            for (String cn : classNames) header.append(",").append(cn);
            pw.println(header);
            // Data
            for (TimetableSlot slot : slots) {
                StringBuilder row = new StringBuilder();
                row.append(csvEscape(slot.getPeriod())).append(",");
                row.append(csvEscape(slot.getTime()));
                for (int c = 0; c < slot.getClassCount(); c++) {
                    row.append(",").append(csvEscape(slot.getClassSlot(c)));
                }
                pw.println(row);
            }
        }
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        s = s.replace("\n", " | ");
        if (s.contains(",") || s.contains("\"")) s = "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    // ───────────────────────────────────────────
    //  PDF EXPORT  (iTextPDF)
    // ───────────────────────────────────────────
    public static void exportPDF(List<TimetableSlot> slots,
                                  List<String> classNames,
                                  Stage ownerStage) {
        if (slots == null || slots.isEmpty()) {
            showInfo("Export PDF", "No timetable data to export. Please generate a timetable first.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save PDF File");
        fc.setInitialFileName("Schedulix_Timetable.pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(ownerStage);
        if (file == null) return;

        try {
            exportPDFiText(slots, classNames, file);
            showInfo("Export Successful", "PDF exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            // Fallback: plain text .txt
            try {
                File txtFile = new File(file.getAbsolutePath().replace(".pdf", ".txt"));
                exportTextFallback(slots, classNames, txtFile);
                showInfo("Exported as Text",
                    "iTextPDF not found. Exported as text instead:\n" + txtFile.getAbsolutePath() +
                    "\n\nTo enable .pdf export, add iTextPDF to your pom.xml dependencies.");
            } catch (IOException ioEx) {
                showError("Export Failed", ioEx.getMessage());
            }
        }
    }

    private static void exportPDFiText(List<TimetableSlot> slots,
                                        List<String> classNames,
                                        File file) throws Exception {
        Class<?> docClass     = Class.forName("com.itextpdf.text.Document");
        Class<?> writerClass  = Class.forName("com.itextpdf.text.pdf.PdfWriter");
        Class<?> tableClass   = Class.forName("com.itextpdf.text.pdf.PdfPTable");
        Class<?> cellClass    = Class.forName("com.itextpdf.text.pdf.PdfPCell");
        Class<?> phraseClass  = Class.forName("com.itextpdf.text.Phrase");
        Class<?> fontClass    = Class.forName("com.itextpdf.text.Font");
        Class<?> paraClass    = Class.forName("com.itextpdf.text.Paragraph");

        Object doc = docClass.getDeclaredConstructor().newInstance();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            writerClass.getMethod("getInstance", docClass, OutputStream.class).invoke(null, doc, fos);
            docClass.getMethod("open").invoke(doc);

            // Title
            Object titlePhrase = phraseClass.getDeclaredConstructor(String.class).newInstance("SCHEDULIX – Timetable Export\n\n");
            Object titlePara   = paraClass.getDeclaredConstructor(String.class).newInstance("SCHEDULIX – Timetable Export");
            docClass.getMethod("add", Class.forName("com.itextpdf.text.Element")).invoke(doc, titlePara);

            // Table
            int cols = classNames.size() + 2;
            Object table = tableClass.getDeclaredConstructor(int.class).newInstance(cols);

            // Header cells
            String[] headers = new String[cols];
            headers[0] = "Period"; headers[1] = "Time";
            for (int i = 0; i < classNames.size(); i++) headers[i + 2] = classNames.get(i);
            for (String h : headers) {
                Object phrase = phraseClass.getDeclaredConstructor(String.class).newInstance(h);
                Object cell   = cellClass.getDeclaredConstructor(phraseClass).newInstance(phrase);
                tableClass.getMethod("addCell", cellClass).invoke(table, cell);
            }

            // Data
            for (TimetableSlot slot : slots) {
                addCell(table, tableClass, cellClass, phraseClass, slot.getPeriod().replace("\n", " "));
                addCell(table, tableClass, cellClass, phraseClass, slot.getTime().replace("\n", " "));
                for (int c = 0; c < slot.getClassCount(); c++) {
                    addCell(table, tableClass, cellClass, phraseClass,
                        slot.getClassSlot(c).replace("\n", " / "));
                }
            }

            docClass.getMethod("add", Class.forName("com.itextpdf.text.Element")).invoke(doc, table);
            docClass.getMethod("close").invoke(doc);
        }
    }

    private static void addCell(Object table, Class<?> tableClass, Class<?> cellClass,
                                  Class<?> phraseClass, String text) throws Exception {
        Object phrase = phraseClass.getDeclaredConstructor(String.class).newInstance(text);
        Object cell   = cellClass.getDeclaredConstructor(phraseClass).newInstance(phrase);
        tableClass.getMethod("addCell", cellClass).invoke(table, cell);
    }

    private static void exportTextFallback(List<TimetableSlot> slots,
                                            List<String> classNames,
                                            File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("SCHEDULIX – Timetable Export");
            pw.println("=".repeat(70));
            pw.println();
            String fmt = "%-16s %-18s";
            StringBuilder headerLine = new StringBuilder(String.format(fmt, "Period", "Time"));
            for (String cn : classNames) headerLine.append(String.format(" %-22s", cn));
            pw.println(headerLine);
            pw.println("-".repeat(70));
            for (TimetableSlot slot : slots) {
                StringBuilder row = new StringBuilder(
                    String.format(fmt,
                        slot.getPeriod().replace("\n", "/"),
                        slot.getTime().replace("\n", "-")));
                for (int c = 0; c < slot.getClassCount(); c++) {
                    row.append(String.format(" %-22s", slot.getClassSlot(c).replace("\n", "/")));
                }
                pw.println(row);
            }
        }
    }

    // ───────────────────────────────────────────
    //  Helpers
    // ───────────────────────────────────────────
    private static void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private static void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
