package com.dulcian.face.model;

import com.dulcian.face.utils.EncryptionUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ImageConverter implements AttributeConverter<byte[],byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(byte[] attribute) {
        return EncryptionUtils.encrypt(attribute);
    }

    @Override
    public byte[] convertToEntityAttribute(byte[] dbData) {
        return EncryptionUtils.decrypt(dbData);
    }
}
