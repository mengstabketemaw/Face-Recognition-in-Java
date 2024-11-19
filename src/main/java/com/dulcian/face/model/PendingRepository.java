package com.dulcian.face.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PendingRepository extends JpaRepository<EmployeeFacePending, Integer> {
    @Query("select e from EmployeeFacePending e where e.employeeId = ?1 and e.parent is null")
    List<EmployeeFacePending> findByEmployeeId(Integer employeeId);

    @Query("select e from EmployeeFacePending e where e.parent = ?1")
    List<EmployeeFacePending> findByParent(Integer parent);

    @Transactional
    @Modifying
    @Query("delete from EmployeeFacePending e where e.id = ?1 or e.parent = ?1")
    void deleteAllByParentId(Integer id);


}
