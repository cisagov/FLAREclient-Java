package gov.dhs.cisa.ctm.flare.client.error;

import java.util.Objects;

abstract class APISubError {

}

class APIValidationError extends APISubError {
    private String object;
    private String field;
    private Object rejectedValue;
    private String message;

    APIValidationError(String object, String message) {
        this.object = object;
        this.message = message;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof APIValidationError)) return false;
        APIValidationError that = (APIValidationError) o;
        return Objects.equals(getObject(), that.getObject()) &&
                Objects.equals(field, that.field) &&
                Objects.equals(rejectedValue, that.rejectedValue) &&
                message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObject(), field, rejectedValue, message);
    }
}
