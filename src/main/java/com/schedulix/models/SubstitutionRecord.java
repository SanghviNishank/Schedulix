package com.schedulix.models;

/**
 * Aa model ek substitution assignment represent kare che.
 * Jyare koi faculty absent hoy tyare kon substitute karshe
 * ane keva karanne te store kare che.
 *
 * This model stores one substitution record:
 * who is absent, who replaced them, for which class/period.
 */
public class SubstitutionRecord {

    // Absent faculty no naam
    private final String absentFaculty;

    // Jo substitute aaviyo te faculty no naam
    private final String substituteFaculty;

    // Kon period / slot ma substitution thi
    private final String periodLabel;

    // Kon class mate substitution thi
    private final String className;

    // Kon subject hato
    private final String subjectName;

    // Substitution type: SAME_SUBJECT (preferred) ya FREE_SLOT (fallback)
    private final String substitutionType;

    public SubstitutionRecord(String absentFaculty, String substituteFaculty,
                               String periodLabel, String className,
                               String subjectName, String substitutionType) {
        this.absentFaculty    = absentFaculty;
        this.substituteFaculty = substituteFaculty;
        this.periodLabel      = periodLabel;
        this.className        = className;
        this.subjectName      = subjectName;
        this.substitutionType = substitutionType;
    }

    public String getAbsentFaculty()     { return absentFaculty; }
    public String getSubstituteFaculty() { return substituteFaculty; }
    public String getPeriodLabel()       { return periodLabel; }
    public String getClassName()         { return className; }
    public String getSubjectName()       { return subjectName; }
    public String getSubstitutionType()  { return substitutionType; }

    /**
     * UI ma display karva mate readable string
     * Example: "Prof. A (absent) → Prof. B [SAME_SUBJECT]"
     */
    @Override
    public String toString() {
        return absentFaculty + " (absent) → " + substituteFaculty
               + " [" + substitutionType + "] for " + subjectName
               + " in " + className + " @ " + periodLabel;
    }
}
