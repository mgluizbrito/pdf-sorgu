package io.github.mgluizbrito.PdfSorgu.repository;

import io.github.mgluizbrito.PdfSorgu.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
}
