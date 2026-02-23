package com.twentyquestions.service;

public class AiQuestionResponse {
    private String context;
    private String question;
    private String guess;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }

    public boolean isGuess() {
        return guess != null && !guess.isEmpty();
    }
}
