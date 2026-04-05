package com.zorvyn.finance.app.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CookieService {

    @Value("${app.security.cookie.name}")
    private String cookieName;
    @Value("${app.security.cookie.http-only}")
    private boolean httpOnly;
    @Value("${app.security.cookie.max-age-seconds}")
    private int maxAge;
    @Value("${app.security.cookie.same-site}")
    private String sameSite;
    @Value("${app.security.cookie.secure}")
    private Boolean secure;


    public HttpCookie createTokenCookie(String token){
        return ResponseCookie.from(cookieName,token)
                .httpOnly(httpOnly)
                .secure(secure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }

    public String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public HttpCookie deleteTokenCookie() {
        return ResponseCookie.from(cookieName, "")
                .maxAge(0).path("/").build();
    }


}
