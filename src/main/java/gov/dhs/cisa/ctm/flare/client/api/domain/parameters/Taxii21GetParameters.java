package gov.dhs.cisa.ctm.flare.client.api.domain.parameters;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * TAXII 2.1 Fetch parameters
 */
@JsonTypeName("TAXII21")
public class Taxii21GetParameters extends ApiParameters implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Taxii21GetParameters.class);

    @Field("added_after")
    private ZonedDateTime addedAfter;

    @Field("api_root_ref")
    private String apiRootRef;

    @Field("queryString")
    private String queryString;

    public Taxii21GetParameters() {

    }

    public ZonedDateTime getAddedAfter() {
        if (!addedAfter.getZone().equals(ZoneId.of("Z"))) {
            return addedAfter.withZoneSameInstant(ZoneId.of("Z"));
        }
        return addedAfter;
    }

    public void setAddedAfter(ZonedDateTime addedAfter) {
        if (!addedAfter.getZone().equals(ZoneId.of("Z"))) {
            this.addedAfter = addedAfter.withZoneSameInstant(ZoneId.of("Z"));
            return;
        }
        this.addedAfter = addedAfter;
    }

    public String getApiRootRef() {
        return apiRootRef;
    }

    public void setApiRootRef(String apiRootRef) {
        this.apiRootRef = apiRootRef;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
}
