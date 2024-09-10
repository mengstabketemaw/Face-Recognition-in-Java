package com.dulcian.face.model;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class ImageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Integer employeeId;
    Instant createdDate = Instant.now();
    @Lob
    byte[] image;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }
}
