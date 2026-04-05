package com.zorvyn.finance.app.filters;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class LoggingFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private static  final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. Get from header (if provided by Gateway/Load Balancer) or generate new
        String  incoming  = httpRequest.getHeader(CORRELATION_ID_HEADER);


        String correlationId = (incoming != null && UUID_PATTERN.matcher(incoming).matches())
                ? incoming
                : UUID.randomUUID().toString();


        // 2. Put in MDC for logging
        MDC.put(MDC_KEY, correlationId);

        // 3. Add to Response Header so the client can reference it
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            // 4. Clear MDC after request is finished to prevent memory leaks
            MDC.remove(MDC_KEY);
        }
    }
}
