# Mock Ollama Server

A Mock Ollama server that mimics Ollama's API for testing of the 20 Questions game.

## Start Mock Server

```bash
cd mock-ollama-server
mvn spring-boot:run
```

## API Endpoints

### POST /api/chat
Ollama-compatible chat endpoint. Returns mock JSON responses.

**Request:**
```json
{
  "model": "llama3.2",
  "messages": [
    {"role": "user", "content": "Start the game..."}
  ]
}
```

**Response:**
```json
{
  "model": "llama3.2",
  "created_at": "2024-01-01T00:00:00Z",
  "message": {
    "role": "assistant",
    "content": "{\"context\":\"Let me start\",\"question\":\"Is the person alive?\"}"
  },
  "done": true
}
```

### GET /api/tags
Returns mock model list (for health checks).

### GET /api/version
Returns mock version info.

### GET /
Health check endpoint.

## Mock Responses

### Questions (8 variations, random):
1. "Is the person alive?"
2. "Is the person in entertainment?"
3. "Is the person American?"
4. "Is the person an actor or actress?"
5. "Did this person become famous after 2000?"
6. "Is the person known for comedy?"
7. "Has the person won an Oscar?"
8. "Is the person known for action movies?"

### Guesses (4 variations, random):
- Albert Einstein
- Tom Hanks
- Oprah Winfrey
- Leonardo DiCaprio


