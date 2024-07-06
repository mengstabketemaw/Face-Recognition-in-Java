package com.dulcian.face.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VectorRepository extends JpaRepository<VectorModel, Integer> {
    @Query(value = "select v from VectorModel v where v.id not in (select i.id from ImageModel i where i.employeeId = ?1)")
    List<VectorModel> findAllExcluding(Integer employeeId);

}
