package com.dulcian.face.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class VectorModel {
    @Id
    Integer id;
    @Lob
    byte[] vector;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getVector() {
        return vector;
    }

    public void setVector(byte[] vector) {
        this.vector = vector;
    }


}
