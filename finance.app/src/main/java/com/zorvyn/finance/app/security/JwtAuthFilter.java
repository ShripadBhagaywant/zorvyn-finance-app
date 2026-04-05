package com.zorvyn.finance.app.security;

import com.zorvyn.finance.app.exception.TokenBlackListedException;
import com.zorvyn.finance.app.repository.BlackListedTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final CustomUserDetailsService userDetailsService;
    private final BlackListedTokenRepository blacklistRepo;
    private final HandlerExceptionResolver resolver;

    public JwtAuthFilter(
            JwtService jwtService,
            CookieService cookieService,
            CustomUserDetailsService userDetailsService,
            BlackListedTokenRepository blacklistRepo,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.cookieService = cookieService;
        this.userDetailsService = userDetailsService;
        this.blacklistRepo = blacklistRepo;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String jwt = cookieService.extractToken(request);

        if(jwt == null){
            filterChain.doFilter(request,response);
            return;
        }

        try{

            String jti = jwtService.extractJti(jwt);

            if(blacklistRepo.existsByJti(jti)){
                throw new TokenBlackListedException("Token has been invalidated. Please login again.");
            }
            String userEmail = jwtService.extractEmail(jwt);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request,response);
        }catch (Exception e){
            resolver.resolveException(request,response,null,e);
        }

    }
}
