package com.bcmc.xor.flare.client.api.service.scheduled.async;

import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.StatusService;
import com.bcmc.xor.flare.client.api.service.TaxiiService;
import com.bcmc.xor.flare.client.error.RequestException;
import com.bcmc.xor.flare.client.error.TaxiiErrorResponseException;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
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
                    String details = "";
                    if (ex.getClass().equals(RequestException.class)){
                        details = String.format("(status=%s, message=%s)",
                                ((RequestException)ex).getStatus().getStatusCode(),
                                ((RequestException)ex).getTitle());
                    }
                    eventService.createEvent(EventType.ASYNC_FETCH_ERROR,
                            String.format("Error updating status with ID '%s' %s", status.getId(), details),
                            association);
                    status.incrementErrorCount();
                    statusService.save(status);
                    log.error("[x] Updated error for status id='{}' (type={}).", status.getId(), ex.getClass(), ex);
                }
            }
        }
    }
}
