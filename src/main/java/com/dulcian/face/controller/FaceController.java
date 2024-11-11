package com.dulcian.face.controller;


import com.dulcian.face.model.ImageModel;
import com.dulcian.face.service.FaceService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public List<Integer> faceRegistration(@RequestBody String[] base64Images, @PathVariable Integer id) {
        List<Integer> result = new ArrayList<>();
        for(String base64Face : base64Images){
            Integer vectorId = faceService.saveEmployeeFace(id, base64Face);
            result.add(vectorId);
        }
        return result; // Face is registered Successfully
    }

    @PostMapping("/identify/{id}")
    public HashMap<String,Object> faceIdentification(@PathVariable Integer id, @RequestBody String base64Image) {
        HashMap<String,Object> result = new HashMap<>(); result.put("vectorId", -2);
        int vectorId = faceService.findTopSimilarFace(base64Image, id);
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