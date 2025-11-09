package io.github.mgluizbrito.PdfSorgu.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Bucket4jRateLimitFilter extends OncePerRequestFilter {

    private final BucketManagerService service;

    // Rotas de acesso livre (a serem ignoradas pelo filtro)
    private static final List<String> FREE_ACCESS_PATHS = List.of(
            "/auth/", "/files/", "/v1/api-docs", "/v2/api-docs", "/v3/api-docs",
            "/swagger-resources", "/swagger-ui.html", "/swagger-ui/", "/webjars/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return FREE_ACCESS_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Identificar a Operação
        Boolean isUpload = isRateLimitedOperation(request);
        if (isUpload == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());

        String identifier = isAuthenticated ? auth.getName() : request.getRemoteAddr();

        // 3. Resolver e Consumir o Token no Bucket
        Bucket bucket = service.resolveBucket(identifier, isUpload, isAuthenticated);

        // Tenta consumir 1 token.
        if (bucket.tryConsume(1)) {
            // Permitido: Continua
            filterChain.doFilter(request, response);
        } else {
            // Negado: Limite excedido (429 Too Many Requests)
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Quota limit exceeded. Please try again later.\"}");
        }
    }

    /**
     * Returns TRUE if upload, FALSE if query, and NULL if none of the limited operations.
     */
    private Boolean isRateLimitedOperation(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/v1/pdf") && "POST".equalsIgnoreCase(method)) return true;

        // Query RAG: GET /v1/query/{fileId}
        if (path.startsWith("/v1/query") && "GET".equalsIgnoreCase(method) && path.split("/").length >= 4) return false;

        return null;
    }
}
