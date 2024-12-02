package com.dulcian.face;

import ai.djl.translate.TranslateException;
import com.dulcian.face.controller.FaceController;
import com.dulcian.face.dto.FaceSimilaritySearch;
import com.dulcian.face.model.VectorRepository;
import com.dulcian.face.service.ExternalFaceExtraction;
import com.dulcian.face.service.FaceService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@Disabled
class FaceApplicationTests {
	Logger logger = LoggerFactory.getLogger("ExternalFaceExtraction");

	@Autowired
	ExternalFaceExtraction externalFaceExtraction;

	@Autowired
	FaceService faceService;

	@Autowired
	ExternalFaceExtraction faceExtraction;

	@Autowired
	VectorRepository vectorRepository;

	@Autowired
	FaceController faceController;

	@Test
	void getImageTest() {

		int[] array = IntStream.range(1, 11)
				.parallel()
				.map(a -> {
					try {
						faceController.getFace(4096, false);
						return 0;
					} catch (Exception e) {
						e.printStackTrace();
						return -1;
					}
				}).toArray();
		System.out.println(Arrays.toString(array));
	}

	@Test
	void testFaceEx(){

		long count = IntStream.rangeClosed(0, 100)
//				.parallel()
				.map(x -> {
					try {
						return faceExtraction.extractEmbedding(getSample()).length;
					} catch (Exception e) {
						e.printStackTrace();
						return -1;
					}
				})
				.filter(a -> a == -1)
				.count();

		logger.info("failure( {} )", count);
	}

	@Test
	void testPerformance(){
		double[] targetFaceVector = faceExtraction.extractEmbedding(getSample());

		Instant now = Instant.now();
		Instant now1 = now.plusSeconds(3600);
		Instant now2 = now.plusSeconds(7200);
		Instant now3 = now.plusSeconds(9800);
		Instant now4 = now.plusSeconds(13400);

		try (MockedStatic<Instant> mockedInstant = Mockito.mockStatic(Instant.class)) {

			long t1 = System.currentTimeMillis();
			List<FaceSimilaritySearch> r1 = faceService.findTopSimilarFace(getSample(), 10);
			long t2 = System.currentTimeMillis();
			mockedInstant.when(Instant::now).thenReturn(now1);
			List<FaceSimilaritySearch> r2 = faceService.findTopSimilarFace(getSample(), 10);
			long t3 = System.currentTimeMillis();
			mockedInstant.when(Instant::now).thenReturn(now2);
			List<FaceSimilaritySearch> r3 = faceService.findTopSimilarFace(getSample(), 10);
			long t4 = System.currentTimeMillis();
			mockedInstant.when(Instant::now).thenReturn(now3);
			List<FaceSimilaritySearch> r4 = faceService.findTopSimilarFace(getSample(), 10);
			long t5 = System.currentTimeMillis();
			mockedInstant.when(Instant::now).thenReturn(now4);
			List<FaceSimilaritySearch> r5 = faceService.findTopSimilarFace(getSample(), 10);
			long t6 = System.currentTimeMillis();

			logger.info("First request: {}", (t2 - t1) /1000.0);
			logger.info("After 1hour: {}", (t3 - t2) /1000.0);
			logger.info("After 2hour: {}", (t4 - t3) /1000.0);
			logger.info("After 3hour: {}", (t5 - t4) /1000.0);
			logger.info("After 4hour: {}", (t6 - t5) /1000.0);

		}
	}

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

	public static String getSample(){
		try {
			return new String(Files.readAllBytes(Paths.get("C:\\Users\\Mengstab\\Downloads\\Compressed\\face\\target\\txtt")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
