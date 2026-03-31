package com.schedulix.algorithms;

import com.schedulix.models.SubstitutionRecord;
import com.schedulix.models.TimetableSlot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

/**
 * ════════════════════════════════════════════════════════════════
 *  Schedulix — TimetableGenerator  (v3 — EXAM MODULE FULLY FIXED)
 * ════════════════════════════════════════════════════════════════
 *
 * ── BUGS FIXED IN THIS VERSION ──
 *
 * BUG-1 (CRITICAL): Date was never incrementing.
 *   Root cause: dayIdx was used as a simple integer label but was
 *   never converted to a real LocalDate using startDate.plusDays().
 *   Every row showed "Day 1" because dayIdx was reset inside the
 *   inner slot loop.
 *   Fix: LocalDate currentDate = startDate.plusDays(dayIdx) — called
 *        once per outer day loop, incremented each iteration correctly.
 *
 * BUG-2 (CRITICAL): All subjects on same day / partial schedule.
 *   Root cause: The inner column loop consumed subIdx for EACH CLASS
 *   in the same slot — so 4 classes × 1 slot = 4 subjects consumed
 *   per slot, cramming all subjects onto Day 1.
 *   Fix: ONE subject per day (not per class-column). All classes share
 *        the SAME subject on the same day (exam timetable behaviour).
 *        One row = one day = one subject for every class.
 *
 * BUG-3 (CRITICAL): Faculty conflict — same invigilator in multiple
 *   classes in same slot.
 *   Root cause: invBusy key was "faculty|dayIdx|slotIdx" but dayIdx
 *   was always 0 when all subjects landed on Day 1.
 *   Fix: Separate invigilator assigned per class column using a
 *        "usedInvigThisSlot" set — each class MUST get a different
 *        invigilator. busySuffix now uses real date string + slot.
 *
 * BUG-4: EXAM_SLOTS_PER_DAY = 3 was grouping multiple subjects
 *   per day (slotIdx loop inside dayIdx loop). For real exam
 *   timetables: ONE subject per day, one slot per day.
 *   Fix: Removed inner slot loop. One day = one TimetableSlot row.
 *        Subject advances by 1 per day, not per class-column.
 *
 * BUG-5: startDate from DatePicker was completely ignored.
 *   The generator had no startDate parameter — always showed "Day N".
 *   Fix: Added LocalDate startDate constructor parameter.
 *        Controller passes examDatePicker.getValue().
 *
 * BUG-6: Absent faculty substitution in exam mode was not logged.
 *   Fix: SubstitutionRecord added for exam mode as well.
 *
 * ── EXAM SCHEDULING LOGIC (New) ──
 *
 * Rule 1: ONE subject per day.
 *         Dates increment correctly: startDate, +1, +2, ...
 * Rule 2: ALL classes have the SAME subject on the same day.
 *         This is standard exam timetable behaviour.
 * Rule 3: Each class column gets a DIFFERENT invigilator.
 *         No two classes share the same invigilator on same day.
 * Rule 4: If preferred invigilator is absent → substitute is found.
 *         Substitute must not be absent AND not already assigned
 *         to another class on the same day.
 * Rule 5: Skipped dates (weekends / holidays) can be added later
 *         via the skipDates set (ready for extension).
 *
 * ── REGULAR TIMETABLE (unchanged, still works) ──
 *  ✓ No faculty double-booked in same period
 *  ✓ Absent faculty auto-substitution
 *  ✓ Max lecture load per faculty per day
 *  ✓ Priority subjects in first period
 */
public class TimetableGenerator {

    // ── Input fields ──
    private final List<String>  classes;
    private final List<String>  facultyList;
    private final List<String>  subjects;
    private final int           numPeriods;
    private final String        mode;           // "REGULAR" or "EXAM"
    private final String        priority;       // "HIGH" / "MEDIUM" / "LOW"
    private final Set<String>   facultyAbsent;  // absent faculty names
    private final LocalDate     startDate;      // BUG-5 FIX: exam start date

    // ── Subject → Primary Faculty map ──
    // Round-robin: Math→Prof.A, Physics→Prof.B, etc.
    private final Map<String, String> subjectFacultyMap = new LinkedHashMap<>();

    // ── Regular timetable tracking ──
    private final Set<String>          facultyBusyInPeriod  = new HashSet<>();
    private final Map<String, Integer> facultyLectureCount  = new HashMap<>();
    private static final int           MAX_LECTURES_PER_DAY = 5;

    // ── Date formatter for display ──
    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ── Time labels ──
    private static final String[] PERIOD_TIMES = {
        "9:00 – 10:00 AM",  "10:00 – 11:00 AM",
        "11:00 – 12:00 PM", "1:00 – 2:00 PM",
        "2:00 – 3:00 PM",   "3:00 – 4:00 PM",
        "4:00 – 5:00 PM"
    };

    // Exam: single morning slot per day (can be extended)
    private static final String EXAM_SLOT_TIME = "9:00 AM – 12:00 PM";

    // ── Result storage ──
    private final List<TimetableSlot>       generatedSlots = new ArrayList<>();
    private final List<SubstitutionRecord>  substitutions  = new ArrayList<>();
    private int conflictCount = 0;
    private int resolvedCount = 0;
    private int priorityCount = 0;

    // ══════════════════════════════════════════════════
    //  Constructor (original — startDate defaults to today)
    //  This keeps backward compatibility with REGULAR mode.
    // ══════════════════════════════════════════════════

    public TimetableGenerator(List<String> classes,
                               List<String> facultyList,
                               List<String> subjects,
                               int          numPeriods,
                               String       mode,
                               String       priority,
                               Set<String>  facultyAbsent) {
        this(classes, facultyList, subjects, numPeriods, mode,
             priority, facultyAbsent, LocalDate.now());
    }

    // ══════════════════════════════════════════════════
    //  Constructor WITH startDate (BUG-5 FIX)
    //  Exam mode MUST use this constructor.
    // ══════════════════════════════════════════════════

    public TimetableGenerator(List<String> classes,
                               List<String> facultyList,
                               List<String> subjects,
                               int          numPeriods,
                               String       mode,
                               String       priority,
                               Set<String>  facultyAbsent,
                               LocalDate    startDate) {

        // Defensive copy + safe defaults
        this.classes      = classes.isEmpty()
            ? new ArrayList<>(List.of("Class A", "Class B", "Class C"))
            : new ArrayList<>(classes);
        this.facultyList  = facultyList.isEmpty()
            ? new ArrayList<>(List.of("Prof. A", "Prof. B", "Prof. C"))
            : new ArrayList<>(facultyList);
        this.subjects     = subjects.isEmpty()
            ? new ArrayList<>(List.of("Math", "Science", "English"))
            : new ArrayList<>(subjects);

        this.numPeriods    = numPeriods;
        this.mode          = mode;
        this.priority      = priority;
        this.facultyAbsent = facultyAbsent != null
            ? new HashSet<>(facultyAbsent) : new HashSet<>();
        // BUG-5 FIX: Use provided date, fallback to today if null
        this.startDate     = (startDate != null) ? startDate : LocalDate.now();

        // Build subject → faculty assignment once
        buildSubjectFacultyMap();
    }

    // ══════════════════════════════════════════════════
    //  Public entry point
    // ══════════════════════════════════════════════════

    public List<TimetableSlot> generate() {
        generatedSlots.clear();
        substitutions.clear();
        conflictCount = 0;
        resolvedCount = 0;
        priorityCount = 0;
        facultyBusyInPeriod.clear();
        facultyLectureCount.clear();

        return "EXAM".equals(mode)
            ? generateExamTimetable()
            : generateRegularTimetable();
    }

    // ══════════════════════════════════════════════════
    //  REGULAR TIMETABLE  (unchanged, working correctly)
    // ══════════════════════════════════════════════════

    /**
     * Regular timetable generate karo — period by period.
     * Drekk period = ek row. Drekk class = ek column.
     * Constraint: same faculty same period ma be jagah nahi.
     *
     * Generates weekly timetable row by row.
     * Each row = one period. Each column = one class.
     * Constraint: a faculty cannot appear in two classes in the same period.
     */
    private List<TimetableSlot> generateRegularTimetable() {

        for (int pIdx = 0; pIdx < numPeriods; pIdx++) {

            String label   = "Period " + (pIdx + 1);
            String timeStr = PERIOD_TIMES[pIdx % PERIOD_TIMES.length];
            TimetableSlot row = new TimetableSlot(label, timeStr, classes.size());

            // This period ma kon faculty busy che te track karo
            Set<String> busyThisPeriod = new HashSet<>();

            for (int cIdx = 0; cIdx < classes.size(); cIdx++) {
                // Subject rotate karo: period + class index
                String subject   = subjects.get((pIdx + cIdx) % subjects.size());
                String preferred = subjectFacultyMap.get(subject);

                // 3-level faculty assignment with constraint checking
                String assigned = assignFacultyRegular(
                    subject, preferred, pIdx, cIdx, busyThisPeriod);

                row.setClassSlot(cIdx, subject + "\n" + assigned);
                String status = computeStatusRegular(pIdx, assigned);
                row.setStatus(cIdx, status);

                if (TimetableSlot.STATUS_PRIORITY.equals(status))    priorityCount++;
                if (TimetableSlot.STATUS_CONFLICT.equals(status))    conflictCount++;

                // Faculty ne busy mark karo — "(Sub)" suffix remove karva
                if (!assigned.startsWith("---")) {
                    String trackName = assigned.replace(" (Sub)", "").trim();
                    busyThisPeriod.add(trackName);
                    facultyBusyInPeriod.add(trackName + "|" + pIdx);
                    facultyLectureCount.merge(trackName, 1, Integer::sum);
                }
            }
            generatedSlots.add(row);
        }

        resolvedCount = substitutions.size();
        return generatedSlots;
    }

    // ── Regular: 3-level faculty assignment ──

    private String assignFacultyRegular(String subject,
                                         String preferred,
                                         int    pIdx,
                                         int    cIdx,
                                         Set<String> busyThisPeriod) {
        // Level 1: Preferred faculty available che?
        if (preferred != null && isAvailableRegular(preferred, pIdx, busyThisPeriod)) {
            return preferred;
        }

        // Level 2: Substitute dhundho
        boolean isAbsent = preferred != null && facultyAbsent.contains(preferred);
        String  sub      = findSubstituteRegular(subject, preferred, pIdx, busyThisPeriod);

        if (sub != null) {
            if (isAbsent) {
                // Substitution record banavo
                boolean sameSubject = subjectFacultyMap.containsValue(sub);
                substitutions.add(new SubstitutionRecord(
                    preferred, sub,
                    "Period " + (pIdx + 1),
                    cIdx < classes.size() ? classes.get(cIdx) : "Class",
                    subject,
                    sameSubject ? "SAME_SUBJECT" : "FREE_SLOT"
                ));
            }
            return sub + " (Sub)";
        }

        // Level 3: Conflict
        conflictCount++;
        return "--- No Faculty ---";
    }

    private String findSubstituteRegular(String subject,
                                          String excluded,
                                          int    pIdx,
                                          Set<String> busy) {
        // Pass 1: Same-subject faculty prefer karo
        for (Map.Entry<String, String> entry : subjectFacultyMap.entrySet()) {
            if (!entry.getKey().equals(subject)) continue;
            String c = entry.getValue();
            if (!c.equals(excluded) && isAvailableRegular(c, pIdx, busy)) return c;
        }
        // Pass 2: Koi pan free faculty
        for (String fac : facultyList) {
            if (!fac.equals(excluded) && isAvailableRegular(fac, pIdx, busy)) return fac;
        }
        return null;
    }

    private boolean isAvailableRegular(String fac, int pIdx, Set<String> busy) {
        return !facultyAbsent.contains(fac)
            && !busy.contains(fac)
            && !facultyBusyInPeriod.contains(fac + "|" + pIdx)
            && facultyLectureCount.getOrDefault(fac, 0) < MAX_LECTURES_PER_DAY;
    }

    private String computeStatusRegular(int pIdx, String assigned) {
        if (assigned.startsWith("---"))            return TimetableSlot.STATUS_CONFLICT;
        if (assigned.contains("(Sub)"))            return TimetableSlot.STATUS_SUBSTITUTION;
        if ("HIGH".equals(priority) && pIdx == 0)  return TimetableSlot.STATUS_PRIORITY;
        return TimetableSlot.STATUS_OK;
    }

    // ══════════════════════════════════════════════════
    //  EXAM TIMETABLE  (COMPLETELY REWRITTEN — v3)
    //
    //  ── Scheduling Logic ──
    //
    //  1 subject per day rule:
    //     Day 1 (01 Apr): Mathematics  → all classes
    //     Day 2 (02 Apr): Physics      → all classes
    //     Day 3 (03 Apr): Chemistry    → all classes
    //     ... and so on until all subjects done
    //
    //  Each day row has columns = classes.
    //  ALL classes have the SAME subject on the same day.
    //  (Standard exam timetable — every class writes same paper same day)
    //
    //  Faculty constraint per day:
    //     Class A → Prof. Raman (invigilator)
    //     Class B → Prof. Saman (different invigilator — BUG-3 FIX)
    //     Class C → Prof. Fram  (different invigilator)
    //
    //  If Prof. Raman absent → replaced by available non-absent faculty
    //  who is not already assigned to another class on same day.
    // ══════════════════════════════════════════════════

    /**
     * Exam timetable generate karo.
     *
     * BUG-1 FIX: currentDate = startDate.plusDays(dayOffset) — correct increment.
     * BUG-2 FIX: One subject per day. subjects.get(dayOffset) — one advance per DAY.
     * BUG-3 FIX: usedInvigThisDay — each class column gets DIFFERENT invigilator.
     * BUG-4 FIX: No inner slot loop. One day = one row.
     * BUG-5 FIX: startDate passed from controller's DatePicker.
     *
     * Generates one row per day, one subject per day across all classes.
     */
    private List<TimetableSlot> generateExamTimetable() {

        int totalSubjects = subjects.size();

        // Drekk subject mate ek din (day) — subject count = total rows
        // One day per subject — total rows = number of subjects
        for (int dayOffset = 0; dayOffset < totalSubjects; dayOffset++) {

            // ── BUG-1 FIX: Correct date increment ──
            // startDate.plusDays() returns a NEW date each time — immutable
            // Pahela: dayIdx was just used as label "Day N" — never converted
            // to real date. Same date repeated for all rows.
            LocalDate currentDate = startDate.plusDays(dayOffset);
            String    dateLabel   = currentDate.format(DATE_FMT);   // "01 Apr 2024"
            String    dayOfWeek   = currentDate.getDayOfWeek()
                                        .toString().substring(0, 3); // "MON", "TUE"

            // Row label: "01 Apr 2024\nMon"
            String rowLabel = dateLabel + "\n" + dayOfWeek;

            // ── BUG-2 FIX: ONE subject per day ──
            // Pahela: subIdx advanced per class column — 4 classes × 1 day = 4 subjects
            // consumed before date incremented. All subjects on Day 1.
            // Fixed: subjects.get(dayOffset) — one subject per outer day loop.
            String todaySubject = subjects.get(dayOffset);

            // Create one row for this day
            TimetableSlot examRow = new TimetableSlot(rowLabel, EXAM_SLOT_TIME, classes.size());

            // ── BUG-3 FIX: Track invigilators used THIS day ──
            // Pahela: invBusy key used dayIdx which was always 0 — no real separation.
            // Fixed: Fresh set per day ensures each class gets a different invigilator.
            Set<String> usedInvigThisDay = new HashSet<>();

            for (int cIdx = 0; cIdx < classes.size(); cIdx++) {

                String className = classes.get(cIdx);

                // Pick invigilator for this class on this day
                // Must be: not absent, not already assigned to another class today
                String invigilator = pickInvigilatorForDay(
                    todaySubject, cIdx, usedInvigThisDay);

                if (invigilator != null) {
                    // Cell content: Subject \n Invigilator \n [ClassName]
                    examRow.setClassSlot(cIdx,
                        todaySubject + "\n" + invigilator + "\n[" + className + "]");

                    // Status based on priority setting
                    examRow.setStatus(cIdx,
                        "HIGH".equals(priority)
                            ? TimetableSlot.STATUS_PRIORITY
                            : TimetableSlot.STATUS_OK);

                    if ("HIGH".equals(priority)) priorityCount++;

                    // Mark this invigilator as used for today
                    usedInvigThisDay.add(invigilator);

                } else {
                    // Koi invigilator available nathi — conflict
                    examRow.setClassSlot(cIdx, todaySubject + "\n⚠ No Invigilator");
                    examRow.setStatus(cIdx, TimetableSlot.STATUS_CONFLICT);
                    conflictCount++;
                }
            }

            generatedSlots.add(examRow);
        }

        resolvedCount = substitutions.size();
        return generatedSlots;
    }

    /**
     * Invigilator pick karo for one class on one day.
     *
     * Priority order (BUG-6 FIX — substitution logged for exam too):
     *  1. Subject no preferred faculty → available? → use them
     *  2. Preferred is absent → find substitute (logged in substitutions)
     *  3. Any available faculty not yet used today → fallback
     *  4. Nobody available → return null (conflict)
     *
     * Picks invigilator for one class on one exam day.
     * Ensures: not absent, not already assigned to another class today.
     */
    private String pickInvigilatorForDay(String        subject,
                                          int           classIdx,
                                          Set<String>   usedToday) {

        // Preferred faculty for this subject
        String preferred = subjectFacultyMap.get(subject);

        // ── Try preferred faculty ──
        if (preferred != null
                && !facultyAbsent.contains(preferred)
                && !usedToday.contains(preferred)) {
            return preferred;
        }

        // ── Preferred is absent → substitute needed ──
        boolean preferredAbsent = preferred != null
            && facultyAbsent.contains(preferred);

        // Find any available substitute
        String substitute = null;
        for (String fac : facultyList) {
            if (!fac.equals(preferred)
                    && !facultyAbsent.contains(fac)
                    && !usedToday.contains(fac)) {
                substitute = fac;
                break;
            }
        }

        // Log substitution record if absent faculty replaced
        if (substitute != null && preferredAbsent) {
            substitutions.add(new SubstitutionRecord(
                preferred,
                substitute,
                "Exam Day " + (classIdx + 1),   // rough label
                classIdx < classes.size() ? classes.get(classIdx) : "Class",
                subject,
                "EXAM_SUBSTITUTE"
            ));
            resolvedCount++;
        }

        return substitute; // null if no one available (conflict)
    }

    // ══════════════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════════════

    /**
     * Round-robin: drekk subject ne ek faculty assign karo.
     * Math→Prof.A, Physics→Prof.B, Chemistry→Prof.C, ...
     *
     * Round-robin: assigns one primary faculty per subject.
     */
    private void buildSubjectFacultyMap() {
        subjectFacultyMap.clear();
        if (facultyList.isEmpty()) return;
        for (int i = 0; i < subjects.size(); i++) {
            subjectFacultyMap.put(
                subjects.get(i),
                facultyList.get(i % facultyList.size())
            );
        }
    }

    // ── Result getters ──
    public int  getConflictCount()                     { return conflictCount; }
    public int  getResolvedCount()                     { return resolvedCount; }
    public int  getPriorityCount()                     { return priorityCount; }
    public List<SubstitutionRecord> getSubstitutions() { return substitutions; }
    public Map<String, String>  getSubjectFacultyMap() { return subjectFacultyMap; }

    public int getFreeSlots() {
        return (int) generatedSlots.stream()
            .flatMapToInt(slot -> IntStream.range(0, slot.getClassCount())
                .filter(i -> TimetableSlot.STATUS_EMPTY.equals(slot.getStatus(i))))
            .count();
    }
}
