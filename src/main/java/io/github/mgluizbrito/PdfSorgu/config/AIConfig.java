package io.github.mgluizbrito.PdfSorgu.config;

import com.google.genai.Client;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Value("${splitter.chunk-size:500}")
    private int chunkSize;

    @Value("${splitter.min-chunk-size:150}")
    private int minChunkSizeChars;

    @Value("${splitter.keep-separator:true}")
    private boolean keepSeparator;

    @Bean
    public TextSplitter textSplitter() {

        int minChunkLengthToEmbed = 50;
        int maxNumChunks = 3000;

        return new TokenTextSplitter(chunkSize, minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, keepSeparator);
    }

    // ==================================================================================
    // OLLAMA (Para EmbeddingModel)
    // ==================================================================================

    @ConditionalOnProperty(name = "spring.ai.ollama.base-url")
    @Bean
    public OllamaApi ollamaApi(
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String OLLAMA_BASE_URL
    ) {

        return OllamaApi.builder().baseUrl(OLLAMA_BASE_URL).build();
    }

    @ConditionalOnProperty(name = "spring.ai.ollama.chat.options.model")
    @Bean
    public ChatModel ollamaChatModel(
            OllamaApi ollamaApi,
            @Value("${spring.ai.ollama.chat.options.model}") String modelName
    ){

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(modelName)
                .build();

        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();
    }

    @ConditionalOnProperty(name = "spring.ai.ollama.base-url")
    @Bean
    public EmbeddingModel embeddingModel(
            OllamaApi ollamaApi,
            @Value("${spring.ai.ollama.embedding.options.model:nomic-embed-text}") String embeddingModelName
    ) {

        OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder()
                .model(embeddingModelName)
                .build();

        return OllamaEmbeddingModel.builder().ollamaApi(ollamaApi).defaultOptions(options).build();
    }

    // ==================================================================================
    // GEMINI (Para ChatModel)
    // ==================================================================================

    @ConditionalOnProperty(name = "spring.ai.google.genai.api-key")
    @Bean
    public Client googleGenAiClient(
            @Value("${spring.ai.google.genai.api-key}") String apiKey
    ){

        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @ConditionalOnProperty(name = "spring.ai.google.genai.api-key")
    @Bean
    public ChatModel googleGenAiChatModel(
            Client googleGenAiClient,
            @Value("${spring.ai.google.genai.chat.options.model}") String modelName
    ) {

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(modelName)
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(googleGenAiClient)
                .defaultOptions(options).build();
    }

}
