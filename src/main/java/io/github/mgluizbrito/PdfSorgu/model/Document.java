package io.github.mgluizbrito.PdfSorgu.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "document", schema = "public")
@Getter
@Setter
public class Document {

    @Id
    private UUID id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_hash", unique = true)
    private String hash;

    @Column(name = "upload", nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Chunk> chunks;
}
