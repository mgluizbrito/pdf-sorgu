package io.github.mgluizbrito.PdfSorgu.config;

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
}
