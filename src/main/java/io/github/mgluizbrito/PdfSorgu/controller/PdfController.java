package io.github.mgluizbrito.PdfSorgu.controller;

import io.github.mgluizbrito.PdfSorgu.dto.UploadResponseDTO;
import io.github.mgluizbrito.PdfSorgu.exceptions.DuplicateRecordException;
import io.github.mgluizbrito.PdfSorgu.exceptions.InvalidFieldException;
import io.github.mgluizbrito.PdfSorgu.properties.FileStorageProperties;
import io.github.mgluizbrito.PdfSorgu.service.DocumentService;
import io.github.mgluizbrito.PdfSorgu.service.HashService;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
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

import static io.github.mgluizbrito.PdfSorgu.utils.FileUploadUtils.*;

@RestController
@RequestMapping("v1/pdf")
public class PdfController {

    private final DocumentService service;
    private final Path fileStorageLocation;
    private final HashService hash;

    @SneakyThrows
    @PostMapping
    public ResponseEntity<UploadResponseDTO> upload(@RequestParam("file") MultipartFile file){
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
        try (InputStream is = new FileInputStream(tempFile)) { fileHash = hash.calculateFileHash(is); }
        if (service.existsByHash(fileHash)) throw new DuplicateRecordException("Duplicate document already processed: " + newFileId);

        try{

            Path targetLocation = fileStorageLocation.resolve(newFileName);
            file.transferTo(targetLocation);

            service.processDocument(newFileId, targetLocation, fileName, fileHash);

            String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/v1/files/")
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

        }finally {
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
