package com.dulcian.face.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

@Service
public class ExternalFaceExtraction {
    Logger logger = LoggerFactory.getLogger("ExternalFaceExtraction");

    RestTemplate restTemplate  = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();

    private final JdbcTemplate jdbcTemplate;

    public ExternalFaceExtraction(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    public double[] extractEmbedding(String face64){
        try{

            String format = null;
            if (face64.charAt(0) == '/') format = "jpeg";
            else if(face64.charAt(0) == 'i') format = "png";

            if(format == null) throw new RuntimeException("Format unknown");

            HashMap<String, Object> payload = new HashMap<>();
            payload.put("payload", "data:image/" + format + ";base64," + face64);
            JsonNode response = restTemplate.postForEntity("http://localhost:5050/represent", new HttpEntity<>(payload, new HttpHeaders()), JsonNode.class).getBody();
            assert response != null;
            JsonNode embedding = response.get("embedding");
            return mapper.convertValue(embedding, double[].class);

        }catch(RestClientResponseException exception){
            String responseString = exception.getResponseBodyAsString();
            JsonNode response;
            try {
                response = mapper.readTree(responseString);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String error = response.get("error").asText();
            throw new RuntimeException(error);
        }

    }

    @PostConstruct
    public void FaceVectorUpdateScript() {
        logger.info("Face vector update script is starting");

        List<Object[]> updateVectorModelParams = new ArrayList<>();
        List<Object[]> updateImageModelParams = new ArrayList<>();

        jdbcTemplate.query("SELECT id, image FROM ImageModel WHERE createdDate IS NULL", rs -> {
            int id = rs.getInt("id");
            byte[] faceRaw = EncryptionUtils.decrypt(rs.getBytes("image"));

            try {
                String face64 = Base64.getEncoder().encodeToString(faceRaw);
                double[] vector = extractEmbedding(face64);

                updateVectorModelParams.add(new Object[]{ConversionUtils.toByte(vector), id});
                updateImageModelParams.add(new Object[]{id});
            }catch (Exception e){
                logger.error("Face vector updating image( {} )", id);
                e.printStackTrace();
            }
        });

        logger.info("Face vector updating rows = " + updateVectorModelParams.size());

        // Execute batch updates
        jdbcTemplate.batchUpdate("UPDATE VectorModel SET vector = ? WHERE id = ?", updateVectorModelParams);
        jdbcTemplate.batchUpdate("UPDATE ImageModel SET createdDate = GETDATE() WHERE id = ?", updateImageModelParams);

        logger.info("Face vector update script has ended");
    }


}
