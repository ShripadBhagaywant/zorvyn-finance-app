package com.zorvyn.finance.app.controller;

import com.zorvyn.finance.app.dtos.request.UserLoginRequest;
import com.zorvyn.finance.app.dtos.response.ApiError;
import com.zorvyn.finance.app.dtos.response.UserLoginResponse;
import com.zorvyn.finance.app.security.CookieService;
import com.zorvyn.finance.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and logout. Authentication uses an HttpOnly cookie (zorvyn_at) containing a JWT. The token is also returned in the response body for non-browser clients.")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @Operation(
            summary = "Login",
            description = "Authenticates a user with email and password. On success, sets an HttpOnly JWT cookie (zorvyn_at) and returns the token in the response body. " +
                    "The account is locked for a period after 5 consecutive failed attempts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful. Cookie set in Set-Cookie header."),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or malformed fields",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials, account disabled, or account locked",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "No account found with the given email",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response) {

        UserLoginResponse loginResponse = authService.login(request, response);

        // Professional Touch: Point to the User's Resource in the Location Header.
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(loginResponse.getUser().getId())
                .toUri();

        return ResponseEntity.ok()
                .location(location)
                .header("X-Auth-Status", "Authenticated")
                .header("X-Token-Type", loginResponse.getTokenType())
                .body(loginResponse);
    }

    @Operation(
            summary = "Logout",
            description = "Invalidates the current JWT by adding its JTI to the blacklist. Clears the auth cookie. " +
                    "If no cookie is present, the endpoint still returns 204 — it is safe to call unconditionally."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logged out successfully. Cookie cleared."),
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String token = cookieService.extractToken(request);
        if (token != null) {
            authService.logout(token, response);
        }

        return ResponseEntity.noContent()
                .header("X-Logout-Status", "Success")
                .header(HttpHeaders.SET_COOKIE, cookieService.deleteTokenCookie().toString())
                .build();
    }

}
