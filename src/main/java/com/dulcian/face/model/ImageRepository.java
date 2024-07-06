package com.dulcian.face.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageModel, Integer> {
    @Query("select i.id from ImageModel i where i.employeeId = ?1")
    List<Integer> findVectorId(Integer employeeId);

    @Query("select i from ImageModel i where i.employeeId = ?1")
    List<ImageModel> findByEmployeeId(Integer employeeId);

}
