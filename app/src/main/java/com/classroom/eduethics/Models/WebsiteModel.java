package com.classroom.eduethics.Models;

import java.util.List;
import java.util.Map;

public class WebsiteModel {

    String city;
    String state;
    String aboutIns;
    String address;
    String fbLink;
    String insLogo;
    String profileLink;
    String insName;
    String subjects;
    String teacherId;
    String yearsExp;
    String youtubeLink;

    List<Map<String, String>> classroom;
    List<Map<String,String>> demoLinks;

    public WebsiteModel() {

    }

    public WebsiteModel(String city, String state, String aboutIns, String address, String fbLink, String insLogo, String profileLink, String insName, String subjects, String teacherId, String yearsExp, String youtubeLink, List<Map<String, String>> classroom, List<Map<String,String>> demoLinks) {
        this.city = city;
        this.state = state;
        this.aboutIns = aboutIns;
        this.address = address;
        this.fbLink = fbLink;
        this.insLogo = insLogo;
        this.profileLink = profileLink;
        this.insName = insName;
        this.subjects = subjects;
        this.teacherId = teacherId;
        this.yearsExp = yearsExp;
        this.youtubeLink = youtubeLink;
        this.classroom = classroom;
        this.demoLinks = demoLinks;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAboutIns() {
        return aboutIns;
    }

    public void setAboutIns(String aboutIns) {
        this.aboutIns = aboutIns;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFbLink() {
        return fbLink;
    }

    public void setFbLink(String fbLink) {
        this.fbLink = fbLink;
    }

    public String getInsLogo() {
        return insLogo;
    }

    public void setInsLogo(String insLogo) {
        this.insLogo = insLogo;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
    }

    public String getInsName() {
        return insName;
    }

    public void setInsName(String insName) {
        this.insName = insName;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getYearsExp() {
        return yearsExp;
    }

    public void setYearsExp(String yearsExp) {
        this.yearsExp = yearsExp;
    }

    public String getYoutubeLink() {
        return youtubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
    }

    public List<Map<String, String>> getClassroom() {
        return classroom;
    }

    public void setClassroom(List<Map<String, String>> classroom) {
        this.classroom = classroom;
    }

    public List<Map<String,String>> getDemoLinks() {
        return demoLinks;
    }

    public void setDemoLinks(List<Map<String,String>> demoLinks) {
        this.demoLinks = demoLinks;
    }
}



