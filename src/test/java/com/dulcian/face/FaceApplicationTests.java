package com.dulcian.face;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.TranslateException;
import com.dulcian.face.service.FaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

@SpringBootTest
class FaceApplicationTests {
	Logger logger = Logger.getLogger("Face Test");

	@Autowired
	FaceService faceService;
	@Test
	void contextLoads() {
	}

	@Test
	void testSimilarity() throws IOException, TranslateException {
		int counter = 0;
		int right = 0;
		BigInteger totalReq = BigInteger.ZERO;

		File folder = new File("C:\\Users\\Mengstab\\Downloads\\Compressed\\lfw_funneled");
		for (File person : folder.listFiles()) {
			if(person.getName().equals("Gene_Robinson")) break;
			File[] images = person.listFiles();
			if(images != null && images.length > 1){

				if(true){
					if (images.length == 2)
						totalReq = totalReq.add(BigInteger.ONE);
					else
						totalReq = totalReq.add(calculateCombination(images.length, 2));
					continue;
				}

				for (int i = 0; i < images.length - 1; i++) {
					Image img1;
					try {
						img1 = ImageFactory.getInstance().fromFile(images[i].toPath());
					}catch (Exception e){
						continue;
					}

					float[] floats1 = extractVector(img1);
					for (int j = i + 1; j < images.length; j++) {
						Image img2;
						try {
							img2 = ImageFactory.getInstance().fromFile(images[j].toPath());

						}catch (Exception e){
							break;
						}
						float[] floats2 = extractVector(img2);
						float similarityIndex = calculateSimilarityIndex(floats1, floats2);
						if(similarityIndex <= 0.67){
							counter++;
							System.out.println("-------------------"+similarityIndex+"---------------------------------");
							logger.info(images[i].toPath().toString());
							logger.info(images[j].toPath().toString());
							System.out.println("-------------------"+similarityIndex+"---------------------------------");
						}else right++;


					}
				}
			}
		}
		System.out.println(totalReq);
		System.out.println("Totaled Right = " + right);
		System.out.println("Totaled Failed = " + counter);
	}

	public float[] extractVector(Image img) throws TranslateException {
		DetectedObjects faceBoundObject = faceService.getFaceBoundObject(img);
		Image cropFace = faceService.getCropFace(img, faceBoundObject);
		return faceService.extractFeatures(cropFace);
	}

	public float calculateSimilarityIndex(float[] feature1, float[] feature2) {
		float dotProduct = 0.0f;
		float magnitudeA = 0.0f;
		float magnitudeB = 0.0f;
		int length = feature1.length;
		for (int i = 0; i < length; ++i) {
			dotProduct += feature1[i] * feature2[i];
			magnitudeA += feature1[i] * feature1[i];
			magnitudeB += feature2[i] * feature2[i];
		}

		magnitudeA = (float)Math.sqrt(magnitudeA);
		magnitudeB = (float)Math.sqrt(magnitudeB);

		return (float) (dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB))) ;
	}

	public static BigInteger calculateCombination(int n, int k) {
		if (k > n || n < 0 || k < 0) {
			return BigInteger.ZERO;
		}
		if (k == 0 || k == n) {
			return BigInteger.ONE;
		}
		k = Math.min(k, n - k); // Take advantage of symmetry
		BigInteger result = BigInteger.ONE;
		for (int i = 0; i < k; i++) {
			result = result.multiply(BigInteger.valueOf(n - i))
					.divide(BigInteger.valueOf(i + 1));
		}
		return result;
	}

	public static long factorial(int n) {
		if (n == 0) {
			return 1;
		}
		long result = 1;
		for (int i = 1; i <= n; i++) {
			result *= i;
		}
		return result;
	}

}
