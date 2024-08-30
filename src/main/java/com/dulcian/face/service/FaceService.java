package com.dulcian.face.service;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.translate.TranslateException;
import com.dulcian.face.model.ImageModel;
import com.dulcian.face.model.ImageRepository;
import com.dulcian.face.model.VectorModel;
import com.dulcian.face.model.VectorRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Service
public class FaceService {

    private final VectorRepository vectorRepository;
    private final ImageRepository imageRepository;
    private final Predictor<Image, float[]> FeatureExtractor;
    private final Predictor<Image, DetectedObjects> ObjectDetectionModel;

    private final JdbcTemplate jdbcTemplate;
    private double FACE_THRESHOLD = 0.67;

    public FaceService(VectorRepository vectorRepository, ImageRepository imageRepository, Predictor<Image, float[]> featureExtractor, Predictor<Image, DetectedObjects> objectDetectionModel, JdbcTemplate jdbcTemplate) throws IOException, TranslateException {
        this.imageRepository = imageRepository;
        this.vectorRepository = vectorRepository;
        FeatureExtractor = featureExtractor;
        ObjectDetectionModel = objectDetectionModel;
        this.jdbcTemplate = jdbcTemplate;
    }
    public Integer findTopSimilarFace(float[] features, Integer exclude){

        List<Float> newFace = new ArrayList<>();
        for (float feature : features) {
            newFace.add(feature);
        }

        int mostSimilarEmployee = -1;
        float maxSimilarityIndex = 0;
        List<VectorModel> allFaces = vectorRepository.findAllExcluding(exclude);
        for(VectorModel vectorModel : allFaces){
            List<Float> employeeFace = convertByteArrayToFloatList(vectorModel.getVector());
            float similarityIndex = calculSimilar(newFace, employeeFace);

            if(similarityIndex < FACE_THRESHOLD)
                continue; //

            if(similarityIndex > maxSimilarityIndex){
                mostSimilarEmployee = vectorModel.getId();
                maxSimilarityIndex = similarityIndex;
            }
            return mostSimilarEmployee;

        }

        return mostSimilarEmployee;
    }

    public void saveVector(Integer newEmployeeId, float[] features){
        List<Float> newFace = new ArrayList<>();
        for (float feature : features) {
            newFace.add(feature);
        }
        VectorModel vectorModel = new VectorModel();
        vectorModel.setId(newEmployeeId);
        vectorModel.setVector(convertFloatListToByteArray(newFace));
        vectorRepository.save(vectorModel);
    }
    public Integer saveImage(Integer employeeId, byte[] face64){
        ImageModel image = new ImageModel();
        image.setEmployeeId(employeeId);
        image.setImage(face64);
        return imageRepository.save(image).getId();
    }

    public String getImage(Integer id){
        Optional<ImageModel> image = imageRepository.findById(id);
        if(image.isPresent()){
            ImageModel imageModel = image.get();
            return Base64.getEncoder().encodeToString(imageModel.getImage());
        }
        return "";
    }
    public Image getFaceImage(String base64Image) throws IOException, TranslateException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Image img = ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(imageBytes));
        DetectedObjects faceBox = getFaceBoundObject(img); //Get the face position
        return getCropFace(img, faceBox);
    }
    public float[] extractFeatures(Image img) throws TranslateException {
        return FeatureExtractor.predict(img);
    }
    public DetectedObjects getFaceBoundObject(Image img) throws TranslateException {
        return ObjectDetectionModel.predict(img);
    }

    public Image getCropFace(Image img, DetectedObjects detectedObjects){
        if (detectedObjects.items().isEmpty()) {
            return null;
        }
        DetectedObjects.DetectedObject detectedObject = (DetectedObjects.DetectedObject) detectedObjects.topK(50).get(0);

        // Get the bounding box of the detected object
        BoundingBox boundingBox = detectedObject.getBoundingBox();
        Rectangle rect = boundingBox.getBounds();

        int x = (int) (rect.getX() * img.getWidth());
        int y = (int) (rect.getY() * img.getHeight());
        int width = (int) (rect.getWidth() * img.getWidth());
        int height = (int) (rect.getHeight() * img.getHeight());
        return img.getSubImage(x, y, width, height);
    }
    public float calculSimilar(List<Float> feature1, List<Float> feature2) {
        float dotProduct = 0.0f;
        float magnitudeA = 0.0f;
        float magnitudeB = 0.0f;
        int length = feature1.size();
        for (int i = 0; i < length; ++i) {
            dotProduct += feature1.get(i) * feature2.get(i);
            magnitudeA += feature1.get(i) * feature1.get(i);
            magnitudeB += feature2.get(i) * feature2.get(i);
        }

        magnitudeA = (float)Math.sqrt(magnitudeA);
        magnitudeB = (float)Math.sqrt(magnitudeB);

        return (float) (dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB))) ;
    }


    private byte[] convertFloatListToByteArray(List<Float> vector) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {
            for (Float f : vector) {
                dos.writeFloat(f);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error converting float list to byte array", e);
        }
    }

    private List<Float> convertByteArrayToFloatList(byte[] data) {
        List<Float> vector = new ArrayList<>();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bis)) {
            while (dis.available() > 0) {
                vector.add(dis.readFloat());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error converting byte array to float list", e);
        }
        return vector;
    }

    public Integer getEmployeeId(Integer vectorId) {
        Optional<ImageModel> byId = imageRepository.findById(vectorId);
        if(byId.isPresent()){
            ImageModel imageModel = byId.get();
            return imageModel.getEmployeeId();
        }
        return -1;
    }

    public List<Integer> getVectorsByEmployeeId(Integer employeeId){
        return imageRepository.findVectorId(employeeId);
    }

    public List<ImageModel> getEmployeeFace(Integer id) {
        return imageRepository.findByEmployeeId(id);
    }

    public void deleteEmployeeFace(Integer id) {
        vectorRepository.deleteById(id);
        imageRepository.deleteById(id);
    }

    @PostConstruct
    public void setFACE_THRESHOLD(){
        FACE_THRESHOLD = jdbcTemplate.queryForObject("SELECT IntValue FROM BiometricSettings WHERE Name = 'FACE_MATCHING_THRESHOLD_VALUE'", Double.class);
    }

    public static List<Float> useWrapper(float[] features){
        List<Float> result = new ArrayList<>();
        for(float f : features)
            result.add(f);
        return result;
    }

    public float euclideanDistance(float[] vectorA, float[] vectorB) {
        float sum = 0.0f;
        for (int i = 0; i < vectorA.length; i++) {
            sum += Math.pow(vectorA[i] - vectorB[i], 2);
        }
        return (float) Math.sqrt(sum);
    }
}
