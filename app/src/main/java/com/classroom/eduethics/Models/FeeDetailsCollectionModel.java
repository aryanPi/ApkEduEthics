package com.classroom.eduethics.Models;

import java.util.List;
import java.util.Map;

public class FeeDetailsCollectionModel {

    String adv;
    List<Map<String, Object>> students;
    String totalDue;
    String totalRec;
    String startDate;
    String feePerM;
    String teacherId;
    String pendingFee;

    public FeeDetailsCollectionModel() {

    }



    public FeeDetailsCollectionModel(String adv, List<Map<String, Object>> students, String totalDue, String totalRec, String startDate, String feePerM, String teacherId, String pendingFee) {
        this.adv = adv;
        this.students = students;
        this.totalDue = totalDue;
        this.totalRec = totalRec;
        this.startDate = startDate;
        this.feePerM = feePerM;
        this.teacherId = teacherId;
        this.pendingFee = pendingFee;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public void setPendingFee(String pendingFee) {
        this.pendingFee = pendingFee;
    }

    public String getPendingFee() {
        return pendingFee;
    }

    public String getAdv() {
        return adv;
    }

    public void setAdv(String adv) {
        this.adv = adv;
    }

    public List<Map<String, Object>> getStudents() {
        return students;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getFeePerM() {
        return feePerM;
    }

    public void setFeePerM(String feePerM) {
        this.feePerM = feePerM;
    }

    public void setStudents(List<Map<String, Object>> students) {
        this.students = students;
    }

    public String getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(String totalDue) {
        this.totalDue = totalDue;
    }

    public String getTotalRec() {
        return totalRec;
    }

    public void setTotalRec(String totalRec) {
        this.totalRec = totalRec;
    }
}
