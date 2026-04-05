package com.zorvyn.finance.app.controller;

import com.zorvyn.finance.app.dtos.request.FinancialRecordRequest;
import com.zorvyn.finance.app.dtos.response.ApiError;
import com.zorvyn.finance.app.dtos.response.PageResponse;
import com.zorvyn.finance.app.entity.enums.Category;
import com.zorvyn.finance.app.entity.enums.TransactionType;
import com.zorvyn.finance.app.projection.FinancialRecordProjection;
import com.zorvyn.finance.app.service.FinancialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/financial-records")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "Create, read, update, and delete personal financial records. All operations are scoped to the authenticated user. ADMIN can access any record.")
@SecurityRequirement(name = "cookieAuth")
public class FinancialController {

    private final FinancialService financialService;

    @Operation(
            summary = "Create a financial record",
            description = "Creates a new income or expense record linked to the authenticated user. Requires ANALYST or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Record created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient role ")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinancialRecordProjection> createRecord(@Valid @RequestBody FinancialRecordRequest request){
        FinancialRecordProjection response = financialService.createRecord(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location)
                .header("X-Record-Action","Created")
                .body(response);
    }

    @Operation(
            summary = "List financial records",
            description = "Returns a paginated list of the authenticated user's financial records. " +
                    "Supports filtering by type, category, date range, and keyword search on the description field."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Records returned successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<PageResponse<FinancialRecordProjection>> getAll(

            @Parameter(description = "Filter by transaction type")
            @RequestParam(required = false) TransactionType type,

            @Parameter(description = "Filter by category")
            @RequestParam(required = false) Category category,

            @Parameter(description = "Filter from this date (ISO 8601, e.g. 2024-01-01T00:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,

            @Parameter(description = "Filter up to this date (ISO 8601, e.g. 2024-12-31T23:59:59)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,

            @Parameter(description = "Keyword search — matches against description (case-insensitive, partial match)")
            @RequestParam(required = false) String search,

            @PageableDefault(size = 10) Pageable pageable)
    {
        return ResponseEntity.ok()
                .header("X-Query-Status", "Successful")
                .body(financialService.getAllRecords(type, category, start, end, search,pageable));
    }

    @Operation(
            summary = "Get a financial record by ID",
            description = "Returns a single record. The record must belong to the authenticated user, or the user must be an ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Record found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or record belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Record not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<FinancialRecordProjection> getById( @Parameter(description = "Record UUID") @PathVariable String id) {
        return ResponseEntity.ok(financialService.getRecordById(id));
    }

    @Operation(
            summary = "Update a financial record",
            description = "Fully replaces a record's fields. Ownership is enforced — only the record owner or an ADMIN can update. Requires ANALYST or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated or not the record owner"),
            @ApiResponse(responseCode = "403", description = "Insufficient role"),
            @ApiResponse(responseCode = "404", description = "Record not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<FinancialRecordProjection> update(
            @Parameter(description = "Record UUID")@PathVariable String id, @Valid @RequestBody FinancialRecordRequest request) {
        return ResponseEntity.ok()
                .header("X-Record-Action", "Updated")
                .body(financialService.updateRecord(id, request));
    }

    @Operation(
            summary = "Delete a financial record",
            description = "Soft-deletes a record (sets is_deleted = true). Only ADMIN can delete records. The record is not permanently removed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Record deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can delete records"),
            @ApiResponse(responseCode = "404", description = "Record not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete( @Parameter(description = "Record UUID")@PathVariable String id) {
        financialService.deleteRecord(id);
        return ResponseEntity.noContent()
                .header("X-Record-Action", "Deleted")
                .build();
    }

}
