package com.dulcian.face.dto;

import java.util.List;

public class InvalidFaceMap{
    public boolean tobeSaved;
    public String face64;
    public List<FaceSimilaritySearch> similarFaceList;

    public InvalidFaceMap(String face64, List<FaceSimilaritySearch> similarFaceList, boolean tobeSaved) {
        this.tobeSaved = tobeSaved;
        this.face64 = face64;
        this.similarFaceList = similarFaceList;
    }

    public boolean isTobeSaved() {
        return tobeSaved;
    }

    public void setTobeSaved(boolean tobeSaved) {
        this.tobeSaved = tobeSaved;
    }

    public String getFace64() {
        return face64;
    }

    public void setFace64(String face64) {
        this.face64 = face64;
    }

    public List<FaceSimilaritySearch> getSimilarFaceList() {
        return similarFaceList;
    }

    public void setSimilarFaceList(List<FaceSimilaritySearch> similarFaceList) {
        this.similarFaceList = similarFaceList;
    }
}