package com.zorvyn.finance.app.controller;

import com.zorvyn.finance.app.dtos.response.DashboardResponse;
import com.zorvyn.finance.app.service.DashboardSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Summary analytics scoped to the authenticated user. Returns aggregated totals, category breakdown, recent transactions, and weekly trends.")
@SecurityRequirement(name = "cookieAuth")
public class DashboardController {

    private final DashboardSummary dashboardSummary;

    @Operation(
            summary = "Get dashboard summary",
            description = "Returns a full summary for the authenticated user: total income, total expenses, net balance, " +
                    "per-category expense breakdown, 5 most recent transactions, and net amounts for each day of the current week (Mon-Sun)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardResponse> getSummary()
    {
        return ResponseEntity.ok()
                .header("X-Dashboard-Status","Refreshed")
                .body(dashboardSummary.getDashboardSummary());
    }

}
