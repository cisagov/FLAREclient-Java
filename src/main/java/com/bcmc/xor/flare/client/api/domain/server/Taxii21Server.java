package com.bcmc.xor.flare.client.api.domain.server;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii21Collection;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import xor.bcmc.taxii2.resources.Discovery;

import javax.validation.constraints.Size;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;


@Document(collection = Constants.RepositoryLabels.SERVER)
public class Taxii21Server extends TaxiiServer {

    private static final Logger log = LoggerFactory.getLogger(Taxii21Server.class);

    @Size(min = 1, max = 140)
    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("contact")
    private String contact;

    @Field("default")
    private String defaultApiRoot;

    @Field("api_roots")
    private HashSet<String> apiRoots;

    @DBRef
    @Field("api_root_objects")
    private HashSet<ApiRoot> apiRootObjects;

    @DBRef
    @Field("collections")
    private HashSet<Taxii21Collection> collections;

    @Field("has_received_api_root_information")
    private boolean hasReceivedApiRootInformation = false;

    @Field("last_received_api_root_information")
    private Instant lastReceivedApiRootInformation = null;

    public Taxii21Server() {
        setVersion(Constants.TaxiiVersion.TAXII21);
    }

    public Taxii21Server(ServerDTO serverDTO) {
        super(serverDTO);
        setVersion(Constants.TaxiiVersion.TAXII21);
        URI uri = URI.create(serverDTO.getUrl());
        try {
            setUrl(new URI(uri.getScheme(), uri.getAuthority(), null, null, null));
        } catch (URISyntaxException e) {
            log.warn("Unable to set URL for server from '{}'", serverDTO.getUrl());
        }
    }

    public Taxii21Server updateFromDiscovery(Discovery discovery) {
        if (discovery == null) {
            log.error("Tried to update from a null discovery object");
            return this;
        }
        if (discovery.getTitle() != null) {
            this.setTitle(discovery.getTitle());
        }
        if (discovery.getDescription() != null) {
            this.setDescription(discovery.getDescription());
        }
        if (discovery.getContact() != null) {
            this.setContact(discovery.getContact());
        }
        if (discovery.getApiRoots() != null) {
            if (this.getApiRoots() == null) {
                this.setApiRoots(new HashSet<>());
            }
            this.getApiRoots().addAll(discovery.getApiRoots());
        }
        if (discovery.getDefaultApiRoot() != null) {
            this.setDefaultApiRoot(discovery.getDefaultApiRoot());
        }
        return this;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    private void setContact(String contact) {
        this.contact = contact;
    }

    public String getDefaultApiRoot() {
        return defaultApiRoot;
    }

    public void setDefaultApiRoot(String defaultApiRoot) {
        this.defaultApiRoot = defaultApiRoot;
    }

    public HashSet<String> getApiRoots() {
        return apiRoots;
    }

    public void setApiRoots(HashSet<String> apiRoots) {
        this.apiRoots = apiRoots;
    }

    public HashSet<ApiRoot> getApiRootObjects() {
        return apiRootObjects;
    }

    public void setApiRootObjects(HashSet<ApiRoot> apiRootObjects) {
        this.apiRootObjects = apiRootObjects;
    }

    public HashSet<Taxii21Collection> getCollections() {
        return collections;
    }

    public void setCollections(HashSet<Taxii21Collection> collections) {
        this.collections = collections;
    }

    public boolean hasReceivedApiRootInformation() {
        return hasReceivedApiRootInformation;
    }

    public void setHasReceivedApiRootInformation(boolean hasReceivedApiRootInformation) {
        this.hasReceivedApiRootInformation = hasReceivedApiRootInformation;
    }

    public Instant getLastReceivedApiRootInformation() {
        return lastReceivedApiRootInformation;
    }

    public void setLastReceivedApiRootInformation(Instant lastReceivedApiRootInformation) {
        this.lastReceivedApiRootInformation = lastReceivedApiRootInformation;
    }

    @Override
    public Constants.TaxiiVersion getVersion() {
        return Constants.TaxiiVersion.TAXII21;
    }

    /** /taxii */
    public URI getServerInformationUrl() {
        return this.getUrl().resolve(Constants.TAXII2_TAXII_ENDPOINT).normalize();
    }

    /** /{apiRoot} */
    public URI getApiRootUrl(String apiRoot) {
        if (apiRoot == null) {
            return null;
        }
        if (!apiRoot.endsWith("/")) {
            apiRoot += "/";
        }
        URI uri = URI.create(apiRoot);
        if (uri.getScheme() == null) {

            if (!apiRoot.startsWith("/")) {
                apiRoot = "/" + apiRoot;
            }
            return this.getUrl().resolve(apiRoot).normalize();
        }
        return uri;
    }

    /** /{apiRoot}/status/{statusId} */
    public URI getStatusUrl(String apiRootEndpoint, String statusId) {
        return this.getApiRootUrl(apiRootEndpoint).resolve(String.join("/", Constants.TAXII2_STATUS_ENDPOINT, statusId, "/")).normalize();
    }

    /** /{apiRoot}/collections/ */
    public URI getCollectionInformationUrl(String apiRootEndpoint) {
        return this.getApiRootUrl(apiRootEndpoint).resolve(Constants.TAXII2_COLLECTIONS_ENDPOINT).normalize();
    }

    /** /{apiRoot}/collections/{collectionId} */
    private URI getCollectionUrl(String apiRootEndpoint, String collectionId) {
        return this.getApiRootUrl(apiRootEndpoint).resolve(String.join("/", Constants.TAXII2_COLLECTIONS_ENDPOINT, collectionId, "/")).normalize();
    }

    /** /{apiRoot}/collections/{collectionId}/objects/ */
    public URI getCollectionObjectsUrl(String apiRootEndpoint, String collectionId) {
        return this.getCollectionUrl(apiRootEndpoint, collectionId).resolve(Constants.TAXII2_OBJECTS_ENDPOINT).normalize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxiiServer that = (TaxiiServer) o;
        return Objects.equals(getId(), that.getId()) &&
            Objects.equals(getLabel(), that.getLabel());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getLabel());
    }

}
