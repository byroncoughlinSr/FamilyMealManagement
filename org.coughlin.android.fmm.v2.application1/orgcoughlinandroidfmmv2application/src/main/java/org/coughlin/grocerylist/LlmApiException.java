package org.coughlin.grocerylist;

public class LlmApiException extends Exception {
    private final int statusCode;

    public LlmApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }
}
