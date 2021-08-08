package gov.dhs.cisa.ctm.flare.client.api.domain.parameters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.net.URI;

/**
 * An abstract class representing basic fetch parameters
 *
 * Fetch parameters are used when downloading content from TAXII servers, and can be persisted in the datastore to create 'recurring' fetches for content.
 *
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Taxii11PollParameters.class, name = "TAXII11"),
    @JsonSubTypes.Type(value = Taxii21GetParameters.class, name = "TAXII21")
})
public class ApiParameters implements Serializable {

    @Id
    private String id;

    /**
     * The server label denoting which server is associated with these fetch parameters
     */
    @Field("server_label")
    private String serverLabel;

    private TaxiiAssociation association;

    /**
     * The time interval in between each recurring fetch, when 'repeat' is true
     */
    @Field("window")
    private int window;

    /**
     * Indicates whether or not these fetch parameters should be persisted into a recurring fetch
     */
    @Field("repeat")
    private boolean repeat;

    @Field("fetch_url")
    private URI fetchUrl;

    @SuppressWarnings("unused")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TaxiiAssociation getAssociation() {
        return association;
    }

    public void setAssociation(TaxiiAssociation association) {
        this.association = association;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public String getServerLabel() {
        return serverLabel;
    }

    public void setServerLabel(String serverLabel) {
        this.serverLabel = serverLabel;
    }

    public boolean getRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public URI getFetchUrl() {
        return fetchUrl;
    }

    public void setFetchUrl(URI fetchUrl) {
        this.fetchUrl = fetchUrl;
    }

}

