package com.bcmc.xor.flare.client.api.domain.content;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import xor.bcmc.taxii2.JsonHandler;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * STIX 2.X implementation of a Content Wrapper
 */
@Document(collection = Constants.RepositoryLabels.CONTENT)
@JsonTypeName("STIX2")
public class Stix2ContentWrapper extends AbstractContentWrapper {

    private static final Logger log = LoggerFactory.getLogger(Stix2ContentWrapper.class);

    @Field("content_object")
    private org.bson.Document contentObject;

    public Stix2ContentWrapper() {
        this.setId(UUID.randomUUID().toString());
    }

    public Stix2ContentWrapper(String contentString, TaxiiAssociation association) {
        org.bson.Document dbObject = org.bson.Document.parse(contentString);
        this.contentObject = dbObject;
        this.setId(UUID.randomUUID().toString());
        if (dbObject.get("id") != null) {
            this.setContentId(dbObject.get("id").toString());
        } else {
            throw new IllegalArgumentException("Parsed JSON object from content string did not have an 'id' field");
        }
        if (dbObject.get("modified") != null) {
            this.setContentTimestamp(ZonedDateTime.parse(dbObject.get("modified").toString()));
        } else {
            if (dbObject.get("created") != null) {
                this.setContentTimestamp(ZonedDateTime.parse(dbObject.get("created").toString()));
            } else {
                log.error("Couldn't parse 'modified' or 'created' timestamp from DBObject created from content string (id: '{}')", this.getContentId());
            }
        }
        this.setAssociation(association);
        this.setValidationResult(null);
    }

    @Override
    public ValidationResult validate() {
        return null;
    }

    public org.bson.Document getContentObject() {
        return contentObject;
    }

    public void setContentObject(org.bson.Document contentObject) {
        this.contentObject = contentObject;
    }
}
