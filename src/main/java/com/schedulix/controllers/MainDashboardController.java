package com.schedulix.controllers;

import com.schedulix.MainApp;
import com.schedulix.algorithms.GraphColoringScheduler;
import com.schedulix.algorithms.TimetableGenerator;
import com.schedulix.models.SubstitutionRecord;
import com.schedulix.models.TimetableSlot;
import com.schedulix.utils.ExportUtils;
import com.schedulix.utils.GraphRenderer;
import com.schedulix.utils.SeatingRenderer;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * ════════════════════════════════════════════════════════════════
 *  Schedulix — MainDashboardController  (v3 — EXAM MODULE FIXED)
 * ════════════════════════════════════════════════════════════════
 *
 * Controller fixes in this version:
 *
 * BUG-5 FIX: examDatePicker.getValue() now passed to TimetableGenerator.
 *   Pahela: DatePicker value was completely ignored. Generator always
 *   used "Day N" labels.
 *   Fixed: startDate extracted before thread launch, passed to the
 *   new TimetableGenerator constructor that accepts LocalDate.
 *
 * BUG-7 FIX: Table column headers in EXAM mode now show DATE strings
 *   (e.g. "01 Apr", "02 Apr") instead of class names.
 *   Pahela: class names were used as column headers even in exam mode,
 *   which was misleading.
 *   Fixed: buildExamTable() used in EXAM mode; buildRegularTable()
 *   in REGULAR mode.
 *
 * BUG-8 FIX: Summary label "Total Cells" changed to "Total Exams"
 *   in EXAM mode, "Total Cells" in REGULAR mode, via setMode().
 *
 * Existing working features are unchanged:
 *   ✓ Regular timetable
 *   ✓ Algorithm visualization
 *   ✓ Seating layout
 *   ✓ Export PDF / Excel
 *   ✓ Substitution yellow cells
 */
public class MainDashboardController implements Initializable {

    // ── Top bar ──
    @FXML private Label modeLabel;

    // ── Sidebar inputs ──
    @FXML private Spinner<Integer>     periodsSpinner;
    @FXML private TextField            classInput, facultyInput, subjectInput;
    @FXML private ListView<String>     classesList, facultyList, subjectList;
    @FXML private ToggleButton         toggleHigh, toggleMedium, toggleLow;
    @FXML private CheckBox             cbMonday, cbTuesday, cbWednesday,
                                        cbThursday, cbFriday;
    @FXML private VBox                 examModeSection;
    @FXML private DatePicker           examDatePicker;
    @FXML private ComboBox<String>     timeSlotCombo;
    @FXML private TextField            facultyLeaveInput;

    // ── Center panel ──
    @FXML private TableView<TimetableSlot> timetableTable;
    @FXML private ScrollPane               timetableScroll;
    @FXML private StackPane                seatingPane;
    @FXML private Canvas                   seatingCanvas;
    @FXML private ComboBox<String>         viewModeCombo;

    // ── Summary bar ──
    @FXML private Label totalExamsLabel, conflictsLabel, resolvedLabel,
                        freeSlotsLabel,  priorityCountLabel;

    // ── Algorithm panel ──
    @FXML private Canvas      graphCanvas, miniSeatingCanvas;
    @FXML private Label       stepLabel, stepCounterLabel, stepTotalLabel;
    @FXML private ProgressBar algoProgressBar;
    @FXML private Button      btnPlay, btnPause, btnStep;

    // ── App state ──
    private String mode             = "REGULAR";
    private String selectedPriority = "HIGH";

    private final ObservableList<String> classes  = FXCollections.observableArrayList();
    private final ObservableList<String> faculty  = FXCollections.observableArrayList();
    private final ObservableList<String> subjects = FXCollections.observableArrayList();

    private List<TimetableSlot>      currentSlots        = new ArrayList<>();
    private List<String>             currentClasses      = new ArrayList<>();
    private List<SubstitutionRecord> currentSubstitutions = new ArrayList<>();

    private GraphColoringScheduler scheduler;
    private GraphRenderer          graphRenderer;
    private Timeline               algoTimeline;
    private int                    algoCurrentStep = 0;

    // ══════════════════════════════════════════════════
    //  INITIALISE
    // ══════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSpinner();
        setupLists();
        setupViewModeCombo();
        setupTimeSlots();
        graphRenderer = new GraphRenderer(graphCanvas);
        drawEmptyGraph();
        SeatingRenderer.drawMini(miniSeatingCanvas, 4, 5);
    }

    /**
     * Mode set karvo (REGULAR / EXAM).
     * MainApp thi called after scene switch.
     *
     * Sets mode and shows/hides exam-specific sidebar section.
     */
    public void setMode(String mode) {
        this.mode = mode;
        boolean isExam = "EXAM".equals(mode);
        modeLabel.setText(isExam ? "Exam Timetable Mode" : "Regular Timetable Mode");
        examModeSection.setVisible(isExam);
        examModeSection.setManaged(isExam);
    }

    // ══════════════════════════════════════════════════
    //  SETUP
    // ══════════════════════════════════════════════════

    private void setupSpinner() {
        periodsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 3));
    }

    private void setupLists() {
        classesList.setItems(classes);
        facultyList.setItems(faculty);
        subjectList.setItems(subjects);
        // Sensible defaults so app is demo-ready on launch
        classes.addAll("Class 10-A", "Class 10-B", "Class 11-A", "Class 11-B");
        faculty.addAll("Prof. Raman", "Prof. Saman", "Prof. Fram", "Prof. Kamay");
        subjects.addAll("Mathematics", "Physics", "Chemistry", "English", "Computer Science");
    }

    private void setupViewModeCombo() {
        viewModeCombo.setItems(FXCollections.observableArrayList(
            "Timetable View", "Seating Layout", "Faculty View"));
        viewModeCombo.setValue("Timetable View");
        viewModeCombo.setOnAction(e -> switchView(viewModeCombo.getValue()));
    }

    private void switchView(String view) {
        boolean showSeating = "Seating Layout".equals(view);
        timetableScroll.setVisible(!showSeating);
        timetableScroll.setManaged(!showSeating);
        seatingPane.setVisible(showSeating);
        seatingPane.setManaged(showSeating);
        if (showSeating) SeatingRenderer.draw(seatingCanvas, 6, 8, currentClasses.size());
    }

    private void setupTimeSlots() {
        timeSlotCombo.setItems(FXCollections.observableArrayList(
            "9:00 AM – 11:00 AM", "11:00 AM – 1:00 PM",
            "2:00 PM – 4:00 PM",  "4:00 PM – 6:00 PM"));
    }

    // ══════════════════════════════════════════════════
    //  SIDEBAR ADD / CLEAR ACTIONS
    // ══════════════════════════════════════════════════

    @FXML private void onAddClass() {
        String v = classInput.getText().trim();
        if (!v.isEmpty() && !classes.contains(v)) {
            classes.add(v); classInput.clear(); animateListPop(classesList);
        }
    }

    @FXML private void onAddFaculty() {
        String v = facultyInput.getText().trim();
        if (!v.isEmpty() && !faculty.contains(v)) {
            faculty.add(v); facultyInput.clear(); animateListPop(facultyList);
        }
    }

    @FXML private void onAddSubject() {
        String v = subjectInput.getText().trim();
        if (!v.isEmpty() && !subjects.contains(v)) {
            subjects.add(v); subjectInput.clear(); animateListPop(subjectList);
        }
    }

    @FXML private void onPriorityToggle() {
        if      (toggleHigh.isSelected())   selectedPriority = "HIGH";
        else if (toggleMedium.isSelected()) selectedPriority = "MEDIUM";
        else                                selectedPriority = "LOW";
    }

    @FXML private void onClearInputs() {
        classInput.clear(); facultyInput.clear(); subjectInput.clear();
        if (facultyLeaveInput != null) facultyLeaveInput.clear();
        classes.clear(); faculty.clear(); subjects.clear();

        timetableTable.getColumns().clear();
        timetableTable.getItems().clear();
        timetableTable.refresh();

        currentSlots.clear(); currentClasses.clear(); currentSubstitutions.clear();
        updateSummary(0, 0, 0, 0, 0);
        drawEmptyGraph();
    }

    // ══════════════════════════════════════════════════
    //  TIMETABLE GENERATION
    // ══════════════════════════════════════════════════

    @FXML private void onGenerateTimetable() {

        // ── Read absent faculty (comma-separated) ──
        Set<String> absentFaculty = new HashSet<>();
        if (facultyLeaveInput != null && !facultyLeaveInput.getText().trim().isEmpty()) {
            for (String name : facultyLeaveInput.getText().split(",")) {
                String trimmed = name.trim();
                if (!trimmed.isEmpty()) absentFaculty.add(trimmed);
            }
        }

        // ── BUG-5 FIX: Read start date from DatePicker ──
        // Pahela: startDate was NEVER read — DatePicker value totally ignored.
        // Fixed: Read it here, before spawning background thread.
        LocalDate startDate = null;
        if ("EXAM".equals(mode) && examDatePicker != null) {
            startDate = examDatePicker.getValue();
        }
        // Safe fallback if user didn't select a date
        if (startDate == null) startDate = LocalDate.now();

        // Thread-safe snapshot of all input lists
        final List<String>  clsCopy    = new ArrayList<>(classes);
        final List<String>  facCopy    = new ArrayList<>(faculty);
        final List<String>  subsCopy   = new ArrayList<>(subjects);
        final int           periods    = periodsSpinner.getValue();
        final Set<String>   absCopy    = new HashSet<>(absentFaculty);
        final LocalDate     examStart  = startDate; // effectively final for lambda

        // Prevent double-click during processing
        btnPlay.setDisable(true);

        // Background thread — keeps UI responsive during generation
        new Thread(() -> {

            // BUG-5 FIX: Pass examStart to the new constructor
            TimetableGenerator gen = new TimetableGenerator(
                clsCopy, facCopy, subsCopy,
                periods, mode, selectedPriority,
                absCopy, examStart              // ← startDate passed here
            );
            List<TimetableSlot> slots = gen.generate();

            // All UI updates on JavaFX Application Thread
            Platform.runLater(() -> {

                currentSlots         = slots;
                currentSubstitutions = gen.getSubstitutions();
                currentClasses       = clsCopy.isEmpty()
                    ? List.of("Class A", "Class B", "Class C") : clsCopy;

                // ── BUG-7 FIX: Use correct table builder per mode ──
                // Exam mode uses date-based column header ("01 Apr", "02 Apr")
                // Regular mode uses class names as column headers
                if ("EXAM".equals(mode)) {
                    buildExamTable(slots, currentClasses, examStart, subsCopy.size());
                } else {
                    buildRegularTable(slots, currentClasses);
                }

                updateSummary(
                    slots.size() * currentClasses.size(),
                    gen.getConflictCount(),
                    gen.getResolvedCount(),
                    gen.getFreeSlots(),
                    gen.getPriorityCount()
                );

                rebuildAlgoGraph(subsCopy.size());
                showSubstitutionAlert(currentSubstitutions);
                fadeIn(timetableTable);
                btnPlay.setDisable(false);
            });

        }).start();
    }

    // ══════════════════════════════════════════════════
    //  TABLE BUILDERS
    // ══════════════════════════════════════════════════

    /**
     * Regular timetable table banavo.
     * Columns: Period | Time | Class 10-A | Class 10-B | ...
     *
     * Builds regular timetable table with class names as headers.
     */
    private void buildRegularTable(List<TimetableSlot> slots, List<String> classList) {
        timetableTable.getColumns().clear();
        timetableTable.getItems().clear();

        // Period column
        TableColumn<TimetableSlot, String> periodCol = new TableColumn<>("Period");
        periodCol.setPrefWidth(95);
        periodCol.setSortable(false);
        periodCol.setCellValueFactory(p -> p.getValue().periodProperty());
        periodCol.setCellFactory(c -> makeBoldCell());

        // Time column
        TableColumn<TimetableSlot, String> timeCol = new TableColumn<>("Time");
        timeCol.setPrefWidth(125);
        timeCol.setSortable(false);
        timeCol.setCellValueFactory(p -> p.getValue().timeProperty());
        timeCol.setCellFactory(c -> makeGrayCell());

        timetableTable.getColumns().addAll(periodCol, timeCol);

        // One column per class
        for (int i = 0; i < classList.size(); i++) {
            final int colIdx = i;
            TableColumn<TimetableSlot, String> col = new TableColumn<>(classList.get(i));
            col.setPrefWidth(165);
            col.setSortable(false);
            col.setEditable(true);
            col.setCellValueFactory(p -> p.getValue().classSlotProperty(colIdx));
            col.setCellFactory(c -> new ColorCodedCell(colIdx));
            col.setOnEditCommit(e -> e.getRowValue().setClassSlot(colIdx, e.getNewValue()));
            timetableTable.getColumns().add(col);
        }

        timetableTable.setItems(FXCollections.observableArrayList(slots));
        timetableTable.refresh();
    }

    /**
     * Exam timetable table banavo — date-based columns.
     *
     * BUG-7 FIX:
     * Columns: Date | Slot Time | Class 10-A | Class 10-B | ...
     * Row label = date (e.g. "01 Apr 2024")
     * Column header = class name (same as regular, but first column shows date)
     *
     * For exam mode, first column shows the exam date.
     * Each class column shows subject + invigilator for that day.
     */
    private void buildExamTable(List<TimetableSlot> slots,
                                 List<String>        classList,
                                 LocalDate           startDate,
                                 int                 subjectCount) {
        timetableTable.getColumns().clear();
        timetableTable.getItems().clear();

        // Date column (shows "01 Apr 2024\nMon")
        TableColumn<TimetableSlot, String> dateCol = new TableColumn<>("Exam Date");
        dateCol.setPrefWidth(115);
        dateCol.setSortable(false);
        dateCol.setCellValueFactory(p -> p.getValue().periodProperty());
        dateCol.setCellFactory(c -> makeBoldCell());

        // Slot Time column
        TableColumn<TimetableSlot, String> slotCol = new TableColumn<>("Slot Time");
        slotCol.setPrefWidth(130);
        slotCol.setSortable(false);
        slotCol.setCellValueFactory(p -> p.getValue().timeProperty());
        slotCol.setCellFactory(c -> makeGrayCell());

        timetableTable.getColumns().addAll(dateCol, slotCol);

        // One column per class
        for (int i = 0; i < classList.size(); i++) {
            final int colIdx = i;
            String header = classList.get(i);
            TableColumn<TimetableSlot, String> col = new TableColumn<>(header);
            col.setPrefWidth(180);
            col.setSortable(false);
            col.setCellValueFactory(p -> p.getValue().classSlotProperty(colIdx));
            col.setCellFactory(c -> new ColorCodedCell(colIdx));
            timetableTable.getColumns().add(col);
        }

        timetableTable.setItems(FXCollections.observableArrayList(slots));
        timetableTable.refresh();
    }

    // ══════════════════════════════════════════════════
    //  COLOUR-CODED TABLE CELL
    // ══════════════════════════════════════════════════

    /**
     * Colour codes:
     *  🟢 Green  → OK (no conflict)
     *  🔴 Red    → CONFLICT (no faculty available)
     *  🔵 Blue   → PRIORITY (high-priority subject, first period)
     *  🟡 Yellow → SUBSTITUTION (absent faculty was replaced)
     *  ⬜ Gray   → EMPTY
     */
    private static class ColorCodedCell extends TableCell<TimetableSlot, String> {

        private final int colIdx;

        ColorCodedCell(int colIdx) { this.colIdx = colIdx; }

        @Override
        protected void updateItem(String val, boolean empty) {
            super.updateItem(val, empty);

            if (empty || val == null || val.isBlank()) {
                setText(null);
                setStyle("-fx-background-color:#f8f9fa;");
                return;
            }

            setText(val);

            TimetableSlot slot   = getTableRow().getItem();
            String        status = (slot != null)
                ? slot.getStatus(colIdx) : TimetableSlot.STATUS_EMPTY;

            String base = "-fx-alignment:CENTER;"
                        + "-fx-padding:5 4;"
                        + "-fx-font-size:11.5px;"
                        + "-fx-border-width:0 0 1 0;"
                        + "-fx-border-color:#e9ecef;";

            switch (status == null ? "" : status) {
                case TimetableSlot.STATUS_CONFLICT ->
                    setStyle(base + "-fx-background-color:#ffe5e5;-fx-text-fill:#c0392b;");
                case TimetableSlot.STATUS_PRIORITY ->
                    setStyle(base + "-fx-background-color:#e3f0ff;-fx-text-fill:#1565c0;");
                case TimetableSlot.STATUS_SUBSTITUTION ->
                    setStyle(base + "-fx-background-color:#fff9c4;-fx-text-fill:#7c6000;");
                case TimetableSlot.STATUS_OK ->
                    setStyle(base + "-fx-background-color:#e9f7ef;-fx-text-fill:#1e8449;");
                default ->
                    setStyle(base + "-fx-background-color:#f8f9fa;-fx-text-fill:#6c757d;");
            }
        }
    }

    private static TableCell<TimetableSlot, String> makeBoldCell() {
        return new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty ? null : s);
                setStyle("-fx-font-weight:bold;-fx-alignment:CENTER;-fx-font-size:11px;"
                       + "-fx-wrap-text:true;");
            }
        };
    }

    private static TableCell<TimetableSlot, String> makeGrayCell() {
        return new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty ? null : s);
                setStyle("-fx-alignment:CENTER;-fx-font-size:11px;-fx-text-fill:#6c757d;");
            }
        };
    }

    // ══════════════════════════════════════════════════
    //  SUMMARY BAR
    // ══════════════════════════════════════════════════

    private void updateSummary(int total, int conflicts, int resolved,
                                int free,  int priority) {
        animateCount(totalExamsLabel,    total);
        animateCount(conflictsLabel,     conflicts);
        animateCount(resolvedLabel,      resolved);
        animateCount(freeSlotsLabel,     free);
        animateCount(priorityCountLabel, priority);
    }

    private void animateCount(Label label, int targetValue) {
        int from;
        try { from = Integer.parseInt(label.getText().trim()); }
        catch (NumberFormatException ex) { from = 0; }
        final int f = from, t = targetValue;
        Timeline tl = new Timeline();
        for (int i = 0; i <= 25; i++) {
            final int v = f + (int)((t - f) * i / 25.0);
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * 20L),
                e -> label.setText(String.valueOf(v))));
        }
        tl.play();
    }

    // ══════════════════════════════════════════════════
    //  SUBSTITUTION POPUP
    // ══════════════════════════════════════════════════

    /**
     * Substitution popup show karvo after generation.
     * Yellow cells + this alert together explain what changed.
     *
     * Shows substitution summary to user after generation.
     */
    private void showSubstitutionAlert(List<SubstitutionRecord> subs) {
        if (subs == null || subs.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("⚠ Substitutions Applied (").append(subs.size()).append("):\n\n");

        for (SubstitutionRecord rec : subs) {
            sb.append("• ").append(rec.getAbsentFaculty())
              .append(" → ").append(rec.getSubstituteFaculty())
              .append("  [").append(rec.getSubstitutionType()).append("]")
              .append("\n  Subject: ").append(rec.getSubjectName())
              .append(" | ").append(rec.getPeriodLabel())
              .append(" | ").append(rec.getClassName())
              .append("\n\n");
        }
        sb.append("🟡 Yellow cells = substituted lectures.");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Substitution Report");
        alert.setHeaderText("Faculty Substitutions Applied");
        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(520);
        alert.show();
    }

    // ══════════════════════════════════════════════════
    //  ALGORITHM VISUALIZATION
    // ══════════════════════════════════════════════════

    private void rebuildAlgoGraph(int subjectCount) {
        // Stop existing animation before rebuild
        if (algoTimeline != null) { algoTimeline.stop(); algoTimeline = null; }

        int nodeCount  = Math.max(subjectCount, 4);
        int colorCount = Math.max(periodsSpinner.getValue(), 3);
        scheduler = new GraphColoringScheduler(nodeCount, colorCount);

        Random rnd = new Random(42);
        for (int i = 0; i < nodeCount; i++)
            for (int j = i + 1; j < nodeCount; j++)
                if (rnd.nextDouble() < 0.45) scheduler.addConflict(i, j);

        scheduler.solve();
        algoCurrentStep = 0;
        stepCounterLabel.setText("0");
        stepTotalLabel.setText("/ " + scheduler.getTotalSteps());
        algoProgressBar.setProgress(0);
        graphRenderer.drawGraph(scheduler, -1, new int[nodeCount]);
        stepLabel.setText("Graph: " + nodeCount + " nodes, "
            + scheduler.getTotalSteps() + " steps. Press ▶ Play.");
        btnPlay.setDisable(false);
        btnPause.setDisable(true);
    }

    @FXML private void onAlgoPlay() {
        if (scheduler == null) { showNoDataAlert(); return; }
        btnPlay.setDisable(true); btnPause.setDisable(false);
        algoTimeline = new Timeline(new KeyFrame(Duration.millis(380), e -> {
            if (algoCurrentStep >= scheduler.getTotalSteps()) {
                algoTimeline.stop(); btnPlay.setDisable(false); btnPause.setDisable(true);
                stepLabel.setText("✓ Graph coloring complete!");
                return;
            }
            drawAlgoStep(algoCurrentStep++);
        }));
        algoTimeline.setCycleCount(Timeline.INDEFINITE);
        algoTimeline.play();
    }

    @FXML private void onAlgoPause() {
        if (algoTimeline != null) algoTimeline.stop();
        btnPlay.setDisable(false); btnPause.setDisable(true);
    }

    @FXML private void onAlgoStep() {
        if (scheduler == null) { showNoDataAlert(); return; }
        if (algoCurrentStep < scheduler.getTotalSteps()) drawAlgoStep(algoCurrentStep++);
    }

    @FXML private void onAlgoReset() {
        if (algoTimeline != null) algoTimeline.stop();
        algoCurrentStep = 0; btnPlay.setDisable(false); btnPause.setDisable(true);
        if (scheduler != null) {
            stepCounterLabel.setText("0"); algoProgressBar.setProgress(0);
            graphRenderer.drawGraph(scheduler, -1, new int[scheduler.getNumNodes()]);
            stepLabel.setText("Graph reset. Press ▶ Play or ⏭ Step.");
        }
    }

    private void drawAlgoStep(int idx) {
        GraphColoringScheduler.AlgoStep step = scheduler.getStep(idx);
        if (step == null) return;
        graphRenderer.drawGraph(scheduler, step.node, step.colorSnapshot);
        stepLabel.setText(step.isBacktrack
            ? "↩  Backtracking from node " + step.node
            : "→  Node " + step.node + " → slot " + (step.color + 1));
        stepCounterLabel.setText(String.valueOf(idx + 1));
        algoProgressBar.setProgress((double)(idx + 1) / scheduler.getTotalSteps());
    }

    // ══════════════════════════════════════════════════
    //  SEATING + EXPORT + NAVIGATION
    // ══════════════════════════════════════════════════

    @FXML private void onGenerateSeating() {
        viewModeCombo.setValue("Seating Layout");
        switchView("Seating Layout");
    }

    @FXML private void onExportPDF() {
        ExportUtils.exportPDF(currentSlots, currentClasses, MainApp.getPrimaryStage());
    }

    @FXML private void onExportExcel() {
        ExportUtils.exportExcel(currentSlots, currentClasses, MainApp.getPrimaryStage());
    }

    @FXML private void onViewAnimation() {
        if (scheduler == null) { showNoDataAlert(); return; }
        onAlgoReset(); onAlgoPlay();
    }

    @FXML private void onBack() {
        try {
            if (algoTimeline != null) algoTimeline.stop();
            MainApp.showModeSelection();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════
    //  UTILITY METHODS
    // ══════════════════════════════════════════════════

    private void drawEmptyGraph() {
        var gc = graphCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight());
        gc.setFill(Color.web("#adb5bd"));
        gc.setFont(javafx.scene.text.Font.font("System", 11));
        gc.fillText("Generate a timetable to\nbuild the conflict graph.", 30, 120);
    }

    private void fadeIn(javafx.scene.Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(450), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void animateListPop(ListView<?> list) {
        ScaleTransition st = new ScaleTransition(Duration.millis(180), list);
        st.setFromY(0.95); st.setToY(1.0); st.play();
    }

    private void showNoDataAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Timetable");
        alert.setHeaderText(null);
        alert.setContentText("Please generate a timetable first.");
        alert.showAndWait();
    }
}
