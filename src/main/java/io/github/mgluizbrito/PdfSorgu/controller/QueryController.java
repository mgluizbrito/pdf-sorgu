package io.github.mgluizbrito.PdfSorgu.controller;

import io.github.mgluizbrito.PdfSorgu.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("v1/query")
@RequiredArgsConstructor
public class QueryController {

    private final QueryService service;

    @GetMapping("{fileId}")
    public ResponseEntity<String> query(
            @PathVariable("fileId") UUID fileId,
            @RequestParam("q") String query,
            @RequestParam(value = "topK", defaultValue = "3") Integer topK
    ) {

        String response = service.processQuery(fileId, query);
        return ResponseEntity.ok(response);
    }
}
