package io.github.mgluizbrito.PdfSorgu.repository;

import io.github.mgluizbrito.PdfSorgu.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
}
