package com.dulcian.face.dto;

public class VectorDto {
    int id;
    int employeeId;
    double[] vector;

    public VectorDto(int id, int employeeId) {
        this.id = id;
        this.employeeId = employeeId;
    }

    public VectorDto(int id, int employeeId, double[] vector) {
        this.id = id;
        this.employeeId = employeeId;
        this.vector = vector;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public double[] getVector() {
        return vector;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }
}
