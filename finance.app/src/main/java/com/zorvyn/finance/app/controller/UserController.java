package com.zorvyn.finance.app.controller;

import com.zorvyn.finance.app.dtos.request.UserRegisterRequestDto;
import com.zorvyn.finance.app.dtos.response.ApiError;
import com.zorvyn.finance.app.dtos.response.PageResponse;
import com.zorvyn.finance.app.dtos.response.UserRegisterResponse;
import com.zorvyn.finance.app.entity.enums.Role;
import com.zorvyn.finance.app.entity.enums.Status;
import com.zorvyn.finance.app.service.UserService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration and management. Registration is public. All other operations require authentication and appropriate roles.")
public class UserController {

    private final UserService userService;


    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. The default role is VIEWER. Email must be unique across active accounts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Email already in use or belongs to a deleted account",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequestDto request) {
        UserRegisterResponse response = userService.registerUser(request);

        // Build the Location header for the newly created resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location)
                .header("X-Registration-Source", "Zorvyn-Auth-Service")
                .body(response);
    }


    @Operation(
            summary = "List all users",
            description = "Returns a paginated list of users. Supports filtering by email (partial match), role, and status. Requires ADMIN or ANALYST role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<PageResponse<UserRegisterResponse>> getAllUsers(
            @Parameter(description = "Filter by email (partial, case-insensitive)")
            @RequestParam(required = false) String email,
            @Parameter(description = "Filter by role")
            @RequestParam(required = false) Role role,
            @Parameter(description = "Filter by account status")
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(userService.getAllUsers(email, role, status, pageable));
    }


    @Operation(
            summary = "Get a user by ID",
            description = "Returns a single user. ADMIN can fetch any user. A non-admin user can only fetch their own profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Attempting to access another user's profile without ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<UserRegisterResponse> getUserById( @Parameter(description = "User UUID")@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Update a user's role",
            description = "Changes the role of a user. Only ADMIN can perform this action."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can update roles"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<UserRegisterResponse> updateRole(
            @Parameter(description = "User UUID") @PathVariable String id,
            @Parameter(description = "New role to assign", schema = @Schema(allowableValues = {"VIEWER", "ANALYST", "ADMIN"}))@RequestParam Role role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }


    @Operation(
            summary = "Update a user's status",
            description = "Activates or deactivates a user account. Inactive users cannot log in. Only ADMIN can perform this action."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can update status"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<UserRegisterResponse> updateStatus(
            @Parameter(description = "User UUID")@PathVariable String id,
            @Parameter(description = "New status", schema = @Schema(allowableValues = {"ACTIVE", "INACTIVE"})) @RequestParam Status status) {
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }


    @Operation(
            summary = "Delete a user",
            description = "Soft-deletes a user account (sets is_deleted = true). The user cannot log in after deletion. Their email cannot be re-registered without admin support. Only ADMIN can perform this action."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can delete users"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<Void> deleteUser( @Parameter(description = "User UUID")@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent()
                .header("X-Action-Info", "User marked as deleted")
                .build();
    }
}