package com.zorvyn.finance.app.service;

import com.zorvyn.finance.app.dtos.request.FinancialRecordRequest;
import com.zorvyn.finance.app.dtos.response.PageResponse;
import com.zorvyn.finance.app.entity.enums.Category;
import com.zorvyn.finance.app.entity.enums.TransactionType;
import com.zorvyn.finance.app.projection.FinancialRecordProjection;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface FinancialService {

    FinancialRecordProjection createRecord(FinancialRecordRequest request);
    FinancialRecordProjection updateRecord(String id, FinancialRecordRequest request);
    void deleteRecord(String id);
    FinancialRecordProjection getRecordById(String id);
    PageResponse<FinancialRecordProjection> getAllRecords(TransactionType type, Category category, LocalDateTime start, LocalDateTime end,String search, Pageable pageable);

}
