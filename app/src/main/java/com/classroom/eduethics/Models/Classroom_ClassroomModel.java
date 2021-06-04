package com.classroom.eduethics.Models;

public class Classroom_ClassroomModel {

    String subject;
    String studentsActive = "";
    String doc;
    String aClass;
    boolean isConfirm ;


    public boolean isConfirm() {
        return isConfirm;
    }

    public void setConfirm(boolean confirm) {
        isConfirm = confirm;
    }

    public String getaClass() {
        return aClass;
    }

    public void setaClass(String aClass) {
        this.aClass = aClass;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStudentsActive() {
        return studentsActive;
    }

    public void setStudentsActive(String studentsActive) {
        this.studentsActive = studentsActive;
    }
}
