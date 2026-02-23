package com.twentyquestions.model;

public class StartGameResponse {
    private String sessionId;
    private GameStatus status;

    private StartGameResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSessionId() {
        return sessionId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public static class Builder {
        private final StartGameResponse response = new StartGameResponse();

        public Builder sessionId(String sessionId) {
            response.sessionId = sessionId;
            return this;
        }

        public Builder status(GameStatus status) {
            response.status = status;
            return this;
        }

        public StartGameResponse build() {
            return response;
        }
    }
}
