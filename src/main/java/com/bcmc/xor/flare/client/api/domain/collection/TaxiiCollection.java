package com.bcmc.xor.flare.client.api.domain.collection;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.audit.AbstractAuditingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * An abstract base class for a TAXII Collection
 */
@Document(collection = Constants.RepositoryLabels.TAXII_COLLECTION)
public abstract class TaxiiCollection extends AbstractAuditingEntity implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(TaxiiCollection.class);

    @Id
    private String id;

    @Field("display_name")
    private String displayName;

    @Field("taxii_version")
    private Constants.TaxiiVersion taxiiVersion;

    /**
     * A server's ID, not its label, denoting which server this collection belongs to
     */
    @Field("server_ref")
    private String serverRef;

    @Field("content_volume")
    private long contentVolume;

    @Field("latest_fetch")
    private Instant latestFetch;

    TaxiiCollection() {
    }


//    public ResponseEntity<String> fetchContent(DownloadService downloadService, ApiParameters apiParameters) {
//        TaxiiServer server = downloadService.getServer(apiParameters.getServerRef());
//        validateInput(downloadService, apiParameters, server);
//        // If no user present in fetchParams (recurring fetch), then check current logged in user
//        if (apiParameters.getUserRef() == null || apiParameters.getUserRef().isEmpty()) {
//            apiParameters.setUserRef(SecurityUtils.getCurrentUserLogin().orElseThrow(IllegalStateException::new));
//        }
//        prepareRequest(downloadService, apiParameters, server);
//        if (apiParameters.getRepeat() && apiParameters.getWindow() > 0) {
//            startRecurringFetch(downloadService, new RecurringFetch(apiParameters));
//            return ResponseEntity.ok().headers(HeaderUtil.createAlert("Started recurring fetch", this.getDisplayName())).body("Started recurring fetch");
//        } else {
//            switch (server.getVersion()) {
//                case TAXII21:
//                    startAsyncFetch(downloadService, new Taxii20AsyncFetch((Taxii20GetParameters) apiParameters, downloadService.getFetchChunkPageSize()));
//                    return ResponseEntity.ok().headers(HeaderUtil.createAlert("Started async fetch", this.getDisplayName())).body("Started async fetch");
//                case TAXII11:
//                    startAsyncFetch(downloadService, new Taxii11AsyncFetch((Taxii11PollParameters) apiParameters, downloadService.getFetchChunkWindow()));
//                    return ResponseEntity.ok().headers(HeaderUtil.createAlert("Started async fetch", this.getDisplayName())).body("Started async fetch");
//                default:
//                    return ResponseEntity.unprocessableEntity()
//                        .headers(HeaderUtil.createFailureAlert(server.getLabel(), ErrorConstants.ERR_BAD_REQUEST, "Cannot determine server version"))
//                        .body("Cannot determine server version");
//            }
//        }
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Constants.TaxiiVersion getTaxiiVersion() {
        return taxiiVersion;
    }

    public void setTaxiiVersion(Constants.TaxiiVersion taxiiVersion) {
        this.taxiiVersion = taxiiVersion;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getServerRef() {
        return serverRef;
    }

    void setServerRef(String serverRef) {
        this.serverRef = serverRef;
    }

    public long getContentVolume() {
        return contentVolume;
    }

    public void setContentVolume(long contentVolume) {
        this.contentVolume = contentVolume;
    }

    public Instant getLatestFetch() {
        return latestFetch;
    }

    public void setLatestFetch(Instant latestFetch) {
        this.latestFetch = latestFetch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxiiCollection that = (TaxiiCollection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TaxiiCollection{" +
            "id='" + id + '\'' +
            ", displayName='" + displayName + '\'' +
            ", taxiiVersion=" + taxiiVersion +
            ", serverRef='" + serverRef + '\'' +
            ", contentVolume=" + contentVolume +
            ", latestFetch=" + latestFetch +
            "} " + super.toString();
    }
}
