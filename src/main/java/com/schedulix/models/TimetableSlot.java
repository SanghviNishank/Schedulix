package com.schedulix.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Ek row represent kare che timetable ma.
 * Drekk row = ek period (ya exam slot).
 * Drekk column = ek class.
 *
 * One row in the timetable grid.
 * Each column index corresponds to one class/room.
 */
public class TimetableSlot {

    // Period ya date+slot no label
    private final StringProperty period;

    // Time display string
    private final StringProperty time;

    // Drekk class mate cell content
    private final StringProperty[] classSlots;

    // Drekk cell no status (colour coding mate)
    private final String[] statuses;

    // ── Status constants ──
    public static final String STATUS_OK           = "OK";
    public static final String STATUS_CONFLICT      = "CONFLICT";
    public static final String STATUS_PRIORITY      = "PRIORITY";
    public static final String STATUS_EMPTY         = "EMPTY";
    public static final String STATUS_SUBSTITUTION  = "SUBSTITUTION"; // NEW: substituted faculty

    public TimetableSlot(String period, String time, int classCount) {
        this.period     = new SimpleStringProperty(period);
        this.time       = new SimpleStringProperty(time);
        this.classSlots = new StringProperty[classCount];
        this.statuses   = new String[classCount];

        for (int i = 0; i < classCount; i++) {
            classSlots[i] = new SimpleStringProperty("");
            statuses[i]   = STATUS_EMPTY;
        }
    }

    // ── Property accessors (for JavaFX TableView binding) ──
    public String         getPeriod()               { return period.get(); }
    public StringProperty periodProperty()           { return period; }

    public String         getTime()                 { return time.get(); }
    public StringProperty timeProperty()             { return time; }

    // ── Cell content ──
    public String         getClassSlot(int idx)     { return classSlots[idx].get(); }
    public void           setClassSlot(int idx, String val) { classSlots[idx].set(val); }
    public StringProperty classSlotProperty(int idx){ return classSlots[idx]; }

    // ── Cell status ──
    public String  getStatus(int idx)               { return statuses[idx]; }
    public void    setStatus(int idx, String status) { statuses[idx] = status; }

    public int getClassCount() { return classSlots.length; }
}
