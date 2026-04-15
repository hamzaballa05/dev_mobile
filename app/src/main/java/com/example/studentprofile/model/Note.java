package com.example.studentprofile.model;

import java.io.Serializable;

public class Note implements Serializable {
    private String subject;
    private double score;

    public Note() {
    }

    public Note(String subject, double score) {
        this.subject = subject;
        this.score = score;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isGood() {
        return score >= 10.0;
    }
}
