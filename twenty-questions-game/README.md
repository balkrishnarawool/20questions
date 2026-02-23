# 20 Questions Game SpringBoot App

## Setup

1. **Install Ollama and download Llama model:**

    ```bash
   # Install Ollama from https://ollama.ai/
   
   # Pull the Llama model
   ollama pull llama3.2
   ```

    **OR**

    **Run Ollama in a docker container:**
    1. Download Ollama docker image from https://hub.docker.com/r/ollama/ollama
    2. Run it and then in the terminal of this container instance do `ollama pull llama3.2`

    **OR**

    *If you don't want to run Ollama then you can also use mock it. See [mock-ollama-server](../mock-ollama-server/README.md) for detailed documentation.*


2. **Build and verify the application:**
   ```bash
   mvn verify
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Open your browser:**
   Navigate to `http://localhost:8081`

### Flow Rules
1. **Create Game** → `POST /api/game` to create a session
2. **Ready** → `POST /api/game/{sessionId}/ready` (only after creating game)
3. **Answer** → `POST /api/game/{sessionId}/answer` repeatedly with YES/NO (only after ready)
4. **Status** → `GET /api/game/{sessionId}/status` anytime to check game state

### Game Status Values
- `STARTED` - Session created, waiting for first question
- `IN_PROGRESS` - Game active, questions being asked
- `GUESSED` - AI made a guess
- `PLAYER_WON` - Player won (20 questions exhausted)

## API Endpoints

The API follows a strict flow with RESTful resource-based URLs:

### 1. `POST /api/game`
Create a new game session. Can be called anytime.

**Request:** None

**Response:** `StartGameResponse`
```json
{
  "sessionId": "uuid",
  "status": "STARTED"
}
```

### 2. `POST /api/game/{sessionId}/ready`
Get the first question from the AI. Can only be called after creating a game (when status is `STARTED`).

**Path Parameter:** `sessionId` - The game session ID

**Request:** None

**Response:** `ReadyResponse`
```json
{
  "sessionId": "uuid",
  "question": "Is the person alive?",
  "context": "Let me start with a basic question",
  "questionNumber": 1,
  "remainingQuestions": 19,
  "status": "IN_PROGRESS",
  "history": [...]
}
```

### 3. `POST /api/game/{sessionId}/answer`
Submit an answer and get the next question. Can only be called after `/ready`. Can be called multiple times.

**Path Parameter:** `sessionId` - The game session ID

**Request:**
```json
{
  "answer": "YES" // or "NO"
}
```

**Response:** `AnswerResponse`
```json
{
  "sessionId": "uuid",
  "question": "Is the person in entertainment?",
  "context": "Narrowing down the field",
  "questionNumber": 2,
  "remainingQuestions": 18,
  "status": "IN_PROGRESS",
  "guess": null,
  "message": null,
  "history": [...]
}
```

When AI guesses:
```json
{
  "sessionId": "uuid",
  "status": "GUESSED",
  "guess": "Albert Einstein",
  "message": "Your person in mind is Albert Einstein",
  ...
}
```

### 4. `GET /api/game/{sessionId}/status`
Get current game status. Can be called anytime.

**Path Parameter:** `sessionId` - The game session ID

**Response:** `StatusResponse`
```json
{
  "sessionId": "uuid",
  "questionNumber": 5,
  "remainingQuestions": 15,
  "status": "IN_PROGRESS",
  "guess": null
}
```


