package io.github.mgluizbrito.PdfSorgu.misc;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Interface for generating cryptographic hash values (digests)
 * for both strings (messages) and files, using various algorithms.
 */
public interface HashAlgorithm {

    /**
     * Generates the cryptographic hash (digest) for a given input message string.
     * <p>
     * The specific hashing algorithm used must be determined by the
     * concrete implementation of this interface.
     *
     * @param msg The input string message to be hashed.
     * @return The hash value as a hexadecimal string.
     */
    String msgHash(String msg);


    /**
     * Generates the cryptographic hash (digest) for the contents of a file.
     * <p>
     * This implementation reads the file in chunks (8KB buffer)
     * to efficiently handle large files without excessive memory usage.
     *
     * @param file The {@code File} object representing the file to be hashed.
     * @return The file's hash value as a hexadecimal string.
     * @throws IOException              If an I/O error occurs while reading the file.
     * @throws NoSuchAlgorithmException If the specified algorithm is not
     *                                  available in the environment.
     */
    String fileHash(File file) throws IOException, NoSuchAlgorithmException;
}
