package io.github.mgluizbrito.PdfSorgu.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String name,
        String accessToken,
        LocalDateTime expiresIn
) {
}
