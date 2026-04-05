package com.zorvyn.finance.app.service.impl;

import com.zorvyn.finance.app.dtos.request.FinancialRecordRequest;
import com.zorvyn.finance.app.dtos.response.PageResponse;
import com.zorvyn.finance.app.entity.FinancialRecord;
import com.zorvyn.finance.app.entity.User;
import com.zorvyn.finance.app.entity.enums.Category;
import com.zorvyn.finance.app.entity.enums.TransactionType;
import com.zorvyn.finance.app.exception.AuthException;
import com.zorvyn.finance.app.exception.ResourceNotFoundException;
import com.zorvyn.finance.app.projection.FinancialRecordProjection;
import com.zorvyn.finance.app.repository.FinancialRepository;
import com.zorvyn.finance.app.service.FinancialService;
import com.zorvyn.finance.app.specification.FinancialSpecification;
import com.zorvyn.finance.app.utils.IdentifierUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialServiceImpl implements FinancialService {

    private final FinancialRepository financialRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public FinancialRecordProjection createRecord(FinancialRecordRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Creating financial record | userId={} type={} category={} amount={}",
                currentUser.getId(), request.type(), request.category(), request.amount());

        FinancialRecord record = modelMapper.map(request, FinancialRecord.class);
        record.setCreatedBy(currentUser);
        FinancialRecord saved = financialRepository.save(record);

        log.info("Financial record created successfully | recordId={} userId={}",
                saved.getId(), currentUser.getId());

        return financialRepository.findProjectedById(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error retrieving created record"));
    }

    @Override
    @Transactional
    public FinancialRecordProjection updateRecord(String id, FinancialRecordRequest request) {

        log.info("Updating financial record | recordId={}", id);

        FinancialRecord record = getAndVerifyOwnership(id);
        modelMapper.map(request, record);
        financialRepository.save(record);

        log.info("Financial record updated successfully | recordId={}", id);

        return financialRepository.findProjectedById(record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error retrieving updated record: " +id));
    }

    @Override
    @Transactional
    public void deleteRecord(String id) {
        log.info("Deleting financial record | recordId={}", id);
        FinancialRecord record = getAndVerifyOwnership(id);
        financialRepository.delete(record);
        log.info("Financial record soft-deleted successfully | recordId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialRecordProjection getRecordById(String id) {
        log.info("Fetching financial record by id | recordId={}", id);
        UUID uuid = IdentifierUtils.parseUuid(id);
        FinancialRecordProjection projection = financialRepository.findProjectedById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found: " + id));
        verifyOwnership(projection.getCreatedBy().getId());
        return projection;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinancialRecordProjection> getAllRecords(TransactionType type, Category category, LocalDateTime start, LocalDateTime end,String search, Pageable pageable) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Fetching all financial records | userId={} type={} category={} page={} size={}",
                currentUser.getId(), type, category, pageable.getPageNumber(), pageable.getPageSize());

        Specification<FinancialRecord> spec = Specification.where(FinancialSpecification.hasUser(currentUser.getId()))
                .and(FinancialSpecification.hasType(type))
                .and(FinancialSpecification.hasCategory(category))
                .and(FinancialSpecification.createdBetween(start, end))
                .and(FinancialSpecification.descriptionContains(search));

        Page<FinancialRecordProjection> pageData = financialRepository.findAllBy(spec, pageable);

        log.info("Financial records fetch complete | userId={} totalElements={} totalPages={}",
                currentUser.getId(), pageData.getTotalElements(), pageData.getTotalPages());


        return PageResponse.of(pageData);
    }


    // -- Helper Methods.
    private FinancialRecord getAndVerifyOwnership(String id) {
        FinancialRecord record = financialRepository.findById(IdentifierUtils.parseUuid(id))
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        verifyOwnership(record.getCreatedBy().getId());
        return record;
    }

    private void verifyOwnership(UUID ownerId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

//        if (currentUser.getRole() == Role.ADMIN) {
//        log.info("Ownership check bypassed — ADMIN access | adminId={} recordOwnerId={}",
//                currentUser.getId(), ownerId);
//            return;
//        }

        if (!ownerId.equals(currentUser.getId())) {
            log.warn("Unauthorized record access attempt | requesterId={} recordOwnerId={}",
                    currentUser.getId(), ownerId);
            throw new AuthException("Access Denied: You do not have permission to access this record");
        }
    }
}
