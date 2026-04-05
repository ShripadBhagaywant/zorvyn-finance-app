package com.zorvyn.finance.app.repository;


import com.zorvyn.finance.app.entity.FinancialRecord;
import com.zorvyn.finance.app.projection.FinancialRecordProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialRepository extends JpaRepository<FinancialRecord , UUID>, JpaSpecificationExecutor<FinancialRecord> {

    Page<FinancialRecordProjection> findAllBy(Specification<FinancialRecord> spec, Pageable pageable);

    Optional<FinancialRecordProjection> findProjectedById(UUID id);
}
