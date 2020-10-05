package com.bcmc.xor.flare.client.api.service.dto;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.security.SecurityUtils;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;

/**
 *  * A DTO representing a server
 */

public class ServerDTO {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotBlank
    @Size(min = 1, max = 50)
    private String label;

    @URL
    @NotBlank
    @Size(max = 255)
    private String url;

    @Size(max = 5000)
    private String serverDescription;

    private Constants.TaxiiVersion version;

    private boolean isAvailable = false;

    private boolean hasReceivedServerInformation = false;

    private boolean hasReceivedCollectionInformation = false;

    private boolean requiresBasicAuth = false;

    private Instant lastReceivedServerInformation = null;

    private Instant lastReceivedCollectionInformation = null;

    private Instant lastUpdated = null;

    private boolean hasCredentials = false;

    private String username;

    private String password;

    private String defaultApiRoot;

    private HashSet<String> apiRoots;

    public ServerDTO() {
    }

    public ServerDTO(TaxiiServer server) {
        this.id = server.getId();
        this.label = server.getLabel();
        this.url = server.getUrl() == null ? null : server.getUrl().toString();
        this.version = server.getVersion();
        this.serverDescription = server.getServerDescription();
        this.isAvailable = server.isAvailable();
        this.hasReceivedServerInformation = server.hasReceivedServerInformation();
        this.hasReceivedCollectionInformation = server.hasReceivedCollectionInformation();
        this.lastReceivedServerInformation = server.getLastReceivedServerInformation();
        this.lastReceivedCollectionInformation = server.getLastReceivedCollectionInformation();
        this.lastUpdated = server.getLastUpdated();
        this.requiresBasicAuth = server.getRequiresBasicAuth();
        if (requiresBasicAuth && SecurityUtils.getCurrentUserLogin().isPresent()) {
            this.hasCredentials = ServerCredentialsUtils.getInstance().getServerCredentialsMap().get(SecurityUtils.getCurrentUserLogin().get()) != null
                && ServerCredentialsUtils.getInstance().getServerCredentialsMap().get(SecurityUtils.getCurrentUserLogin().get()).containsKey(this.getLabel());
        }
        this.defaultApiRoot = server.getDefaultApiRoot();
        this.apiRoots = server.getApiRoots();
    }


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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @SuppressWarnings("unused")
    public Constants.TaxiiVersion getVersion() {
        return version;
    }

    public void setVersion(Constants.TaxiiVersion version) {
        this.version = version;
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
        isAvailable = available;
    }

    public boolean isHasReceivedServerInformation() {
        return hasReceivedServerInformation;
    }

    public void setHasReceivedServerInformation(boolean hasReceivedServerInformation) {
        this.hasReceivedServerInformation = hasReceivedServerInformation;
    }

    public boolean isHasReceivedCollectionInformation() {
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

    @SuppressWarnings("unused")
    public void setLastReceivedCollectionInformation(Instant lastReceivedCollectionInformation) {
        this.lastReceivedCollectionInformation = lastReceivedCollectionInformation;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean getRequiresBasicAuth() {
        return requiresBasicAuth;
    }

    public void setRequiresBasicAuth(boolean requiresBasicAuth) {
        this.requiresBasicAuth = requiresBasicAuth;
    }

    public boolean isHasCredentials() {
        return hasCredentials;
    }

    public void setHasCredentials(boolean hasCredentials) {
        this.hasCredentials = hasCredentials;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
