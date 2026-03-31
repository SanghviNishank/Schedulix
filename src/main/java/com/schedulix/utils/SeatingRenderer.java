package com.schedulix.utils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Random;

/**
 * Renders a seating arrangement grid on a JavaFX Canvas.
 */
public class SeatingRenderer {

    private static final Color[] CLASS_COLORS = {
        Color.web("#e3f2fd"), Color.web("#e8f5e9"), Color.web("#fff3e0"),
        Color.web("#fce4ec"), Color.web("#ede7f6"), Color.web("#e0f7fa"),
    };
    private static final Color[] CLASS_BORDER = {
        Color.web("#64b5f6"), Color.web("#81c784"), Color.web("#ffb74d"),
        Color.web("#f48fb1"), Color.web("#ce93d8"), Color.web("#4dd0e1"),
    };

    public static void draw(Canvas canvas, int rows, int cols, int classCount) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, w, h);

        double padTop = 30, padLeft = 40;
        double cellW = (w - padLeft - 20) / cols;
        double cellH = (h - padTop - 20) / rows;

        Random rnd = new Random(7);
        int numClasses = Math.max(1, classCount);

        // Column headers
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setFill(Color.web("#495057"));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int c = 0; c < cols; c++) {
            gc.fillText(String.valueOf((char)('A' + c)),
                padLeft + c * cellW + cellW / 2, padTop - 10);
        }
        // Row headers
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int r = 0; r < rows; r++) {
            gc.fillText(String.valueOf(r + 1),
                padLeft - 8, padTop + r * cellH + cellH / 2 + 4);
        }

        // Seats
        gc.setFont(Font.font("System", 9));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int classIdx = rnd.nextInt(numClasses) % CLASS_COLORS.length;
                double x = padLeft + c * cellW;
                double y = padTop + r * cellH;

                // Seat background
                gc.setFill(CLASS_COLORS[classIdx]);
                gc.fillRoundRect(x + 2, y + 2, cellW - 4, cellH - 4, 6, 6);

                // Seat border
                gc.setStroke(CLASS_BORDER[classIdx]);
                gc.setLineWidth(1.2);
                gc.strokeRoundRect(x + 2, y + 2, cellW - 4, cellH - 4, 6, 6);

                // Seat label
                gc.setFill(Color.web("#495057"));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("S" + (r * cols + c + 1), x + cellW / 2, y + cellH / 2 + 3);
            }
        }

        // Legend
        gc.setFont(Font.font("System", 10));
        gc.setFill(Color.web("#868e96"));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Colors = different classes  |  Grid = Row × Column seating", 40, h - 8);
    }

    public static void drawMini(Canvas canvas, int rows, int cols) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#f0f4f8"));
        gc.fillRoundRect(0, 0, w, h, 8, 8);

        double cellW = (w - 10) / cols;
        double cellH = (h - 10) / rows;
        Random rnd = new Random(13);
        Color[] colors = { Color.web("#d0ebff"), Color.web("#d3f9d8"), Color.web("#fff3bf"), Color.web("#ffd6e0") };

        gc.setFont(Font.font("System", 8));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = 5 + c * cellW;
                double y = 5 + r * cellH;
                gc.setFill(colors[rnd.nextInt(colors.length)]);
                gc.fillRoundRect(x, y, cellW - 2, cellH - 2, 4, 4);
                gc.setStroke(Color.web("#ced4da"));
                gc.setLineWidth(0.8);
                gc.strokeRoundRect(x, y, cellW - 2, cellH - 2, 4, 4);
                gc.setFill(Color.web("#495057"));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("" + (r * cols + c + 1), x + cellW / 2, y + cellH / 2 + 3);
            }
        }
    }
}
