package com.classroom.eduethics.Models;

import java.util.List;
import java.util.Map;

public class HomeClassroomWholeModel {

    String aboutClass;
    String aboutInstructor;
    String className;
    String subject;
    List<Map<String, Object>> assignments;
    List<Map<String, Object>> studyMaterial;
    List<Map<String, Object>> test;
    List<Map<String, Object>> notice;
    List<Map<String, Object>> timetable;
    List<Map<String, Object>> currentStudents;
    List<Map<String, Object>> pendingStudents;
    String fee;
    String id;
    String teacherId;
    String runningClass = "false";
    boolean onlyTeacherMessage = false;


    public HomeClassroomWholeModel() {

    }


    public String getRunningClass() {
        return runningClass;
    }

    public void setRunningClass(String runningClass) {
        this.runningClass = runningClass;
    }

    public boolean isOnlyTeacherMessage() {
        return onlyTeacherMessage;
    }

    public void setOnlyTeacherMessage(boolean onlyTeacherMessage) {
        this.onlyTeacherMessage = onlyTeacherMessage;
    }


    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAboutClass() {
        return aboutClass;
    }

    public void setAboutClass(String aboutClass) {
        this.aboutClass = aboutClass;
    }

    public String getAboutInstructor() {
        return aboutInstructor;
    }

    public void setAboutInstructor(String aboutInstructor) {
        this.aboutInstructor = aboutInstructor;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<Map<String, Object>> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Map<String, Object>> assignments) {
        this.assignments = assignments;
    }

    public List<Map<String, Object>> getStudyMaterial() {
        return studyMaterial;
    }

    public void setStudyMaterial(List<Map<String, Object>> studyMaterial) {
        this.studyMaterial = studyMaterial;
    }

    public List<Map<String, Object>> getTest() {
        return test;
    }

    public void setTest(List<Map<String, Object>> test) {
        this.test = test;
    }

    public List<Map<String, Object>> getNotice() {
        return notice;
    }

    public void setNotice(List<Map<String, Object>> notice) {
        this.notice = notice;
    }

    public List<Map<String, Object>> getTimetable() {
        return timetable;
    }

    public void setTimetable(List<Map<String, Object>> timeTable) {
        this.timetable = timeTable;
    }

    public List<Map<String, Object>> getCurrentStudents() {
        return currentStudents;
    }

    public void setCurrentStudents(List<Map<String, Object>> currentStudents) {
        this.currentStudents = currentStudents;
    }

    public List<Map<String, Object>> getPendingStudents() {
        return pendingStudents;
    }

    public void setPendingStudents(List<Map<String, Object>> pendingStudents) {
        this.pendingStudents = pendingStudents;
    }
}
