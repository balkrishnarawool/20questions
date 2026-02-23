package com.mock.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
public class MockOllamaController {

    private static final Logger log = LoggerFactory.getLogger(MockOllamaController.class);

    private int questionCount = 0;
    private final Random random = new Random();

    private static final List<Map<String, String>> MOCK_QUESTIONS = Arrays.asList(
            Map.of("context", "Let me start with a basic question", "question", "Is the person alive?"),
            Map.of("context", "Narrowing down the field", "question", "Is the person in entertainment?"),
            Map.of("context", "Getting more specific", "question", "Is the person American?"),
            Map.of("context", "Focusing on their profession", "question", "Is the person an actor or actress?"),
            Map.of("context", "Checking the time period", "question", "Did this person become famous after 2000?"),
            Map.of("context", "Looking at their work", "question", "Is the person known for comedy?"),
            Map.of("context", "Considering their awards", "question", "Has the person won an Oscar?"),
            Map.of("context", "Thinking about their roles", "question", "Is the person known for action movies?")
    );

    private static final List<Map<String, String>> MOCK_GUESSES = Arrays.asList(
            Map.of("context", "Based on the answers", "guess", "Albert Einstein"),
            Map.of("context", "I'm confident it's", "guess", "Tom Hanks"),
            Map.of("context", "My guess is", "guess", "Oprah Winfrey"),
            Map.of("context", "I believe it's", "guess", "Leonardo DiCaprio")
    );

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        List<?> messages = (List<?>) request.get("messages");
        if (messages != null && !messages.isEmpty()) {
            Map<?, ?> lastMessage = (Map<?, ?>) messages.get(messages.size() - 1);
            String content = lastMessage.get("content").toString();
            log.info("Message received: {}", content.substring(0, Math.min(100, content.length())));
        }

        questionCount++;

        Map<String, String> responseData;
        
        // After 5 questions, make a guess (30% chance, or always on 6th question)
        if (questionCount >= 6 || (questionCount >= 3 && random.nextDouble() < 0.3)) {
            responseData = MOCK_GUESSES.get(random.nextInt(MOCK_GUESSES.size()));
            log.info("Responding with GUESS: {}", responseData.get("guess"));
            questionCount = 0; // Reset for next game
        } else {
            responseData = MOCK_QUESTIONS.get(random.nextInt(MOCK_QUESTIONS.size()));
            log.info("Responding with QUESTION: {}", responseData.get("question"));
        }

        // Format JSON response to be parseable by Spring AI
        String jsonContent = formatAsJson(responseData);

        Map<String, Object> response = new HashMap<>();
        response.put("model", "llama3.2");
        response.put("created_at", Instant.now().toString());
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "assistant");
        message.put("content", jsonContent);
        response.put("message", message);
        
        response.put("done", true);

        return response;
    }

    private String formatAsJson(Map<String, String> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    @GetMapping("/tags")
    public Map<String, Object> tags() {
        Map<String, Object> model = new HashMap<>();
        model.put("name", "llama3.2");
        model.put("modified_at", Instant.now().toString());
        model.put("size", 1000000);

        Map<String, Object> response = new HashMap<>();
        response.put("models", Collections.singletonList(model));
        return response;
    }

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of("version", "0.0.0-mock");
    }

    @GetMapping("/")
    public String health() {
        return "Mock Ollama Server Running!";
    }
}
