package com.bcmc.xor.flare.client.error;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;

public class APIError {

    private HttpStatus httpStatus;
    private String message;
    private String debugMessage;
    private List<APISubError> subErrorList;

    private APIError() {}

    public APIError(HttpStatus httpStatus) {
        this();
        this.httpStatus = httpStatus;
    }

    public APIError(HttpStatus httpStatus, Throwable ex) {
        this();
        this.httpStatus = httpStatus;
        this.message = "Unexpected error";
        this.debugMessage = ex.getLocalizedMessage();
    }


    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public void setDebugMessage(String debugMessage) {
        this.debugMessage = debugMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof APIError)) return false;
        APIError apiError = (APIError) o;
        return getHttpStatus() == apiError.getHttpStatus() &&
                Objects.equals(getMessage(), apiError.getMessage()) &&
                Objects.equals(getDebugMessage(), apiError.getDebugMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHttpStatus(), getMessage(), getDebugMessage());
    }
}
