package com.dulcian.face;

import ai.djl.translate.TranslateException;
import com.dulcian.face.service.ExternalFaceExtraction;
import com.dulcian.face.service.FaceService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.logging.Logger;

@SpringBootTest
@Disabled
class FaceApplicationTests {
	Logger logger = Logger.getLogger("Face Test");

	@Autowired
	FaceService faceService;

	@Autowired
	ExternalFaceExtraction faceExtraction;

	@Test
	void contextLoads() {
	}

	@Test
	void testSimilarity() throws IOException, TranslateException {
		int counter = 0;
		int right = 0;
		BigInteger totalReq = BigInteger.ZERO;

		File folder = new File("C:\\Users\\Mengstab\\Downloads\\Compressed\\Livestock similarity\\Similar on 222");
		for (File person : folder.listFiles()) {
			if (!person.isDirectory()) continue;
			File[] images = person.listFiles();
			if(images != null && images.length > 1){
				double[] floats1 = extractVector(images[0]);
				double[] floats2 = extractVector(images[1]);
				double similarityIndex = calculateSimilarityIndex(floats1, floats2);

				if(similarityIndex > 0.7761)
					System.out.println(similarityIndex + " -- " + person.getName());
			}
		}
	}

	private double[] extractVector(File image) throws IOException {
		return faceExtraction.extractEmbedding(encodeImageToBase64(image));
	}

	public double calculateSimilarityIndex(double[] feature1, double[] feature2) {
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

		return (dotProduct / (magnitudeA * magnitudeB)) ;
	}

	public static String encodeImageToBase64(File file) throws IOException {
		// Read the image file into a byte array
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] imageBytes = new byte[(int) file.length()];
		fileInputStream.read(imageBytes);
		fileInputStream.close();
		return Base64.getEncoder().encodeToString(imageBytes);
	}

}
