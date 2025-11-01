# PdfSorgu: Sistema RAG com Spring AI, pgvector e compatibilidade Multi-Modelo 

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

**PdfSorgu** é um sistema avançado de Geração Aumentada por Recuperação (RAG), construído inteiramente sobre o ecossistema Spring AI. Este projeto permite que os usuários façam upload de documentos PDF e, em seguida, realizem consultas em linguagem natural, recebendo respostas concisas e fundamentadas, baseadas exclusivamente no conteúdo desses documentos.

O projeto se diferencia por usar uma arquitetura "RAG na mão", orquestrando os componentes do Spring AI para um controle granular sobre o fluxo de dados. E permitindo a facil integração com modelos de IA variados:

### Modelos de IA pré-implementados até o momento:

* **Modelos de CHAT:** `mistral`(Ollama) e `gemini-[2.0, 2.5]-[flash-lite, flash, pro]`
* **Modelos de Embedding:** `ollama-nomic-embed-text`

**Caso tenha interesse de implementar um modelo customizado, basta criar um Application Profile (`src/main/resources/application-[model_name].yaml`) configurado e CRIAR OS BEANS MANUALMENTE das interfaces `ChatModel` e `EmbeddingModel`, assim como segue o padrão em `src/main/java/io/github/mgluizbrito/PdfSorgu/config/AIConfig.java`**

## Arquitetura RAG

O PdfSorgu é dividido em dois pipelines principais: Ingestão de Dados e Consulta (RAG).

### 1. Pipeline de Ingestão de Dados (Upload do PDF)

O objetivo é processar um arquivo PDF, transformá-lo em vetores (embeddings) e persistir esses dados no PostgreSQL com a extensão pgvector.

| Etapa | Ação | Componente Principal                    |
| :--- | :--- |:----------------------------------------|
| 1. Upload | Usuário envia o arquivo PDF via API REST. | `PdfController (@RestController)`       |
| 2. Metadados | Calcula o Hash do arquivo e verifica se ele já existe no banco (hash único). | `PdfService` + Repositório JPA          |
| 3. Extração | Lê o fluxo de bytes do PDF e extrai o texto completo. | Apache PDFBox                           |
| 4. Chunking | Divide o texto completo em pedaços menores (Chunks/Documents) para melhor granularidade de busca. | `TextSplitter` (Spring AI)              |
| 5. Persistência | Salva o metadado do Documento (nome, hash, data) na tabela `documento`. | Repositório JPA (`DocumentoRepository`) |
| 6. Vetorização | Para cada Chunk: Converte o texto em um vetor numérico (embedding). | `EmbeddingModel` (ex. nomic-embed-text) |
| 7. Armazenamento | Persiste o texto original (content) e o vetor (embedding) na tabela `pdf_chunks`. | `PgVectorStore` (Spring AI/JDBC)        |
| **Saída** | **Banco de dados pronto para consultas RAG.** | **PostgreSQL (pgvector)**               |

### 2. Pipeline de Consulta (Busca e Resposta)

O objetivo é receber uma pergunta do usuário, buscar o contexto relevante no banco e usar um LLM para gerar uma resposta.

| Etapa | Ação | Componente Principal                |
| :--- | :--- |:------------------------------------|
| 1. Query | Usuário envia a pergunta via API REST. | `QueryController (@RestController)` |
| 2. Vetorização da Query | Converte a pergunta do usuário em um vetor numérico. | `EmbeddingModel` (Ollama, Mistral)  |
| 3. Recuperação | Executa uma busca de similaridade de cosseno (`<#>`) entre o vetor da pergunta e os vetores armazenados no banco. | `PgVectorStore (similaritySearch)`  |
| 4. Contexto | Pega os N (ex: 5) `chunks` mais similares e os junta para formar um bloco de contexto. | `QueryService (Streams/Collectors)` |
| 5. Augmentação | Cria um Prompt final com uma instrução para o LLM, injetando o contexto recuperado. | `ChatModel` (Spring AI)             |
| 6. Geração | Envia o Prompt contextualizado para o LLM e recebe a resposta final. | `ChatModel`                         |
| **Saída** | **Resposta concisa e fundamentada, baseada no conteúdo dos PDFs.** | **String (Resposta do LLM)**        |

---

## 🛠️ Stack TECH

* **Framework Principal:** Spring Boot 3.x
* **Orquestração AI:** Spring AI 1.1.x
* **Banco de Dados Vetorial:** PostgreSQL com extensão `pgvector`
* **Provedor de Modelos (LLM):** Ollama (`docker/docker-compose.yml`)
* **Modelo de Geração (Chat):** Mistral_7b e Gemini-2.0-flash-lite
* **Modelo de Vetorização (Embedding):** Nomic Embed Text (via Ollama)
* **Persistência de Metadados:** Spring Data JPA
* **Extração de PDF:** Apache PDFBox

---

## ⚠️ Você é um desenvolvedor? Saiba disso entes de tudo

Este projeto não é um "plug-and-play". Ele exige a configuração de serviços externos e o uso correto dos Perfis do Spring Boot.

### 1. Banco de Dados (Obrigatório)

A aplicação **exige** uma instância do PostgreSQL (local ou em nuvem) com a extensão `pgvector` habilitada.
* **Habilitação:** Você deve executar `CREATE EXTENSION IF NOT EXISTS vector;` no seu banco de dados.
* **Configuração:** As credenciais e URL do banco devem ser fornecidas no arquivo de configuração.

### 2. Provedor de LLM e Embeddings (Obrigatório)

A versão padrão utiliza o **Ollama** como provedor de modelos:

* **Ollama Server:** O servidor Ollama deve estar instalado e rodando (padrão em `http://localhost:11434`).
* **Download dos Modelos:** Você **deve** baixar os modelos antes de rodar a aplicação:
    * `ollama pull mistral` (Modelo de Chat)
    * `ollama pull nomic-embed-text` (Modelo de Embedding)
* **Ou crie um API do Gemini e utilize um dos modelos suportados pelo Spring AI 1.1.x**

### 3. Configuração Condicional de Perfis

A aplicação usa `application-mistral.yaml` e `application-gemini.yaml` por padrão (além da lógica `@ConditionalOnProperty` em `AiConfig`) para alternar entre provedores.

* **Obrigatoriedade:** A ativação do perfil é obrigatória para que os Beans corretos sejam inicializados. A aplicação possui um *validator* que impedirá a execução sem um perfil de modelo ativo.
* **Exemplo de Execução (Ollama/Mistral):**
    ```bash
    java -jar PdfSorgu.jar --spring.profiles.active=mistral
    ```
  ou
   ```bash
    java -jar PdfSorgu.jar --spring.profiles.active=gemini
    ```
  e até
    ```bash
    java -jar PdfSorgu.jar --spring.profiles.active=your_custom_model_name
    ```


---

## 🚀 Como Executar

1.  **Clone o repositório:**
    ```bash
    git clone [URL-DO-SEU-REPOSITORIO]
    cd PdfSorgu
    ```

2.  **Configure o Ambiente:**
    * Inicie o PostgreSQL com `pgvector`.
    * Inicie o servidor Ollama e baixe os modelos.
    * Verifique e ajuste as URLs de conexão no seu arquivo de configuração de perfil (`application-mistral.yaml`).

3.  **Compile o Projeto:**
    ```bash
    ./gradlew clean build
    ```

4.  **Execute com o Perfil Correto:**
    ```bash
    java -jar build/libs/PdfSorgu-0.0.1.jar --spring.profiles.active=model_name
    ```

5.  **Utilize a API:**
    * **POST /v1/pdf:** Faça o upload de um arquivo PDF (`multipart/form-data`), será retornado um pdf_id.
    * **GET /v1/query:** Envie uma pergunta para interagir com o RAG.
      * ex: /v1/query/`[pdf_id]`?q=`pergunta com base no pdf`