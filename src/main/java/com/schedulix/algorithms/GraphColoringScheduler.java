package com.schedulix.algorithms;

import java.util.*;

/**
 * Graph Coloring based Timetable Scheduler
 * Uses: Backtracking + Greedy Heuristics + CSP
 */
public class GraphColoringScheduler {

    // Graph represented as adjacency list
    private final int numNodes;
    private final List<List<Integer>> adjacencyList;
    private final int[] colorAssignment;
    private final int numColors;

    // Animation steps
    private final List<AlgoStep> steps = new ArrayList<>();
    private int currentStep = 0;

    public GraphColoringScheduler(int numNodes, int numColors) {
        this.numNodes = numNodes;
        this.numColors = numColors;
        this.adjacencyList = new ArrayList<>();
        this.colorAssignment = new int[numNodes];
        Arrays.fill(colorAssignment, -1);
        for (int i = 0; i < numNodes; i++) adjacencyList.add(new ArrayList<>());
    }

    /** Add conflict edge between two nodes */
    public void addConflict(int u, int v) {
        if (!adjacencyList.get(u).contains(v)) adjacencyList.get(u).add(v);
        if (!adjacencyList.get(v).contains(u)) adjacencyList.get(v).add(u);
    }

    /** Run graph coloring with backtracking, recording animation steps */
    public boolean solve() {
        steps.clear();
        currentStep = 0;
        Arrays.fill(colorAssignment, -1);
        return backtrack(0);
    }

    private boolean backtrack(int node) {
        if (node == numNodes) return true;

        // Greedy: try least-constraining color first
        List<Integer> available = getAvailableColors(node);

        for (int color : available) {
            colorAssignment[node] = color;
            steps.add(new AlgoStep(node, color, false, copyColors()));

            if (backtrack(node + 1)) return true;

            // Backtrack
            colorAssignment[node] = -1;
            steps.add(new AlgoStep(node, -1, true, copyColors())); // backtrack step
        }
        return false;
    }

    private List<Integer> getAvailableColors(int node) {
        Set<Integer> usedColors = new HashSet<>();
        for (int neighbor : adjacencyList.get(node)) {
            if (colorAssignment[neighbor] != -1) usedColors.add(colorAssignment[neighbor]);
        }
        List<Integer> available = new ArrayList<>();
        for (int c = 0; c < numColors; c++) {
            if (!usedColors.contains(c)) available.add(c);
        }
        return available;
    }

    private int[] copyColors() {
        return Arrays.copyOf(colorAssignment, colorAssignment.length);
    }

    public boolean hasConflict(int u, int v) {
        return adjacencyList.get(u).contains(v);
    }

    public List<Integer> getNeighbors(int node) {
        return adjacencyList.get(node);
    }

    public int getColor(int node) { return colorAssignment[node]; }
    public int[] getAllColors()   { return colorAssignment; }
    public List<AlgoStep> getSteps() { return steps; }
    public int getTotalSteps() { return steps.size(); }
    public int getCurrentStep() { return currentStep; }
    public void setCurrentStep(int s) { currentStep = s; }

    public AlgoStep getStep(int idx) {
        if (idx < 0 || idx >= steps.size()) return null;
        return steps.get(idx);
    }

    public int getNumNodes() { return numNodes; }
    public int getNumColors() { return numColors; }

    /** Represents one animation step */
    public static class AlgoStep {
        public final int node;
        public final int color;
        public final boolean isBacktrack;
        public final int[] colorSnapshot;

        public AlgoStep(int node, int color, boolean isBacktrack, int[] snapshot) {
            this.node = node;
            this.color = color;
            this.isBacktrack = isBacktrack;
            this.colorSnapshot = snapshot;
        }
    }
}
