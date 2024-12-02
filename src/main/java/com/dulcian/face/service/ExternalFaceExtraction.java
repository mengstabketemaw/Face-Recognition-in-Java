package com.dulcian.face.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class ExternalFaceExtraction {

    RestTemplate restTemplate  = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();

    public ExternalFaceExtraction() {

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

}
