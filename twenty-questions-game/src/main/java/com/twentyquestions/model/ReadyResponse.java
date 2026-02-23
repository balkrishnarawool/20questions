package com.twentyquestions.model;

import java.util.List;

public class ReadyResponse {
    private String sessionId;
    private String question;
    private String context;
    private int questionNumber;
    private int remainingQuestions;
    private GameStatus status;
    private List<QuestionAnswer> history;

    private ReadyResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getQuestion() {
        return question;
    }

    public String getContext() {
        return context;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public int getRemainingQuestions() {
        return remainingQuestions;
    }

    public GameStatus getStatus() {
        return status;
    }

    public List<QuestionAnswer> getHistory() {
        return history;
    }

    public static class Builder {
        private final ReadyResponse response = new ReadyResponse();

        public Builder sessionId(String sessionId) {
            response.sessionId = sessionId;
            return this;
        }

        public Builder question(String question) {
            response.question = question;
            return this;
        }

        public Builder context(String context) {
            response.context = context;
            return this;
        }

        public Builder questionNumber(int questionNumber) {
            response.questionNumber = questionNumber;
            return this;
        }

        public Builder remainingQuestions(int remainingQuestions) {
            response.remainingQuestions = remainingQuestions;
            return this;
        }

        public Builder status(GameStatus status) {
            response.status = status;
            return this;
        }

        public Builder history(List<QuestionAnswer> history) {
            response.history = history;
            return this;
        }

        public ReadyResponse build() {
            return response;
        }
    }
}
