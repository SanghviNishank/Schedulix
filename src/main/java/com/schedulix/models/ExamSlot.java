package com.schedulix.models;

/**
 * Exam scheduling mate ek slot represent kare che.
 * Drekk exam ek specific date + time slot + classroom ma hoy che.
 *
 * This model represents one scheduled exam entry with
 * date, slot, subject, invigilator and classroom.
 */
public class ExamSlot {

    // Exam ni date (e.g. "2024-01-15")
    private final String date;

    // Slot number (1, 2, 3 ...) te day ma
    private final int slotNumber;

    // Slot time display (e.g. "9:00 AM – 12:00 PM")
    private final String slotTime;

    // Kon subject no exam che
    private final String subjectName;

    // Invigilator (faculty jo duty par che)
    private final String invigilatorName;

    // Exam room / classroom
    private final String roomName;

    // Conflict flag — true means PROBLEM found
    private boolean hasConflict;

    // Optional: conflict reason
    private String conflictReason;

    public ExamSlot(String date, int slotNumber, String slotTime,
                    String subjectName, String invigilatorName, String roomName) {
        this.date           = date;
        this.slotNumber     = slotNumber;
        this.slotTime       = slotTime;
        this.subjectName    = subjectName;
        this.invigilatorName = invigilatorName;
        this.roomName       = roomName;
        this.hasConflict    = false;
        this.conflictReason = "";
    }

    // ── Getters ──
    public String getDate()            { return date; }
    public int    getSlotNumber()      { return slotNumber; }
    public String getSlotTime()        { return slotTime; }
    public String getSubjectName()     { return subjectName; }
    public String getInvigilatorName() { return invigilatorName; }
    public String getRoomName()        { return roomName; }
    public boolean isHasConflict()     { return hasConflict; }
    public String getConflictReason()  { return conflictReason; }

    // ── Setters ──
    public void setHasConflict(boolean b)      { this.hasConflict = b; }
    public void setConflictReason(String r)    { this.conflictReason = r; }

    /**
     * TableView cell ma show karva mate string
     */
    public String getDisplayText() {
        return subjectName + "\n" + invigilatorName + "\n[" + roomName + "]";
    }

    @Override
    public String toString() {
        return date + " | Slot " + slotNumber + " | " + subjectName
               + " | " + invigilatorName + " | " + roomName;
    }
}
