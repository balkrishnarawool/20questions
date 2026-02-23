package com.twentyquestions.service;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.twentyquestions.exception.LlmResponseException;
import com.twentyquestions.model.GameSession;
import com.twentyquestions.model.GameStatus;
import com.twentyquestions.model.QuestionAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    // Prompt Templates
    private static final String SYSTEM_PROMPT = """
            You are playing 20 Questions. Your goal is to guess which famous person (real or fictional)
            the player is thinking of by asking yes/no questions. Be strategic and ask questions that
            help narrow down the possibilities quickly.

            Rules:
            - Ask only ONE question at a time
            - Questions must be answerable with YES or NO
            - Be concise and clear
            - Track the remaining questions wisely

            IMPORTANT: You MUST respond in JSON format with this structure:
            {
              "context": "Optional brief context about why you're asking this question (1 sentence max)",
              "question": "Your yes/no question here. It has to be a single sentence."
            }

            If you are confident about the answer, respond with:
            {
              "context": "Based on the answers",
              "guess": "Person Name"
            }

            Always return valid JSON. Do not include any text outside the JSON structure.
            """;

    private static final String FIRST_QUESTION_PROMPT = 
            "Start by asking the first question to guess the famous person. Respond in JSON format.";

    private static final String HISTORY_TEMPLATE = """
            Previous questions and answers:
            %s
            Questions asked: %d
            Questions remaining: %d

            Based on these answers, ask the next strategic question or make a guess if you're confident. Respond in JSON format.
            """;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    public GameService(ChatClient.Builder chatClientBuilder) {
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(100)
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public GameSession startGame() {
        GameSession session = new GameSession();
        sessions.put(session.getSessionId(), session);
        return session;
    }

    public AiQuestionResponse getNextQuestion(GameSession session) {
        if (session.getQuestionCount() >= 20) {
            session.setStatus(GameStatus.PLAYER_WON);
            return null;
        }

        final var userPrompt = formUserPrompt(session);

        log.info("getNextQuestion() - Calling AI with prompt: {}", userPrompt.substring(0, Math.min(5000, userPrompt.length())) + "...");
        long aiCallStart = System.currentTimeMillis();
        // Call retryable AI method
        AiQuestionResponse aiResponse = null;
        try {
            aiResponse = callAiWithRetry(userPrompt, session.getSessionId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        long aiCallEnd = System.currentTimeMillis();
        log.info("getNextQuestion() - AI call completed in {} ms", (aiCallEnd - aiCallStart));
        log.info("getNextQuestion() - Response: context='{}', question='{}', guess='{}'", 
                aiResponse.getContext(), aiResponse.getQuestion(), aiResponse.getGuess());

        // Handle guess
        if (aiResponse.isGuess()) {
            String guess = aiResponse.getGuess();
            session.setGuess(guess);
            session.setStatus(GameStatus.GUESSED);
            log.info("getNextQuestion() - AI guessed: {}", guess);
        } else {
            // Set status to IN_PROGRESS when asking questions
            session.setStatus(GameStatus.IN_PROGRESS);
        }
        
        return aiResponse;
    }

    private static String formUserPrompt(GameSession session) {
        String userPrompt;
        if (session.getHistory().isEmpty()) {
            userPrompt = FIRST_QUESTION_PROMPT;
        } else {
            // Build conversation history
            StringBuilder historyBuilder = new StringBuilder();
            for (QuestionAnswer qa : session.getHistory()) {
                if (qa.getAnswer() != null) {
                    historyBuilder.append("Q: ").append(qa.getQuestion()).append("\n");
                    historyBuilder.append("A: ").append(qa.getAnswer()).append("\n");
                }
            }

            userPrompt = String.format(HISTORY_TEMPLATE,
                    historyBuilder.toString(),
                    session.getQuestionCount(),
                    session.getRemainingQuestions());
        }
        return userPrompt;
    }

    @Retryable(
        retryFor = {JsonEOFException.class, JsonParseException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
    private AiQuestionResponse callAiWithRetry(String userPrompt, String sessionId) throws com.fasterxml.jackson.core.JsonProcessingException {
        log.info("callAiWithRetry() - Attempting AI call for session {}", sessionId);

        // When LLM does not give valid JSON, quite often it is because it omits the last '}'.
        // So, here's a quick hack to add it if it is not present.
        // TODO: Find a better alternative
        String aiRawResponse = chatClient.prompt()
                .user(userPrompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
        // Add a trailing '}' if missing before deserialization
        String fixedResponse = aiRawResponse == null ? "" : aiRawResponse.trim();
        if (!fixedResponse.endsWith("}")) {
            fixedResponse = fixedResponse + "}";
            log.warn("AI response did not end with '}}'. Appended one for parsing safety.");
        }
        AiQuestionResponse response;
        try {
            response = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                    .readValue(fixedResponse, AiQuestionResponse.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse AI response after fixing trailing '}}'", e);
            throw e;
        }

        log.info("callAiWithRetry() - Successfully parsed AI response");
        return response;
    }

    @Recover
    private AiQuestionResponse recoverFromJsonError(Exception e, String userPrompt, String sessionId) {
        log.error("recoverFromJsonError() - All retry attempts failed for session {}", sessionId, e);
        throw new LlmResponseException("LLM is not responding properly. Please try again.", e);
    }

    public GameSession processAnswer(String sessionId, String answer) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        if (session.getHistory().isEmpty()) {
            throw new IllegalStateException("No question asked yet");
        }

        QuestionAnswer lastQA = session.getHistory().get(session.getHistory().size() - 1);
        lastQA.setAnswer(answer);

        return session;
    }

    public void addQuestionToHistory(GameSession session, String question, String context) {
        QuestionAnswer qa = new QuestionAnswer(question, context, null);
        session.getHistory().add(qa);
        session.setQuestionCount(session.getQuestionCount() + 1);
    }

    public GameSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
}
