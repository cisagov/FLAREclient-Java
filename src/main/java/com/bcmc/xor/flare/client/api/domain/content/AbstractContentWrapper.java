package com.bcmc.xor.flare.client.api.domain.content;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * An abstract class for wrapping STIX content
 */
@CompoundIndexes({
    @CompoundIndex(name = "_content_id_and_timestamp", def = "{ 'content_id' : 1, 'content_timestamp' : 1}", unique = true)
})
@Document(collection = Constants.RepositoryLabels.CONTENT)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "stixVersion",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Stix1ContentWrapper.class, name = "STIX1"),
    @JsonSubTypes.Type(value = Stix2ContentWrapper.class, name = "STIX2")
})
public abstract class AbstractContentWrapper {

    @Id
    private String id;

    /**
     * The content ID taken from either the STIX 1.X 'id' attribute, or the STIX 2.X 'id' property
     */
    @Field("content_id")
    private String contentId;

    @Field("association")
    @Indexed
    private TaxiiAssociation association;

    /**
     * The content timestamp taken from either the STIX 1.X 'timestamp' attribute, or the STIX 2.X 'modified' property
     */
    @Field("content_timestamp")
    @Indexed
    private ZonedDateTime contentTimestamp;

    @Field("validation_result")
    @Indexed
    private ValidationResult validationResult;

    @Field("last_retrieved")
    @Indexed
    private Instant lastRetrieved;

    /**
     * A generic validate method that will result in ValidationResult to be applied to the ContentWrapper
     * @return the ValidationResult
     */
    public abstract ValidationResult validate();

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public void setValidationStatus(ValidationResult.Status validationStatus) {
        this.validationResult.setStatus(validationStatus);
    }

    public ZonedDateTime getContentTimestamp() {
        return contentTimestamp;
    }

    void setContentTimestamp(ZonedDateTime contentTimestamp) {
        this.contentTimestamp = contentTimestamp;
    }

    public Instant getLastRetrieved() {
        return lastRetrieved;
    }

    public void setLastRetrieved(Instant lastRetrieved) {
        this.lastRetrieved = lastRetrieved;
    }

    public String getContentId() {
        return contentId;
    }

    void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public TaxiiAssociation getAssociation() {
        return association;
    }

    void setAssociation(TaxiiAssociation association) {
        this.association = association;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractContentWrapper that = (AbstractContentWrapper) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
