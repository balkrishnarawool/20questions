package com.twentyquestions.model;

public class StatusResponse {
    private String sessionId;
    private int questionNumber;
    private int remainingQuestions;
    private GameStatus status;
    private String guess;

    private StatusResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSessionId() {
        return sessionId;
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

    public String getGuess() {
        return guess;
    }

    public static class Builder {
        private final StatusResponse response = new StatusResponse();

        public Builder sessionId(String sessionId) {
            response.sessionId = sessionId;
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

        public Builder guess(String guess) {
            response.guess = guess;
            return this;
        }

        public StatusResponse build() {
            return response;
        }
    }
}
