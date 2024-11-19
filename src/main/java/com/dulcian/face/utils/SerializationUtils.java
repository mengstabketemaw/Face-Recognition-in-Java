package com.dulcian.face.utils;

import java.io.*;

public class SerializationUtils {

    public static byte[] serializeFace64(String[] face64s) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(face64s);
            return bos.toByteArray();
        }
    }
    public static String[] deserializeFace64(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (String[]) ois.readObject();
        }
    }
}