package com.twentyquestions.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameSession {
    private String sessionId;
    private int questionCount;
    private List<QuestionAnswer> history;
    private GameStatus status;
    private String guess;

    public GameSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.questionCount = 0;
        this.history = new ArrayList<>();
        this.status = GameStatus.STARTED;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public List<QuestionAnswer> getHistory() {
        return history;
    }

    public void setHistory(List<QuestionAnswer> history) {
        this.history = history;
    }

    public void addToHistory(String question, String answer) {
        this.history.add(new QuestionAnswer(question, answer));
        this.questionCount++;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }

    public int getRemainingQuestions() {
        return 20 - questionCount;
    }
}
