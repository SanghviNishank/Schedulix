package com.schedulix.models;

public class Subject {
    private String name;
    private String faculty;
    private String priority; // HIGH, MEDIUM, LOW
    private String assignedClass;

    public Subject(String name, String faculty, String priority, String assignedClass) {
        this.name = name;
        this.faculty = faculty;
        this.priority = priority;
        this.assignedClass = assignedClass;
    }

    public Subject(String name) {
        this.name = name;
        this.priority = "MEDIUM";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getAssignedClass() { return assignedClass; }
    public void setAssignedClass(String assignedClass) { this.assignedClass = assignedClass; }

    @Override
    public String toString() { return name; }
}
