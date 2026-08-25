package com.example.news.exception;

public class NewsUnavailableException extends RuntimeException {

    public NewsUnavailableException(String message) {
        super(message);
    }
}
