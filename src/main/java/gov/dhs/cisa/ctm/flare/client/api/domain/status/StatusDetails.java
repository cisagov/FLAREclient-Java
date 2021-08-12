package gov.dhs.cisa.ctm.flare.client.api.domain.status;

import com.google.gson.annotations.Expose;

import gov.dhs.cisa.ctm.flare.client.api.domain.audit.AbstractAuditingEntity;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A Status failure indicating specific failure messages for given object ID's
 */

public class StatusDetails extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Expose
    private String id;

    @Expose
    private String message;

    @Expose
    private ZonedDateTime version;

    public StatusDetails() {

    }

    @SuppressWarnings("unused")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getVersion() {
        return version;
    }

    public void setVersion(ZonedDateTime version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusDetails that = (StatusDetails) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(message, that.message) &&
            Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, version);
    }

    @Override
    public String toString() {
        return "StatusDetails{" +
            "id='" + id + '\'' +
            ", message='" + message + '\'' +
            ", version=" + version +
            '}';
    }
}
