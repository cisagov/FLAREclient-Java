package gov.dhs.cisa.ctm.flare.client.api.domain.server;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.api.domain.audit.AbstractAuditingEntity;
import gov.dhs.cisa.ctm.flare.client.api.domain.collection.TaxiiCollection;
import gov.dhs.cisa.ctm.flare.client.api.service.dto.ServerDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;


@Document(collection = Constants.RepositoryLabels.SERVER)
public abstract class TaxiiServer extends AbstractAuditingEntity implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(TaxiiServer.class);

    private static final long serialVersionUID = 1L;

    @Id
    @Indexed
    private String id;

    @NotNull
    @Size(min = 1, max = 50)
    @Indexed(unique=true)
    @Field("label")
    private String label;

    @NotNull
    @Field("url")
    private URI url;

    @NotNull
    @Field("version")
    private Constants.TaxiiVersion version;

    @Transient
    String title;

    @Field("server_description")
    private String serverDescription;

    /**
     * A boolean indicating that the Server has been configured successfully and has received both discovery and collection information
     */
    @Field("is_available")
    private boolean isAvailable = false;

    /**
     * Indicates whether or not this server has successfully updated discovery information
     */
    @Field("has_received_server_information")
    private boolean hasReceivedServerInformation = false;

    /**
     * Indicates whether or not this server has successfully updated collection information
     */
    @Field("has_received_collection_information")
    private boolean hasReceivedCollectionInformation = false;

    @Field("last_received_server_information")
    private Instant lastReceivedServerInformation = null;

    @Field("last_received_collection_information")
    private Instant lastReceivedCollectionInformation = null;

    @Field("last_updated")
    private Instant lastUpdated = null;

    /**
     * Indicates whether or not the Server will require HTTP Basic Authentication credentials
     */
    @Field("requires_basic_auth")
    private boolean requiresBasicAuth = false;

    public TaxiiServer() {
    }

    public TaxiiServer(ServerDTO serverDTO) {
        setLabel(serverDTO.getLabel());
        setUrl(URI.create(serverDTO.getUrl()));
        setServerDescription(serverDTO.getServerDescription());
        setRequiresBasicAuth(serverDTO.getRequiresBasicAuth());
    }

    public abstract HashSet<? extends TaxiiCollection> getCollections();

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public URI getUrl() {
        return url;
    }

    public abstract URI getServerInformationUrl();

    public void setUrl(URI url) {
        this.url = url;
    }

    public Constants.TaxiiVersion getVersion() {
        return version;
    }

    public void setVersion(Constants.TaxiiVersion version) {
        this.version = version;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServerDescription() {
        return serverDescription;
    }

    public void setServerDescription(String serverDescription) {
        this.serverDescription = serverDescription;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean hasReceivedServerInformation() {
        return hasReceivedServerInformation;
    }

    public void setHasReceivedServerInformation(boolean hasReceivedServerInformation) {
        this.hasReceivedServerInformation = hasReceivedServerInformation;
    }

    public boolean hasReceivedCollectionInformation() {
        return hasReceivedCollectionInformation;
    }

    public void setHasReceivedCollectionInformation(boolean hasReceivedCollectionInformation) {
        this.hasReceivedCollectionInformation = hasReceivedCollectionInformation;
    }

    public Instant getLastReceivedServerInformation() {
        return lastReceivedServerInformation;
    }

    public void setLastReceivedServerInformation(Instant lastReceivedServerInformation) {
        this.lastReceivedServerInformation = lastReceivedServerInformation;
    }

    public Instant getLastReceivedCollectionInformation() {
        return lastReceivedCollectionInformation;
    }

    public void setLastReceivedCollectionInformation(Instant lastReceivedCollectionInformation) {
        this.lastReceivedCollectionInformation = lastReceivedCollectionInformation;
    }

    public boolean getRequiresBasicAuth() {
        return requiresBasicAuth;
    }

    public void setRequiresBasicAuth(boolean requiresBasicAuth) {
        this.requiresBasicAuth = requiresBasicAuth;
    }

    public String defaultApiRoot;

    public String getDefaultApiRoot() {
        return defaultApiRoot;
    }

    public void setDefaultApiRoot(String defaultApiRoot) {
        this.defaultApiRoot = defaultApiRoot;
    }

    public HashSet<String> apiRoots;

    public HashSet<String> getApiRoots() {
        return apiRoots;
    }

    public void setApiRoots(HashSet<String> apiRoots) {
        this.apiRoots = apiRoots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxiiServer that = (TaxiiServer) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, label);
    }

    @Override
    public String toString() {
        return "Server{" +
            "id='" + id + '\'' +
            ", label='" + label + '\'' +
            ", url=" + url +
            ", version=" + version +
            ", title=" + title +
            ", serverDescription=" + serverDescription +
            ", isAvailable=" + isAvailable +
            ", hasReceivedServerInformation=" + hasReceivedServerInformation +
            ", hasReceivedCollectionInformation=" + hasReceivedCollectionInformation +
            ", lastReceivedServerInformation=" + lastReceivedServerInformation +
            ", lastReceivedCollectionInformation=" + lastReceivedCollectionInformation +
            ", lastUpdated=" + lastUpdated +
            ", requiresBasicAuth=" + requiresBasicAuth +
            '}';
    }
}
