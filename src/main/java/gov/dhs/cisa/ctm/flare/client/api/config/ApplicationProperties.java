package gov.dhs.cisa.ctm.flare.client.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Properties specific to Flareclient.
 * <p>
 * Properties are configured in the application.yml file.
 */
@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "flare", ignoreUnknownFields = true)
public class ApplicationProperties {

    private String schemasDirectory;
    private List<String> supportedContentBindings;
    private int statusUpdateInterval;
    private int validationInterval;
    private int serverInfoUpdateInterval;
    private int maxPageSize;
    private int fetchChunkWindow;
    private int fetchChunkPageSize;

    /* todo: add these values from application.yml
    security:
        authentication:
            jwt:
                secret: EoP3UiBuHr
                # Token is valid 24 hours
                token-validity-in-seconds: 86400
                token-validity-in-seconds-for-remember-me: 2592000
     */

    public ApplicationProperties() {
    }

    public String getSchemasDirectory() {
        return schemasDirectory;
    }

    public void setSchemasDirectory(String schemasDirectory) {
        this.schemasDirectory = schemasDirectory;
    }

    public List<String> getSupportedContentBindings() {
        return supportedContentBindings;
    }

    public void setSupportedContentBindings(List<String> supportedContentBindings) {
        this.supportedContentBindings = supportedContentBindings;
    }

    public int getStatusUpdateInterval() {
        return statusUpdateInterval;
    }

    public void setStatusUpdateInterval(int statusUpdateInterval) {
        this.statusUpdateInterval = statusUpdateInterval;
    }

    public int getValidationInterval() {
        return validationInterval;
    }

    public void setValidationInterval(int validationInterval) {
        this.validationInterval = validationInterval;
    }

    public int getServerInfoUpdateInterval() {
        return serverInfoUpdateInterval;
    }

    public void setServerInfoUpdateInterval(int serverInfoUpdateInterval) {
        this.serverInfoUpdateInterval = serverInfoUpdateInterval;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public int getFetchChunkWindow() {
        return fetchChunkWindow;
    }

    public void setFetchChunkWindow(int fetchChunkWindow) {
        this.fetchChunkWindow = fetchChunkWindow;
    }

    public int getFetchChunkPageSize() {
        return fetchChunkPageSize;
    }

    public void setFetchChunkPageSize(int fetchChunkPageSize) {
        this.fetchChunkPageSize = fetchChunkPageSize;
    }
}
