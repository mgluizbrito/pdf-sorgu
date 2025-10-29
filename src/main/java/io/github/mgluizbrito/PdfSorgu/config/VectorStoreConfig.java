package io.github.mgluizbrito.PdfSorgu.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

//@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String OLLAMA_BASE_URL;

    @Value("${spring.ai.ollama.embedding.options.nomic-embed-text}")
    private String EMBEDDING_MODEL_NAME;

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingModel embeddingModel(OllamaEmbeddingModel ollamaEmbeddingModel) {
        return ollamaEmbeddingModel;
    }

    @Bean
    @ConditionalOnMissingBean
    public VectorStore vectorStore(DataSource dataSource, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder((JdbcTemplate) dataSource, embeddingModel).build();
    }
}
