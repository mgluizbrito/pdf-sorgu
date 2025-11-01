package io.github.mgluizbrito.PdfSorgu.service;

import io.github.mgluizbrito.PdfSorgu.model.Chunk;
import io.github.mgluizbrito.PdfSorgu.repository.ChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChunkService {

    private final ChunkRepository repository;

    public Chunk save(Chunk chunk) {
        return repository.save(chunk);
    }

    public List<Chunk> saveAll(List<Chunk> chunks) {
        return repository.saveAll(chunks);
    }
}
