package com.dulcian.face.controller;


import com.dulcian.face.dto.FaceSimilaritySearch;
import com.dulcian.face.model.ImageModel;
import com.dulcian.face.service.FaceService;
import com.dulcian.face.service.PendingService;
import com.dulcian.face.utils.EncryptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
class FaceController{

    private final FaceService faceService;
    private final PendingService pendingService;

    FaceController(FaceService faceService, PendingService pendingService) {
        this.faceService = faceService;
        this.pendingService = pendingService;
    }

    @PostMapping("/face/{id}")
    public List<Integer> faceRegistration(@RequestBody String[] base64Images, @PathVariable Integer id) {
        List<Integer> result = new ArrayList<>();
        for(String base64Face : base64Images){
            Integer vectorId = faceService.saveEmployeeFace(id, base64Face).getId();
            result.add(vectorId);
        }
        return result; // Face is registered Successfully
    }

    @PostMapping(value = "/identify/{id}", consumes = "text/plain")
    public List<FaceSimilaritySearch> faceIdentification(@PathVariable Integer id, @RequestBody String base64Image) {
        return faceService.findTopSimilarFace(base64Image, id);
    }
    @GetMapping("face/{id}")
    public List<ImageModel> getEmployeeFace(@PathVariable Integer id){
        return faceService.getEmployeeFace(id);
    }
    @DeleteMapping("/face/{id}")
    public void deleteEmployeeFace(@PathVariable Integer id){
        faceService.deleteEmployeeFace(id);
    }
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getFace(@PathVariable Integer id, @RequestParam(required = false, defaultValue = "false") Boolean pending){
        byte[] image;
        if(pending){
            image = pendingService.getImage(id);
        }else{
            image = faceService.getImage(id);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(EncryptionUtils.getType(image))
                .body(image);
    }
    @PostMapping(value = "/pending/{id}", consumes = "text/plain")
    public HashMap<String, Object>  savePendingFaceImages(@PathVariable Integer id, @RequestParam List<Integer> candidates, @RequestBody String base64Images){
        return pendingService.saveEmployeeFaceToPending(base64Images, candidates, id);
    }
    @GetMapping("/pending/{id}")
    public List<HashMap<String, Object>> getEmployeePendingImages(@PathVariable Integer id){
        return pendingService.getPendingEmployeeFaces(id);
    }
    @PutMapping("/pending/{id}")
    public ImageModel approveFaceImage(@PathVariable Integer id){
        return pendingService.approve(id);
    }
    @DeleteMapping("/pending/{id}")
    public void deletePendingFaceImage(@PathVariable Integer id){
        pendingService.delete(id);;
    }

    @ExceptionHandler
    public ResponseEntity<HashMap<String, Object>> exceptionHandler(Exception e){

        HashMap<String, Object> res = new HashMap<>();
        res.put("cause", e.getMessage());
        res.put("message", e.toString());
        e.printStackTrace();
        return ResponseEntity
                .badRequest()
                .body(res);
    }

}