package com.dulcian.face.service;

import com.dulcian.face.model.EmployeeFacePending;
import com.dulcian.face.model.ImageModel;
import com.dulcian.face.model.ImageRepository;
import com.dulcian.face.model.PendingRepository;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class PendingService {

    private final PendingRepository pendingRepository;
    private final ImageRepository imageRepository;
    private final FaceService faceService;
    public PendingService(PendingRepository pendingRepository, ImageRepository imageRepository, FaceService faceService) {
        this.pendingRepository = pendingRepository;
        this.imageRepository = imageRepository;
        this.faceService = faceService;
    }

    public HashMap<String, Object> saveEmployeeFaceToPending(String targetFace64, List<Integer> candidatesId, int employeeId) {
        byte[] targetRaw = Base64.getDecoder().decode(targetFace64);
        EmployeeFacePending target = new EmployeeFacePending(employeeId, targetRaw);
        target = pendingRepository.save(target);
        int parentId = target.getId();
        List<ImageModel> candidates = imageRepository.findAllById(candidatesId);
        List<EmployeeFacePending> candidateImages = candidates.stream()
                .map(candidate -> new EmployeeFacePending(parentId, candidate.getEmployeeId(), candidate.getImage()))
                .collect(Collectors.toList());

        HashMap<String, Object> entry = new HashMap<>();
        entry.put("id", target.getId());
        entry.put("employeeId", target.getEmployeeId());
        entry.put("child", pendingRepository.saveAll(candidateImages));
        return entry;
    }

    public List<HashMap<String, Object>> getPendingEmployeeFaces(int employeeId){

        List<HashMap<String, Object>> result = new ArrayList<>();
        List<EmployeeFacePending> targets = pendingRepository.findByEmployeeId(employeeId);
        targets.forEach(target -> {
                HashMap<String, Object> entry = new HashMap<>();
                entry.put("id", target.getId());
                entry.put("employeeId", target.getEmployeeId());
                entry.put("child", pendingRepository.findByParent(target.getId()));
                result.add(entry);
            }
        );
        return result;
    }

    public void delete(int id){
        pendingRepository.deleteAllByParentId(id);
    }

    public ImageModel approve(int id){
        EmployeeFacePending pendingEmployeeFace = pendingRepository
                .findById(id)
                .orElseThrow(()->new RuntimeException("Pending employee face not found"));

        ImageModel integer = faceService.saveEmployeeFace(
                pendingEmployeeFace.getEmployeeId(),
                Base64.getEncoder().encodeToString(pendingEmployeeFace.getPic())
        );
        delete(id);
        return integer;
    }

    public byte[] getImage(int id){
        return pendingRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("No pending image is found " + id))
                .getPic();
    }
}
