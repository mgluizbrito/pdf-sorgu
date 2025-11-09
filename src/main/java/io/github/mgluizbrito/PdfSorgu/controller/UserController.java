package io.github.mgluizbrito.PdfSorgu.controller;

import io.github.mgluizbrito.PdfSorgu.dto.UserStatusResponse;
import io.github.mgluizbrito.PdfSorgu.security.BucketManagerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final BucketManagerService bucketService;
    private final HttpServletRequest request;

    @GetMapping("/status")
    @Operation(summary = "Get User Quota Status", description = "Returns the user's current rate limit status (uploads and queries).")
    public ResponseEntity<UserStatusResponse> status(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        String identifier = getClientIdentifier(auth);

        Map<String, Integer> quotaStatus = bucketService.getQuotaStatus(identifier, isAuthenticated);

        UserStatusResponse status = new UserStatusResponse(
                isAuthenticated,
                identifier,
                quotaStatus.get("maxUploads"),
                quotaStatus.get("usedUploads"),
                quotaStatus.get("maxQueries"),
                quotaStatus.get("usedQueries")
        );

        return ResponseEntity.ok(status);
    }

    private String getClientIdentifier(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }

        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedForHeader)) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
