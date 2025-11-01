package io.github.mgluizbrito.PdfSorgu.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileUploadUtils {

    /**
     * Converts a MultipartFile to a temporary file in the file system.
     * * @param multipartFile The file received from the upload form.
     *
     * @param prefix The prefix to be used in the temporary file name.
     * @return A java.io.File object that points to the temporary file on disk.
     * @throws IOException If an error occurs while reading or writing the file.
     */
    public static File convertMultipartFileToFile(MultipartFile multipartFile, String prefix) throws IOException {

        String originalFilename = Objects.requireNonNull(multipartFile.getOriginalFilename());
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        Path tempDir = Files.createTempDirectory("temp");
        tempDir.toFile().deleteOnExit();
        Path tempFilePath = tempDir.resolve(prefix + "_" + originalFilename);

        multipartFile.transferTo(tempFilePath);

        return tempFilePath.toFile();
    }

    /**
     * Cleans (deletes) the file system file.
     * * @param file The file to be deleted.
     */
    public static void cleanUpTempFile(File file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
                Files.deleteIfExists(file.toPath().getParent());
            } catch (IOException e) {
                System.err.println("Erro ao deletar o arquivo temporário: " + file.getAbsolutePath() + ". Erro: " + e.getMessage());
            }
        }
    }
}
