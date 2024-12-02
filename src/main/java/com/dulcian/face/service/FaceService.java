package com.dulcian.face.service;

import com.dulcian.face.dto.FaceSimilaritySearch;
import com.dulcian.face.dto.VectorDto;
import com.dulcian.face.model.*;
import com.dulcian.face.utils.ConversionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FaceService {

    private final VectorRepository vectorRepository;
    private final ImageRepository imageRepository;
    private final ExternalFaceExtraction faceExtraction;
    private final JdbcTemplate jdbcTemplate;
    private final VectorMemoryRepository vectorMemoryRepository;

    public FaceService(VectorRepository vectorRepository, ImageRepository imageRepository, ExternalFaceExtraction faceExtraction, JdbcTemplate jdbcTemplate, VectorMemoryRepository vectorMemoryRepository) {
        this.imageRepository = imageRepository;
        this.vectorRepository = vectorRepository;
        this.faceExtraction = faceExtraction;
        this.jdbcTemplate = jdbcTemplate;
        this.vectorMemoryRepository = vectorMemoryRepository;
    }
    public List<FaceSimilaritySearch> findTopSimilarFace(String targetFace64, Integer exclude){

        double[] targetFaceVector = faceExtraction.extractEmbedding(targetFace64);
        double faceThreshold = getFaceThreshold();

        List<VectorDto> allVectors = vectorMemoryRepository.getAllExcluding(exclude);

        return allVectors.parallelStream()
                .map(vectorModel -> {
                    double similarityIndex = getCosineMetrics(targetFaceVector, vectorModel.getVector());

                    if (similarityIndex < faceThreshold) {
                        return null; // Filter out non-matching results
                    }

                    return new FaceSimilaritySearch(
                            vectorModel.getId(),
                            vectorModel.getEmployeeId(),
                            similarityIndex
                    );
                })
                .filter(Objects::nonNull) // Remove null entries
                .sorted(Comparator.comparingDouble(FaceSimilaritySearch::getThreshold).reversed())
                .limit(7)
                .collect(Collectors.toList());
    }

    public ImageModel saveEmployeeFace(Integer employeeId, String face64){
        double[] vector = faceExtraction.extractEmbedding(face64);
        byte[] imageRaw = Base64.getDecoder().decode(face64);
        byte[] vectorByte = ConversionUtils.toByte(vector);

        for (ImageModel savedImage : imageRepository.findByEmployeeId(employeeId)) {
            if(Arrays.equals(savedImage.getImage(), imageRaw)){ // stop users from adding the same image
                return savedImage;
            }
        }

        ImageModel savedImageModel = imageRepository.save(new ImageModel(employeeId, imageRaw));
        vectorRepository.save(new VectorModel(savedImageModel.getId(), vectorByte));
        vectorMemoryRepository.add(savedImageModel.getId(), employeeId, vector);
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

    public List<ImageModel> getEmployeeFace(Integer id) {
        return imageRepository.findByEmployeeId(id);
    }
    public void deleteEmployeeFace(Integer id) {
        vectorRepository.deleteById(id);
        imageRepository.deleteById(id);
        vectorMemoryRepository.deleteById(id);
    }
    public double getFaceThreshold(){
        return jdbcTemplate.queryForObject("SELECT IntValue FROM BiometricSettings WHERE Name = 'FACE_MATCHING_THRESHOLD_VALUE'", Double.class) / 100;
    }
}
