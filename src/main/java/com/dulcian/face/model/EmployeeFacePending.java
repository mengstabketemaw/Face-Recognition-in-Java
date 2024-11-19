package com.dulcian.face.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "HR_EmployeeFacePending")
public class EmployeeFacePending {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Integer parent = null;
    Integer employeeId;
    @Lob
    @JsonIgnore
    byte[] pic;
    public EmployeeFacePending() {
    }

    public EmployeeFacePending(Integer employeeId, byte[] pic) {
        this.employeeId = employeeId;
        this.pic = pic;
    }

    public EmployeeFacePending(Integer parent, Integer employeeId, byte[] pic) {
        this.parent = parent;
        this.employeeId = employeeId;
        this.pic = pic;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }

}
