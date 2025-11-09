package io.github.mgluizbrito.PdfSorgu.dto;

import io.github.mgluizbrito.PdfSorgu.model.RoleEnum;

public record UserStatusResponse(
        boolean isAuthenticated,
        String identifier,

        int maxUploads,
        int usedUploads,

        int maxQueries,
        int usedQueries
    ) {
}
