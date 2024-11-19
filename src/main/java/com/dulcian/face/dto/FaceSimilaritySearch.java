package com.dulcian.face.dto;

public class FaceSimilaritySearch {
    int vectorId;
    int employeeId;
    double threshold;

    public FaceSimilaritySearch(int vectorId, int employeeId,double threshold) {
        this.vectorId = vectorId;
        this.threshold = threshold;
        this.employeeId = employeeId;
    }

    public int getVectorId() {
        return vectorId;
    }

    public void setVectorId(int vectorId) {
        this.vectorId = vectorId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
