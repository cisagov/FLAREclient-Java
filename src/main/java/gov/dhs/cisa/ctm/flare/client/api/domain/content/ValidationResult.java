package gov.dhs.cisa.ctm.flare.client.api.domain.content;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;

/**
 * Stores the state of validation for a Content Wrapper, including the current status (pending, valid, invalid) and list of errors if present
 */
public class ValidationResult {

    @Field("status")
    private Status status;

    @Field("errors")
    private HashSet<String> errors;


    public ValidationResult() {
    }

    public ValidationResult(Status status, HashSet<String> errors) {
        this.status = status;
        this.errors = errors;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public HashSet<String> getErrors() {
        return errors;
    }

    public void setErrors(HashSet<String> errors) {
        this.errors = errors;
    }

    public enum Status {
        VALID,
        INVALID,
        PENDING,
        ERROR,
        VALIDATING
    }
}
