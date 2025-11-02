package io.github.mgluizbrito.PdfSorgu.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Email
        @Size(min = 4, max = 256)
        String email,
        @NotBlank
        @Size(min = 6, max = 256)
        String password
) {
}
