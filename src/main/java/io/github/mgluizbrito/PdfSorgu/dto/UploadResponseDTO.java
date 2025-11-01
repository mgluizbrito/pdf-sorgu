package io.github.mgluizbrito.PdfSorgu.dto;

import java.util.UUID;

public record UploadResponseDTO(
        String msg,
        String fileName,
        UUID fileId,
        String fileUri
) {
}
