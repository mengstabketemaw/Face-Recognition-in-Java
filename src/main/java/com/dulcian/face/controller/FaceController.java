package com.dulcian.face.controller;


import ai.djl.modality.cv.Image;
import ai.djl.translate.TranslateException;
import com.dulcian.face.model.ImageModel;
import com.dulcian.face.service.FaceService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
class FaceController{

    private final FaceService faceService;

    FaceController(FaceService faceService) {
        this.faceService = faceService;
    }

    @PostMapping("/face/{id}")
    public Integer faceRegistration(@RequestBody String base64Image, @PathVariable Integer id) throws IOException, TranslateException {
        Image faceImage = faceService.getFaceImage(base64Image);
        if(faceImage == null) //No face detected
            return -2; //No face detected in the given image

        float[] newFace = faceService.extractFeatures(faceImage);
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        Integer vectorId = faceService.saveImage(id, imageBytes);
        faceService.saveVector(vectorId, newFace);
        return vectorId; // Face is registered Successfully
    }

    @PostMapping("/identify/{id}")
    public HashMap<String,Object> faceIdentification(@PathVariable Integer id, @RequestBody String base64Image) throws TranslateException, IOException {
        Image faceImage = faceService.getFaceImage(base64Image);
        HashMap<String,Object> result = new HashMap<>(); result.put("vectorId", -2);
        if(faceImage == null) //No face detected
            return result; //No face detected in the given image

        float[] newFace = faceService.extractFeatures(faceImage);

        //Vectors to exclude
        List<Integer> vectorsId = faceService.getVectorsByEmployeeId(id);

        int vectorId = faceService.findTopSimilarFace(newFace, id);
        result.put("vectorId", vectorId);
        if(vectorId > -1) //Found a similar face, return the employee id
            result.put("employeeId", faceService.getEmployeeId(vectorId));

        return result;
    }

    @GetMapping("face/{id}")
    public List<ImageModel> getEmployeeFace(@PathVariable Integer id){
        return faceService.getEmployeeFace(id);
    }

    @DeleteMapping("/face/{id}")
    public void deleteEmployeeFace(@PathVariable Integer id){
        faceService.deleteEmployeeFace(id);
    }
    @GetMapping("face-image/{id}")
    public String getFace(@PathVariable Integer id){
        return faceService.getImage(id);
    }

    @ExceptionHandler
    public HashMap<String, Object> exceptionHandler(Exception e){
        HashMap<String, Object> res = new HashMap<>();
        res.put("cause", e.getMessage());
        res.put("message", e.toString());
        e.printStackTrace();
        return res;
    }

}