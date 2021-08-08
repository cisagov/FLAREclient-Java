package gov.dhs.cisa.ctm.flare.client.api.service.dto;

import org.hibernate.validator.constraints.URL;
import org.springframework.data.mongodb.core.mapping.Field;

import gov.dhs.cisa.ctm.flare.client.api.domain.status.Status;
import gov.dhs.cisa.ctm.flare.client.api.domain.status.StatusDetails;

import javax.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.HashSet;

/**
 *  * A DTO representing a status
 */

@SuppressWarnings("unused")
public class StatusDTO {

    private static final long serialVersionUID = 1L;

    private String id;

    @Field("status")
    private String status;

    @Field("request_timestamp")
    private ZonedDateTime requestTimestamp;

    @Field("total_count")
    private Integer totalCount;

    @Field("success_count")
    private Integer successCount;

    private HashSet<StatusDetails> successes;

    @Field("failure_count")
    private Integer failureCount;

    private HashSet<StatusDetails> failures;

    @Field("pending_count")
    private Integer pendingCount;

    private HashSet<StatusDetails> pendings;

    @URL
    @Size(max = 255)
    private String url;

    @Field
    private Integer errorCount;

    public StatusDTO(Status status) {
        this.id = status.getId();
        this.status = status.getStatus();
        this.requestTimestamp = status.getRequestTimestamp();
        this.totalCount = status.getTotalCount();
        this.successCount = status.getSuccessCount();
        this.successes = status.getSuccesses();
        this.failureCount = status.getFailureCount();
        this.failures = status.getFailures();
        this.pendingCount = status.getPendingCount();
        this.pendings = status.getPendings();
        this.url = status.getUrl();
        this.errorCount = status.getErrorCount();
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public ZonedDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public HashSet<StatusDetails> getSuccesses() {
        return successes;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public HashSet<StatusDetails> getFailures() {
        return failures;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public HashSet<StatusDetails> getPendings() {
        return pendings;
    }

    public Integer getErrorCount() {
        return errorCount;
    }
}
