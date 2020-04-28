package com.bcmc.xor.flare.client.api.service.scheduled.async;

import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.StatusService;
import com.bcmc.xor.flare.client.api.service.TaxiiService;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20Association;
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
            log.debug("Found {} status objects with 'pending' status", statuses.size());
            for (Status status : statuses) {
                Taxii20Association association = status.getAssociation();
                if (association.getUser() != null) {
                    ServerCredentialsUtils.getInstance().loadCredentialsForUser(association.getUser());
                }
                URI statusUrl = association.getServer().getStatusUrl(association.getCollection().getApiRootRef(), status.getId());
                Status response = taxiiService.getTaxii20RestTemplate().getStatus(association.getServer(), statusUrl);
                eventService.createEvent(EventType.STATUS_UPDATED, String.format("Updated status with ID '%s'. Status is now: %s, with %d pending.",
                    response.getId(), response.getStatus(), response.getPendingCount()), association);
                response.setAssociation(association);
                statusService.save(response);
            }
        }
    }
}
