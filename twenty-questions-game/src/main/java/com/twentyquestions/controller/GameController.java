package com.twentyquestions.controller;

import com.twentyquestions.model.AnswerResponse;
import com.twentyquestions.model.ReadyResponse;
import com.twentyquestions.model.StartGameResponse;
import com.twentyquestions.model.StatusResponse;
import com.twentyquestions.model.GameSession;
import com.twentyquestions.model.GameStatus;
import com.twentyquestions.service.AiQuestionResponse;
import com.twentyquestions.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<StartGameResponse> startGame() {
        GameSession session = gameService.startGame();

        StartGameResponse response = StartGameResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/ready")
    public ResponseEntity<ReadyResponse> playerReady(@PathVariable("sessionId") String sessionId) {
        GameSession session = gameService.getSession(sessionId);
        
        if (session == null) {
            log.warn("POST /api/game/{}/ready - Session not found", sessionId);
            return ResponseEntity.notFound().build();
        }

        // Validate flow: /ready can only be called after /start (when status is STARTED)
        if (session.getStatus() != GameStatus.STARTED) {
            log.warn("POST /api/game/{}/ready - Invalid state. Expected STARTED but was {}", sessionId, session.getStatus());
            return ResponseEntity.badRequest().build();
        }

        AiQuestionResponse aiResponse = gameService.getNextQuestion(session);
        
        if (aiResponse != null && aiResponse.getQuestion() != null) {
            gameService.addQuestionToHistory(session, aiResponse.getQuestion(), aiResponse.getContext());
        }

        ReadyResponse response = ReadyResponse.builder()
                .sessionId(session.getSessionId())
                .question(aiResponse != null ? aiResponse.getQuestion() : null)
                .context(aiResponse != null ? aiResponse.getContext() : null)
                .questionNumber(session.getQuestionCount())
                .remainingQuestions(session.getRemainingQuestions())
                .status(session.getStatus())
                .history(session.getHistory())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerResponse> answerQuestion(
            @PathVariable("sessionId") String sessionId,
            @RequestBody Map<String, String> request) {
        String answer = request.get("answer");

        GameSession session = gameService.getSession(sessionId);
        
        if (session == null) {
            log.warn("POST /api/game/{}/answer - Session not found", sessionId);
            return ResponseEntity.notFound().build();
        }
        
        // Validate flow: /answer can only be called after /ready (when status is IN_PROGRESS or game-ending states)
        if (session.getStatus() == GameStatus.STARTED) {
            log.warn("POST /api/game/{}/answer - Invalid state. Cannot answer before calling /ready", sessionId);
            return ResponseEntity.badRequest().build();
        }
        
        session = gameService.processAnswer(sessionId, answer);
        
        AiQuestionResponse aiResponse = null;
        if (session.getStatus() == GameStatus.IN_PROGRESS) {
            aiResponse = gameService.getNextQuestion(session);
            
            if (aiResponse != null && aiResponse.getQuestion() != null && session.getStatus() != GameStatus.GUESSED) {
                gameService.addQuestionToHistory(session, aiResponse.getQuestion(), aiResponse.getContext());
            }
        }

        AnswerResponse.Builder responseBuilder = AnswerResponse.builder()
                .sessionId(session.getSessionId())
                .question(aiResponse != null ? aiResponse.getQuestion() : null)
                .context(aiResponse != null ? aiResponse.getContext() : null)
                .questionNumber(session.getQuestionCount())
                .remainingQuestions(session.getRemainingQuestions())
                .status(session.getStatus())
                .guess(session.getGuess())
                .history(session.getHistory());

        if (session.getStatus() == GameStatus.GUESSED) {
            responseBuilder.message("Your person in mind is " + session.getGuess());
        } else if (session.getStatus() == GameStatus.PLAYER_WON) {
            responseBuilder.message("You win!");
        }

        AnswerResponse response = responseBuilder.build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<StatusResponse> getStatus(@PathVariable("sessionId") String sessionId) {
        GameSession session = gameService.getSession(sessionId);
        
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        StatusResponse response = StatusResponse.builder()
                .sessionId(session.getSessionId())
                .questionNumber(session.getQuestionCount())
                .remainingQuestions(session.getRemainingQuestions())
                .status(session.getStatus())
                .guess(session.getGuess())
                .build();

        return ResponseEntity.ok(response);
    }
}
