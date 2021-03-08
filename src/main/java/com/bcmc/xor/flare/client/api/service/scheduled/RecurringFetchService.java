package com.bcmc.xor.flare.client.api.service.scheduled;

import com.bcmc.xor.flare.client.api.domain.async.AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.async.RecurringFetch;
import com.bcmc.xor.flare.client.api.domain.async.Taxii11AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.async.Taxii21AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.parameters.ApiParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii21GetParameters;
import com.bcmc.xor.flare.client.api.repository.RecurringFetchRepository;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.api.service.scheduled.async.AsyncFetchRequestService;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@Service
public class RecurringFetchService {

    private RecurringFetchRepository repository;
    private ServerService serverService;
    private CollectionService collectionService;
    private UserService userService;
    private EventService eventService;
    private AsyncFetchRequestService asyncFetchRequestService;

    @Value("${flare.fetch-chunk-window}")
    private int fetchChunkWindow;

    @Value("${flare.fetch-chunk-page-size}")
    private int fetchChunkPageSize;

    private static final Logger log = LoggerFactory.getLogger(RecurringFetchService.class);

    public RecurringFetchService(@Qualifier("recurring_fetch") RecurringFetchRepository repository, ServerService serverService, CollectionService collectionService, UserService userService, EventService eventService, AsyncFetchRequestService asyncFetchRequestService) {
        this.repository = repository;
        this.serverService = serverService;
        this.collectionService = collectionService;
        this.userService = userService;
        this.eventService = eventService;
        this.asyncFetchRequestService = asyncFetchRequestService;
    }

    public Optional<RecurringFetch> findByAssociation(TaxiiAssociation association) {
        return repository.findByAssociation(association);
    }

    public void startRecurringFetch(RecurringFetch recurringFetch) {
        TaxiiAssociation association = recurringFetch.getAssociation();
        ServerCredentialsUtils.getInstance().loadCredentialsForUser(association.getUser());

        if (recurringFetch.getId() == null || (recurringFetch.getId() != null && !repository.existsById(recurringFetch.getId()))) {
            log.debug("Saving recurring fetch for server {} and collection {}",
                association.getServer().getLabel(), association.getCollection().getDisplayName());
            try {
                repository.insert(recurringFetch);
                eventService.createEvent(EventType.RECURRING_POLL_STARTED,
                    String.format("Started recurring fetch for server '%s' and collection '%s'", association.getServer().getLabel(), association.getCollection().getDisplayName()),
                    association);
            } catch (org.springframework.dao.DuplicateKeyException | DuplicateKeyException e) {
                log.warn("Duplicate recurring fetch found. Will ignore.");
            }
        }
    }

    private void processRecurringFetch(Taxii11Association association, RecurringFetch<Taxii11PollParameters> recurringFetch) {
        log.debug("Processing recurring fetch for server '{}' and collection '{}'", association.getServer().getLabel(), association.getCollection().getDisplayName());
        Optional<AsyncFetch> existingAsyncFetch = asyncFetchRequestService.findExisting(association);
        if (!existingAsyncFetch.isPresent()) {
            Instant latestFetch = association.getCollection().getLatestFetch();
            if (latestFetch == null ||
                latestFetch.plus(recurringFetch.getApiParameters().getWindow(), ChronoUnit.MINUTES).isBefore(Instant.now())) {
                association.getCollection().setLatestFetch(Instant.now()); //Set latest fetch to now. It was null if first time poll
                recurringFetch.getApiParameters().setStartDate(association.getCollection().getLatestFetch().atZone(ZoneId.of("Z")));
                recurringFetch.getApiParameters().setEndDate(ZonedDateTime.now());
                asyncFetchRequestService.startAsyncFetch(new Taxii11AsyncFetch(recurringFetch.getApiParameters(), fetchChunkWindow));
                repository.save(recurringFetch);
            }
        }
    }

    private void processRecurringFetch(Taxii21Association association, RecurringFetch<Taxii21GetParameters> recurringFetch) {
        log.debug("Processing recurring fetch for server '{}' and collection '{}'", association.getServer().getLabel(), association.getCollection().getDisplayName());
        Optional<AsyncFetch> existingAsyncFetch = asyncFetchRequestService.findExisting(association);
        if (!existingAsyncFetch.isPresent()) {
            Instant latestFetch = association.getCollection().getLatestFetch();
            if (latestFetch == null ||
                latestFetch.plus(recurringFetch.getApiParameters().getWindow(), ChronoUnit.MINUTES).isBefore(Instant.now())) {
                association.getCollection().setLatestFetch(Instant.now()); //Set latest fetch to now. It was null if first time poll
//                log.info("Recurring fetch: " + recurringFetch);
                recurringFetch.getApiParameters().setAddedAfter(association.getCollection().getLatestFetch().atZone(ZoneId.of("Z")));
                asyncFetchRequestService.startAsyncFetch(new Taxii21AsyncFetch(recurringFetch.getApiParameters()));
                repository.save(recurringFetch);
            }
        }
    }

    /**
     * An asynchronous service that will check if there are any {@link ApiParameters} persisted in the local database, comparing their windows and last fetch times, to determine if
     * another fetch should be performed when this service executes. This service executes every minute.
     */
    @Scheduled(cron = "0 * * * * *")
    public synchronized void recurringFetch() {
        List<RecurringFetch> listOfRecurringFetches = repository.findAll();
        if (!listOfRecurringFetches.isEmpty()) {
            log.trace("Found {} recurring fetches", listOfRecurringFetches.size());
            for (RecurringFetch recurringFetch : listOfRecurringFetches) {
                TaxiiAssociation taxiiAssociation = recurringFetch.getAssociation();

                Instant now = Instant.now();
                boolean shouldFetch = taxiiAssociation.getCollection().getLatestFetch() == null || now.minus(Duration.ofMinutes(recurringFetch.getWindow())).isAfter
                    (taxiiAssociation.getCollection().getLatestFetch());
                if (shouldFetch) {
                    switch (taxiiAssociation.getCollection().getTaxiiVersion()) {
                        case TAXII21:
                            //noinspection unchecked
                            processRecurringFetch((Taxii21Association) taxiiAssociation, recurringFetch);
                            break;
                        case TAXII11:
                            //noinspection unchecked
                            processRecurringFetch((Taxii11Association) taxiiAssociation, recurringFetch);
                            break;
                        default:
                            log.warn("Collection found for recurring fetch did not have a version. Deleting...");
                            repository.delete(recurringFetch);
                    }
                }
            }
        }
    }

    /**
     * Delete a recurring fetch
     * @param recurringFetch the RecurringFetch object to delete from the repository
     * @return a ResponseEntity containing feedback in the form of either HTTP body or headers
     */
    private ResponseEntity<String> deleteRecurringFetch(RecurringFetch recurringFetch) {
        if (recurringFetch != null) {
            repository.delete(recurringFetch);
            deleteRecurringFetch(recurringFetch.getId());
            eventService.createEvent(EventType.RECURRING_POLL_DELETED, String.format("Deleted recurring fetch with ID '%s'", recurringFetch.getId()), recurringFetch.getAssociation());
        }
        String feedback = "Deleted recurring fetch.";
        return ResponseEntity.ok().body(feedback);
    }

    /**
     * Delete a recurring fetch by its ID
     * @param id the ID of the FetchParams
     * @return a ResponseEntity containing feedback in the form of either HTTP body or headers
     */
    private ResponseEntity<String> deleteRecurringFetch(String id) {
        log.info("Stopping recurring fetch with id: {}", id);
        Optional<RecurringFetch> params = repository.findById(id);
        if (params.isPresent()) {
            return deleteRecurringFetch(params.get());
        } else {
            String feedback = "Successfully deleted recurring fetch.";
            return ResponseEntity.ok().body(feedback);
        }
    }

    /**
     * Delete a recurring fetch by the server and collection associated with it (there should only be one per server-collection pair)
     * @return a ResponseEntity containing feedback in the form of either HTTP body or headers
     */
    public ResponseEntity<String> deleteRecurringFetch(TaxiiAssociation association) {
        log.info("Stopping recurring fetch for server '{}' and collection '{}'", association.getServer().getLabel(), association.getCollection().getDisplayName());
        Optional<RecurringFetch> recurringFetch = repository.findByAssociation(association);
        if (recurringFetch.isPresent()) {
            return deleteRecurringFetch(recurringFetch.get());
        } else {
            String feedback = String.format("No recurring fetch exists for server '%s' and collection '%s'",
                association.getServer().getLabel(), association.getCollection().getDisplayName());
            return ResponseEntity.badRequest().body(feedback);
        }
    }

    /**
     * Delete all recurring fetches by the server label
     */
    public void deleteAllRecurringFetchesByServerLabel(String serverLabel) {
        log.info("Deleting all recurring fetches for server '{}'", serverLabel);
        repository.deleteAllByApiParametersServerLabelEquals(serverLabel);
    }


    public RecurringFetchRepository getRepository() {
        return repository;
    }

    public void setRepository(RecurringFetchRepository repository) {
        this.repository = repository;
    }

    public ServerService getServerService() {
        return serverService;
    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    public CollectionService getCollectionService() {
        return collectionService;
    }

    public void setCollectionService(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public AsyncFetchRequestService getAsyncFetchRequestService() {
        return asyncFetchRequestService;
    }

    public void setAsyncFetchRequestService(AsyncFetchRequestService asyncFetchRequestService) {
        this.asyncFetchRequestService = asyncFetchRequestService;
    }
}
