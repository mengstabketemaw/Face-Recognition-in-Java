package com.dulcian.face.service;

import java.nio.ByteBuffer;
public class ConversionUtils {


    public static byte[] toByte(double[] doubleArray) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(doubleArray.length * Double.BYTES);
        for (double value : doubleArray) {
            byteBuffer.putDouble(value);
        }
        return byteBuffer.array();
    }


    public static double[] toDouble(byte[] byteArray) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        double[] doubleArray = new double[byteArray.length / Double.BYTES];
        for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = byteBuffer.getDouble();
        }
        return doubleArray;
    }

}
