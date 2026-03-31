package com.schedulix.utils;

import com.schedulix.algorithms.GraphColoringScheduler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Renders the graph coloring animation on a JavaFX Canvas.
 */
public class GraphRenderer {

    private final Canvas canvas;

    // Slot colors matching the UI palette
    private static final Color[] SLOT_COLORS = {
        Color.web("#4dabf7"), // blue
        Color.web("#51cf66"), // green
        Color.web("#ff8787"), // red
        Color.web("#ffd43b"), // yellow
        Color.web("#cc5de8"), // purple
        Color.web("#ff922b"), // orange
        Color.web("#20c997"), // teal
        Color.web("#f783ac"), // pink
    };

    public GraphRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void drawGraph(GraphColoringScheduler scheduler, int activeNode, int[] colorSnapshot) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        int n = scheduler.getNumNodes();

        // Clear
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRoundRect(0, 0, w, h, 12, 12);

        // Node positions in a circle
        double cx = w / 2, cy = h / 2 - 10;
        double radius = Math.min(w, h) * 0.36;
        double[] nx = new double[n];
        double[] ny = new double[n];
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            nx[i] = cx + radius * Math.cos(angle);
            ny[i] = cy + radius * Math.sin(angle);
        }

        // Draw edges
        for (int i = 0; i < n; i++) {
            List<Integer> neighbors = scheduler.getNeighbors(i);
            for (int j : neighbors) {
                if (j > i) {
                    boolean conflict = colorSnapshot[i] != -1 && colorSnapshot[j] != -1
                        && colorSnapshot[i] == colorSnapshot[j];
                    gc.setStroke(conflict ? Color.web("#ff6b6b") : Color.web("#ced4da"));
                    gc.setLineWidth(conflict ? 2.5 : 1.2);
                    if (conflict) {
                        gc.setLineDashes(6, 4);
                    } else {
                        gc.setLineDashes();
                    }
                    gc.strokeLine(nx[i], ny[i], nx[j], ny[j]);
                }
            }
        }
        gc.setLineDashes();

        // Draw nodes
        double nodeR = 22;
        for (int i = 0; i < n; i++) {
            Color nodeColor;
            if (colorSnapshot[i] >= 0 && colorSnapshot[i] < SLOT_COLORS.length) {
                nodeColor = SLOT_COLORS[colorSnapshot[i]];
            } else {
                nodeColor = Color.web("#dee2e6");
            }

            boolean isActive = (i == activeNode);

            // Shadow for active node
            if (isActive) {
                gc.setFill(Color.web("#339af080"));
                gc.fillOval(nx[i] - nodeR - 5, ny[i] - nodeR - 5, (nodeR + 5) * 2, (nodeR + 5) * 2);
            }

            // Node fill
            gc.setFill(nodeColor);
            gc.fillOval(nx[i] - nodeR, ny[i] - nodeR, nodeR * 2, nodeR * 2);

            // Node border
            gc.setStroke(isActive ? Color.web("#1971c2") : Color.web("#868e96"));
            gc.setLineWidth(isActive ? 2.5 : 1.2);
            gc.strokeOval(nx[i] - nodeR, ny[i] - nodeR, nodeR * 2, nodeR * 2);

            // Node label
            gc.setFill(colorSnapshot[i] >= 0 ? Color.WHITE : Color.web("#495057"));
            gc.setFont(Font.font("System", FontWeight.BOLD, 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("N" + i, nx[i], ny[i] + 4);
        }

        // Legend
        gc.setFont(Font.font("System", 10));
        gc.setFill(Color.web("#868e96"));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Nodes = Subjects/Exams", 6, h - 22);
        gc.fillText("Edges = Conflicts", 6, h - 10);
    }
}
