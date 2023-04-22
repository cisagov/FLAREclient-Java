package com.bcmc.xor.flare.client.api.domain.status;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.audit.AbstractAuditingEntity;
import com.bcmc.xor.flare.client.api.service.dto.StatusDTO;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
import com.google.gson.annotations.Expose;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import xor.bcmc.taxii2.JsonHandler;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

/**
 * An object for representing the current status of TAXII 2.1 publish/upload requests
 */
@Document(collection = Constants.RepositoryLabels.STATUS)
public class Status extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Expose
    private String id;

    @Field("status")
    @Expose
    private String status;

    @Field("request_timestamp")
    @Expose
    private ZonedDateTime requestTimestamp;

    @Field("total_count")
    @Expose
    private Integer totalCount;

    @Field("success_count")
    @Expose
    private Integer successCount;

    @Expose
    private HashSet<StatusDetails> successes;

    @Field("failure_count")
    @Expose
    private Integer failureCount;

    @Expose
    private HashSet<StatusDetails> failures;

    @Field("pending_count")
    @Expose
    private Integer pendingCount;

    @Expose
    private HashSet<StatusDetails> pendings;

    @URL
    @Size(max = 255)
    private String url;

    @Expose
    private Taxii21Association association;

    // A count of the number of times retrieving this status
    // has resulted in some sort of error.
    @Expose
    private int errorCount;

    public Status() { this.id = UUID.randomUUID().toString(); }

    public Status(StatusDTO statusDTO) {
        if (statusDTO.getId() == null) {
            id = UUID.randomUUID().toString();
        } else {
            id = statusDTO.getId();
        }
        status = statusDTO.getStatus();
        requestTimestamp = statusDTO.getRequestTimestamp();
        url = statusDTO.getUrl();

        totalCount = statusDTO.getTotalCount();

        failureCount = statusDTO.getFailureCount();
        failures = statusDTO.getFailures();
        pendingCount = statusDTO.getPendingCount();
        pendings = statusDTO.getPendings();
        successCount = statusDTO.getSuccessCount();
        successes = statusDTO.getSuccesses();

        errorCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(ZonedDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public HashSet<StatusDetails> getSuccesses() { return successes; }

    public void setSuccesses(HashSet<StatusDetails> successes) { this.successes = successes; }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public HashSet<StatusDetails> getFailures() { return failures; }

    public void setFailures(HashSet<StatusDetails> failures) { this.failures = failures; }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public HashSet<StatusDetails> getPendings() { return pendings; }

    public void setPendings(HashSet<StatusDetails> pendings) { this.pendings = pendings; }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Taxii21Association getAssociation() {
        return association;
    }

    public void setAssociation(Taxii21Association association) {
        this.association = association;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount){
        this.errorCount = errorCount;
    }

    public void incrementErrorCount() {
        this.errorCount+=1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return Objects.equals(id, status.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return JsonHandler.getInstance().toJson(this);
    }
}
