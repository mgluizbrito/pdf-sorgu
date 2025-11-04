package io.github.mgluizbrito.PdfSorgu.controller;

import io.github.mgluizbrito.PdfSorgu.dto.UploadResponseDTO;
import io.github.mgluizbrito.PdfSorgu.exceptions.DuplicateRecordException;
import io.github.mgluizbrito.PdfSorgu.exceptions.InvalidFieldException;
import io.github.mgluizbrito.PdfSorgu.properties.FileStorageProperties;
import io.github.mgluizbrito.PdfSorgu.service.DocumentService;
import io.github.mgluizbrito.PdfSorgu.service.HashService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.github.mgluizbrito.PdfSorgu.utils.FileUploadUtils.cleanUpTempFile;
import static io.github.mgluizbrito.PdfSorgu.utils.FileUploadUtils.convertMultipartFileToFile;

@RestController
@RequestMapping("v1/pdf")
@Tag(name = "PDF Upload", description = "Endpoint for securely uploading and processing PDF documents for RAG.")
public class PdfController {

    private final DocumentService service;
    private final Path fileStorageLocation;
    private final HashService hash;

    @SneakyThrows
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload PDF Document",
            description = "Uploads a PDF file, calculates its hash, checks for duplicates, and initiates document processing for the RAG system.",
            security = @SecurityRequirement(name = "Bearer"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "PDF uploaded and processing initiated successfully.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UploadResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid file type (not PDF), file is empty, or internal I/O error."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT token)."),
                    @ApiResponse(responseCode = "409", description = "Duplicate document detected (file with same hash already exists).",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DuplicateRecordException.class))) // Use a exceção ou um DTO de erro
            }
    )
    public ResponseEntity<UploadResponseDTO> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) throw new InvalidFieldException("file", "You must to pass a file");

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        UUID newFileId = UUID.randomUUID();
        String newFileName = newFileId.toString() + "." + StringUtils.getFilenameExtension(fileName);

        final String PDF_MIME_TYPE = "application/pdf";
        if (!PDF_MIME_TYPE.equalsIgnoreCase(file.getContentType())) {
            throw new InvalidFieldException("file", "File type not supported. Only PDF files are allowed (Expected: " + PDF_MIME_TYPE + ", Received: " + file.getContentType() + ").");
        }

        String fileHash;
        File tempFile = convertMultipartFileToFile(file, "hash_validation");
        try (InputStream is = new FileInputStream(tempFile)) {
            fileHash = hash.calculateFileHash(is);
        }
        if (service.existsByHash(fileHash))
            throw new DuplicateRecordException("Duplicate document already processed: " + newFileId);

        try {

            Path targetLocation = fileStorageLocation.resolve(newFileName);
            file.transferTo(targetLocation);

            service.processDocument(newFileId, targetLocation, fileName, fileHash);

            String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files/")
                    .path(newFileName)
                    .toUriString();

            return new ResponseEntity<UploadResponseDTO>(new UploadResponseDTO(
                    "PDF uploaded successfully!",
                    fileName,
                    newFileId,
                    fileUri
            ), HttpStatus.CREATED);

        } catch (IOException e) {
            return ResponseEntity.badRequest().build();

        } finally {
            cleanUpTempFile(tempFile);
        }
    }

    /*
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws IOException {
        Path filePath = fileStorageLocation.resolve(fileName).normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
     */

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() throws IOException {
        List<String> fileNames = Files.list(fileStorageLocation)
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();

        return ResponseEntity.ok(fileNames);
    }

    public PdfController(DocumentService service, HashService hash, FileStorageProperties fileStorageProperties) {
        this.service = service;
        this.hash = hash;
        fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
    }
}
