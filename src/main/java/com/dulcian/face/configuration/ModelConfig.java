package com.dulcian.face.configuration;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import com.dulcian.face.service.FaceDetectionTranslator;
import com.dulcian.face.service.FaceFeatureTranslator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;


@Configuration
public class ModelConfig {

    @Bean
    public Predictor<Image, float[]> FeatureExtractor() throws IOException, ModelNotFoundException, MalformedModelException {
        ClassPathResource resource = new ClassPathResource("vggface2.zip");
        Criteria<Image, float[]> criteria =
                Criteria.builder()
                        .setTypes(Image.class, float[].class)
                        .optModelUrls(resource.getURL().toString())
                        .optModelName("vggface2") // specify model file prefix
                        .optTranslator(new FaceFeatureTranslator())
                        .optProgress(new ProgressBar())
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();
        ZooModel<Image, float[]> imageZooModel = criteria.loadModel();
        return imageZooModel.newPredictor();
    }

    @Bean
    public Predictor<Image, DetectedObjects> ObjectDetectionModel() throws ModelNotFoundException, MalformedModelException, IOException {
        ClassPathResource resource = new ClassPathResource("retinaface.zip");
        double confThresh = 0.85f;
        double nmsThresh = 0.45f;
        double[] variance = {0.1f, 0.2f};
        int topK = 50;
        int[][] scales = {{16, 32}, {64, 128}, {256, 512}};
        int[] steps = {8, 16, 32};
        FaceDetectionTranslator translator =
                new FaceDetectionTranslator(confThresh, nmsThresh, variance, topK, scales, steps);

        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelUrls(resource.getURL().toString())
                        // Load model from local file, e.g:
                        .optModelName("retinaface") // specify model file prefix
                        .optTranslator(translator)
                        .optProgress(new ProgressBar())
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();
        ZooModel<Image, DetectedObjects> imageDetectedObjectsZooModel = criteria.loadModel();
        return imageDetectedObjectsZooModel.newPredictor();
    }

}
