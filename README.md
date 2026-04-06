# Grant Chatbot Skeleton

This is a Maven multi-module Java skeleton for a persona-style chatbot with a Drools policy layer.

## Modules

- `chat-core`: shared domain models and interfaces
- `chat-rules`: Drools facts, DRL files, and rule engine adapter
- `chat-memory`: memory and persona-context service stub
- `chat-llm`: LLM adapter, including a stub implementation and a real OpenAI-backed implementation
- `chat-api`: Spring Boot app and REST controller

## Quick start

### Run with the built-in stub

```bash
mvn clean package
mvn -pl chat-api spring-boot:run
```

Then test with:

```bash
curl -X POST http://localhost:8080/api/messages/suggest \
  -H 'Content-Type: application/json' \
  -d '{
    "conversationId": "conv-123",
    "senderId": "user-42",
    "senderType": "FRIEND",
    "messageText": "Can you make it tomorrow?",
    "hourOfDay": 20
  }'
```

## Switch to the real OpenAI implementation

The skeleton now includes an `OpenAiLlmService` wired through the official OpenAI Java SDK. The SDK README shows the Maven dependency, the `OpenAIOkHttpClient.fromEnv()` pattern, and Spring Boot configuration options, and it documents structured outputs for both Chat Completions and the Responses API. citeturn297557view0turn499721view1

Set your API key and switch the provider:

```bash
export OPENAI_API_KEY="your_api_key_here"
export OPENAI_PROJECT_ID="your_project_id_if_you_use_one"
```

Update `chat-api/src/main/resources/application.yml`:

```yaml
app:
  llm:
    provider: openai
    openai:
      model: GPT_5_4
      max-completion-tokens: 450
```

Then run again:

```bash
mvn clean package
mvn -pl chat-api spring-boot:run
```

## What the OpenAI service does

- uses structured output so the model returns three reply candidates in a fixed schema
- builds prompts from inbound text, persona context, style examples, and Drools policy constraints
- returns `concise`, `natural`, and `bold` reply variants
- falls back to deterministic local candidates if the OpenAI call fails

## Current limitations

- the memory layer is still in-memory only
- the OpenAI implementation currently uses Chat Completions structured output because the Java SDK examples for typed schemas are especially explicit there, even though the SDK identifies the Responses API as the primary API surface. citeturn297557view0turn248774view0
- `resolveModel()` currently maps only `GPT_5_2` and `GPT_5_4`; extend that method if you want more selectable models
- there is no persistence, authentication, mobile bridge, or pgvector retrieval yet

## Next steps

1. Replace `InMemoryMemoryService` with Postgres + pgvector retrieval.
2. Add a feedback endpoint that stores your edits as new style examples.
3. Add auth, telemetry, and an iPhone/mobile bridge.
4. Expand the DRL policy set for sender types, risk controls, and approval rules.
5. Move model selection, temperature, and timeouts into richer typed configuration.
