package io.github.mgluizbrito.PdfSorgu.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "file_chunk", schema = "public")
@Getter
@Setter
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /*
    O vetor de embedding.
    Para o pgvector, usamos columnDefinition="VECTOR".
    No entanto, no Spring AI, o PgVectorStore é responsável por manipular
    esta coluna (como float[] ou List<Float>) diretamente via JDBC,
    muitas vezes ignorando o mapeamento JPA complexo. Usamos String como placeholder
    para a entidade JPA, mas na prática, a lógica de persistência do vetor é do VectorStore.
    */
    @Column(name = "embedding", columnDefinition = "VECTOR")
    private String embedding;

    @Column(name = "chunk_order")
    private Integer chunkOrder;
}
