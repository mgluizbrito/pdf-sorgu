package io.github.mgluizbrito.PdfSorgu.config;

import com.google.genai.Client;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String OLLAMA_BASE_URL;

    @Value("${spring.ai.ollama.embedding.options.model:nomic-embed-text}")
    private String embeddingModelName;

    @Bean
    public OllamaApi ollamaApi() {
        return OllamaApi.builder().baseUrl(OLLAMA_BASE_URL).build();
    }

    @Bean
    public EmbeddingModel embeddingModel(OllamaApi ollamaApi) {
        OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder()
                .model(embeddingModelName)
                .build();

        return OllamaEmbeddingModel.builder().ollamaApi(ollamaApi).defaultOptions(options).build();
    }

    // ==================================================================================
    // GEMINI (Para ChatModel)
    // ==================================================================================

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Value("${spring.ai.google.genai.chat.options.model}")
    private String modelName;

    @Bean
    public Client googleGenAiClient(){
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public ChatModel googleGenAiChatModel(Client googleGenAiClient){
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(modelName)
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(googleGenAiClient)
                .defaultOptions(options).build();
    }

}
