package io.github.mgluizbrito.PdfSorgu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final VectorStore vector;
    private final ChatModel chatModel;

    @Value("classpath:prompts/rag-prompt.st")
    private Resource ragPrompt;

    public String processQuery(UUID fileId, String query) {

        final String filterExpression = "document_id == '" + fileId.toString() + "'";

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .filterExpression(filterExpression)
                .build();

        List<Document> documentsContext = vector.similaritySearch(searchRequest);

        return generateResponseWithContext(query, documentsContext);
    }

    public String processQuery(UUID fileId, String query, int topK) {

        final String filterExpression = "document_id == '" + fileId.toString() + "'";

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .filterExpression(filterExpression)
                .topK(topK)
                .build();

        List<Document> documentsContext = vector.similaritySearch(searchRequest);

        return generateResponseWithContext(query, documentsContext);
    }

    @Deprecated
    public String processGlobalQuery(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        List<Document> documentContext = vector.similaritySearch(searchRequest);

        return generateResponseWithContext(query, documentContext);
    }

    private String generateResponseWithContext(String query, List<Document> documentsContext) {

        String context = documentsContext.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n---\n"));

        PromptTemplate promptTemplate = new PromptTemplate(ragPrompt);

        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("question", query);
        promptParams.put("context", context);

        Prompt prompt = promptTemplate.create(promptParams);

        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
