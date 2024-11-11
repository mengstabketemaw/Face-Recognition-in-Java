package com.dulcian.face.service;

import com.dulcian.face.model.ImageModel;
import com.dulcian.face.model.ImageRepository;
import com.dulcian.face.model.VectorModel;
import com.dulcian.face.model.VectorRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;

@Service
public class FaceService {

    private final VectorRepository vectorRepository;
    private final ImageRepository imageRepository;
    private final ExternalFaceExtraction faceExtraction;
    private final JdbcTemplate jdbcTemplate;

    public FaceService(VectorRepository vectorRepository, ImageRepository imageRepository, ExternalFaceExtraction faceExtraction, JdbcTemplate jdbcTemplate) {
        this.imageRepository = imageRepository;
        this.vectorRepository = vectorRepository;
        this.faceExtraction = faceExtraction;
        this.jdbcTemplate = jdbcTemplate;
    }
    public Integer findTopSimilarFace(String targetFace64, Integer exclude){

        double[] targetFaceVector = faceExtraction.extractEmbedding(targetFace64);
        double faceThreshold = getFaceThreshold();

        List<VectorModel> allFaces = vectorRepository.findAllExcluding(exclude);
        for(VectorModel vectorModel : allFaces){
            double[] candidateFaceVector = ConversionUtils.toDouble(vectorModel.getVector());
            double similarityIndex = getCosineMetrics(targetFaceVector, candidateFaceVector);

            if(similarityIndex < faceThreshold)
                continue; //
            return vectorModel.getId();
        }
        return -1;
    }

    public Integer saveEmployeeFace(Integer employeeId, String face64){
        double[] vector = faceExtraction.extractEmbedding(face64);
        byte[] imageRaw = Base64.getDecoder().decode(face64);
        byte[] vectorByte = ConversionUtils.toByte(vector);
        ImageModel savedImageModel = imageRepository.save(new ImageModel(employeeId, imageRaw));
        vectorRepository.save(new VectorModel(savedImageModel.getId(), vectorByte));
        return savedImageModel.getId();
    }
    public String getImage(Integer id){
        Optional<ImageModel> image = imageRepository.findById(id);
        if(image.isPresent()){
            ImageModel imageModel = image.get();
            return Base64.getEncoder().encodeToString(imageModel.getImage());
        }
        return "";
    }
    public double getCosineMetrics(double[] feature1, double[] feature2) {
        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;
        int length = feature1.length;
        for (int i = 0; i < length; ++i) {
            dotProduct += feature1[i] * feature2[i];
            magnitudeA += feature1[i] * feature1[i];
            magnitudeB += feature2[i] * feature2[i];
        }

        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        return dotProduct / (magnitudeA * magnitudeB) ;
    }
    public Integer getEmployeeId(Integer vectorId) {
        Optional<ImageModel> byId = imageRepository.findById(vectorId);
        if(byId.isPresent()){
            ImageModel imageModel = byId.get();
            return imageModel.getEmployeeId();
        }
        return -1;
    }
    public List<ImageModel> getEmployeeFace(Integer id) {
        return imageRepository.findByEmployeeId(id);
    }
    public void deleteEmployeeFace(Integer id) {
        vectorRepository.deleteById(id);
        imageRepository.deleteById(id);
    }
    public double getFaceThreshold(){
        return jdbcTemplate.queryForObject("SELECT IntValue FROM BiometricSettings WHERE Name = 'FACE_MATCHING_THRESHOLD_VALUE'", Double.class) / 100;
    }
}
