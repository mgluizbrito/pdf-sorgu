package io.github.mgluizbrito.PdfSorgu.service;

import io.github.mgluizbrito.PdfSorgu.model.Chunk;
import io.github.mgluizbrito.PdfSorgu.model.Document;
import io.github.mgluizbrito.PdfSorgu.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository repository;
    private final ChunkService chunkService;
    private final TextSplitter splitter;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public Document save(Document document) {
        return repository.save(document);
    }

    @Transactional
    public void processDocument(UUID fileId, Path filePath, String fileName, String fileHash) throws IOException {
        File documentFile = filePath.toFile();

        DocumentReader reader = pdfDocumentReader(new FileSystemResource(documentFile));
        List<org.springframework.ai.document.Document> pages = reader.get();
        List<org.springframework.ai.document.Document> chunksSpringAi = splitter.apply(pages);

        Document newDoc = new Document();
        newDoc.setId(fileId);
        newDoc.setHash(fileHash);
        newDoc.setFileName(fileName);
        newDoc.setUploadDate(LocalDateTime.now());
        Document managedDoc = repository.save(newDoc);

        List<Chunk> chunksJpa = new ArrayList<>();
        AtomicInteger chunksOrder = new AtomicInteger();

        chunksSpringAi.forEach(chunk -> {
            float[] embeddingVector = embeddingModel.embed(chunk.getFormattedContent());

            Chunk chunkJpa = new Chunk();
            chunkJpa.setDocument(managedDoc);
            chunkJpa.setContent(chunk.getFormattedContent());
            chunkJpa.setChunkOrder(chunksOrder.getAndIncrement());
            chunkJpa.setEmbedding(convertEmbeddingToString(embeddingVector));
            chunksJpa.add(chunkJpa);

            chunk.getMetadata().put("document_id", fileId.toString());
            chunk.getMetadata().put("document_hash", fileHash);
        });

        chunkService.saveAll(chunksJpa);
        vectorStore.add(chunksSpringAi);
    }

    private DocumentReader pdfDocumentReader(Resource pdfResource) {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(0)
                .build();

        return new PagePdfDocumentReader(pdfResource, config);
    }

    private String convertEmbeddingToString(float[] embedding) {
        if (embedding == null || embedding.length == 0) return "{}";

        return "{" + Stream.of(embedding)
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "}";
    }

    public boolean existsByHash(String fileHash) {
        return repository.existsByHash(fileHash);
    }
}
