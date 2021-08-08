package gov.dhs.cisa.ctm.flare.client.api.service.scheduled.async;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import gov.dhs.cisa.ctm.flare.client.api.domain.audit.EventType;
import gov.dhs.cisa.ctm.flare.client.api.domain.status.Status;
import gov.dhs.cisa.ctm.flare.client.api.security.ServerCredentialsUtils;
import gov.dhs.cisa.ctm.flare.client.api.service.EventService;
import gov.dhs.cisa.ctm.flare.client.api.service.StatusService;
import gov.dhs.cisa.ctm.flare.client.api.service.TaxiiService;
import gov.dhs.cisa.ctm.flare.client.error.RequestException;
import gov.dhs.cisa.ctm.flare.client.error.TaxiiErrorResponseException;
import gov.dhs.cisa.ctm.flare.client.taxii.taxii21.Taxii21Association;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

/**
 * A service for asynchronously updating {@link Status} objects
 */
@SuppressWarnings("unused")
@Service
public class AsyncStatusUpdateService {

    private static final Logger log = LoggerFactory.getLogger(AsyncStatusUpdateService.class);
    private final EventService eventService;
    private final TaxiiService taxiiService;
    private final StatusService statusService;

    public AsyncStatusUpdateService(EventService eventService, TaxiiService taxiiService, StatusService statusService) {
        this.taxiiService = taxiiService;
        this.eventService = eventService;
        this.statusService = statusService;
    }

    /**
     * This method will find all Status objects in the database that have the status "pending", and makes a request to the associated server to update that Status object.
     */
    @Async("taskExecutor")
    @Scheduled(fixedRateString = "${flare.status-update-interval}")
    public synchronized void checkStatus() {
        List<Status> statuses = statusService.getPending();
        if (!statuses.isEmpty()) {
            log.debug("[ ] Updating {} status objects with 'pending' status", statuses.size());
            for (Status status : statuses) {
                Taxii21Association association = status.getAssociation();
                if (association.getUser() != null) {
                    ServerCredentialsUtils.getInstance().loadCredentialsForUser(association.getUser());
                }
                URI statusUrl = association.getServer().getStatusUrl(association.getCollection().getApiRootRef(), status.getId());
                try {
                    Status response = taxiiService.getTaxii21RestTemplate().getStatus(association.getServer(), statusUrl);
                    eventService.createEvent(EventType.STATUS_UPDATED,
                            String.format("Updated status with ID '%s'. Status is now: %s, with %d pending.", response.getId(), response.getStatus(), response.getPendingCount()),
                            association);
                    response.setAssociation(association);
                    statusService.save(response);
                    log.info("[*] Updated status id='{}'", status.getId());

                } catch (Exception ex) {
                    // If we have an error, communicate the error: (1) update events to show
                    // that we have errors and (2) update the status to note how many times
                    // we encounter errors when fetching the status.
                    String details = "";
                    String message = null;
                    if (ex.getClass().equals(RequestException.class)){
                        String errorTitle = ((RequestException)ex).getTitle();
                        if (errorTitle != null){
                            try {
                                JsonElement errorTitleJson = new JsonParser().parse(errorTitle);
                                if (errorTitleJson.isJsonObject() && errorTitleJson.getAsJsonObject().has("description")){
                                    message = errorTitleJson.getAsJsonObject().get("description").getAsString();
                                }
                            } catch (Exception jsonEx){
                                // This may happen: error title is not json
                            }

                            if (message == null){
                                message = errorTitle;
                            }
                        } else {
                            message = "";
                        }

                        details = String.format("(status=%s, message=%s)", ((RequestException)ex).getStatus().getStatusCode(), message);
                    }

                    // Create a fetch error event that will show up in the ui. This way, the user
                    // sees there was an error and that the status isn't just "pending"
                    eventService.createEvent(EventType.ASYNC_FETCH_ERROR,
                            String.format("Error updating status with ID '%s' %s", status.getId(), details),
                            association);

                    // Since the ui doesn't really hold any state regarding the statuses (the client-ui
                    // just asks the back end for the state of status), we will track the state with
                    // regards to errors fetching the status here in the backend.
                    status.incrementErrorCount();
                    statusService.save(status);
                    log.error("[x] Updated error for status id='{}' (type={}).", status.getId(), ex.getClass(), ex);
                }
            }
        }
    }
}
