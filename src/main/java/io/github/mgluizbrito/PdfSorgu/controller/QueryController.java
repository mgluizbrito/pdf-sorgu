package io.github.mgluizbrito.PdfSorgu.controller;

import io.github.mgluizbrito.PdfSorgu.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("v1/query")
@RequiredArgsConstructor
@Tag(name = "Query", description = "Query based on a PDF endpoint")
public class QueryController {

    private final QueryService service;

    @GetMapping("{fileId}")
    @Operation(
            summary = "Query PDF Content",
            description = "Allows you to query information based on a specific PDF file ID (RAG system).",
            security = @SecurityRequirement(name = "Bearer"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "LLM response generated successfully."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT token)."),
                    @ApiResponse(responseCode = "404", description = "File ID not found or invalid.")
            }
    )
    public ResponseEntity<String> query(
            @PathVariable("fileId") UUID fileId,
            @RequestParam("q") String query,
            @RequestParam(value = "topK", defaultValue = "3") Integer topK
    ) {

        String response = service.processQuery(fileId, query);
        return ResponseEntity.ok(response);
    }
}
