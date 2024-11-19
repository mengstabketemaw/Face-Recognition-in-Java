package com.dulcian.face.service;

import com.dulcian.face.dto.FaceSimilaritySearch;
import com.dulcian.face.model.ImageModel;
import com.dulcian.face.model.ImageRepository;
import com.dulcian.face.model.VectorModel;
import com.dulcian.face.model.VectorRepository;
import com.dulcian.face.utils.ConversionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<FaceSimilaritySearch> findTopSimilarFace(String targetFace64, Integer exclude){

        double[] targetFaceVector = faceExtraction.extractEmbedding(targetFace64);
        double faceThreshold = getFaceThreshold();

        HashMap<Integer, Double> calculationResult = new HashMap<>();
        Set<Integer> candidateVectors = new HashSet<>();


        List<VectorModel> allFaces = vectorRepository.findAllExcluding(exclude);

        for(VectorModel vectorModel : allFaces){
            double[] candidateFaceVector = ConversionUtils.toDouble(vectorModel.getVector());
            double similarityIndex = getCosineMetrics(targetFaceVector, candidateFaceVector);

            if(similarityIndex < faceThreshold)
                continue; //

            calculationResult.put(vectorModel.getId(), similarityIndex);
            candidateVectors.add(vectorModel.getId());

        }

        List<ImageModel> candidateImages = imageRepository.findAllById(candidateVectors);

        return candidateImages.stream()
                .map(i -> new FaceSimilaritySearch(
                        i.getId(),
                        i.getEmployeeId(),
                        calculationResult.get(i.getId())
                ))
                .collect(Collectors.toList());
    }

    public ImageModel saveEmployeeFace(Integer employeeId, String face64){
        double[] vector = faceExtraction.extractEmbedding(face64);
        byte[] imageRaw = Base64.getDecoder().decode(face64);
        byte[] vectorByte = ConversionUtils.toByte(vector);
        ImageModel savedImageModel = imageRepository.save(new ImageModel(employeeId, imageRaw));
        vectorRepository.save(new VectorModel(savedImageModel.getId(), vectorByte));
        return savedImageModel;
    }
    public byte[] getImage(Integer id){
        return imageRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Image not found with id = " + id))
                .getImage();
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
