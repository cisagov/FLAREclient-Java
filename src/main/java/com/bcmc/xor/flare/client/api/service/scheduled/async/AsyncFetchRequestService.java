package com.bcmc.xor.flare.client.api.service.scheduled.async;

import com.bcmc.xor.flare.client.api.domain.async.AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.async.FetchChunk;
import com.bcmc.xor.flare.client.api.domain.async.Taxii11AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.async.Taxii21AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.content.CountResult;
import com.bcmc.xor.flare.client.api.domain.parameters.ApiParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii21GetParameters;
import com.bcmc.xor.flare.client.api.repository.AsyncFetchRequestRepository;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.DownloadService;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
import com.mongodb.DuplicateKeyException;
import org.mitre.taxii.messages.xml11.PollResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class AsyncFetchRequestService {

    private static final Logger log = LoggerFactory.getLogger(AsyncFetchRequestService.class);
    private AsyncFetchRequestRepository repository;
    private final CollectionService collectionService;
    private DownloadService downloadService;
    private UserService userService;
    private EventService eventService;

    @Value("${flare.fetch-chunk-window}")
    private int fetchChunkWindow;

    @Value("${flare.fetch-chunk-page-size}")
    private int fetchChunkPageSize;

    public AsyncFetchRequestService(@Qualifier("async_fetch") AsyncFetchRequestRepository repository, CollectionService collectionService, DownloadService downloadService, UserService userService, EventService
        eventService) {
        this.repository = repository;
        this.collectionService = collectionService;
        this.downloadService = downloadService;
        this.userService = userService;
        this.eventService = eventService;
    }

    @Async("taskExecutor")
    @Scheduled(fixedRate = 5000)
    public synchronized void performAsyncFetchRequest() {
        List<AsyncFetch> requests = repository.findAllByStatus(AsyncFetch.Status.PENDING);
        if (!requests.isEmpty()) {
            log.debug("Found {} async fetch request objects with 'pending' status", requests.size());
            for (AsyncFetch request : requests) {
                request.setStatus(AsyncFetch.Status.FETCHING);
                repository.save(request);

                try {
                    processAsyncFetch(request);
                } catch (Exception e) {
                    request.setStatus(AsyncFetch.Status.ERROR);
                    repository.save(request);
                    throw e;
                }

            }
        }
    }

    public <T extends ApiParameters> void startAsyncFetch(TaxiiAssociation association, T fetchParams) {
        switch (association.getServer().getVersion()) {
            case TAXII21:
                startAsyncFetch(new Taxii21AsyncFetch((Taxii21GetParameters) fetchParams));
                break;
            case TAXII11:
                startAsyncFetch(new Taxii11AsyncFetch((Taxii11PollParameters) fetchParams, fetchChunkWindow));
                break;
            default:
                throw new IllegalStateException("Tried to start an async fetch for a server that didn't have a version");
        }
    }

    public void startAsyncFetch(AsyncFetch request) {
        log.debug("Saving async fetch for server '{}' and collection '{}'", request.getAssociation().getServer().getLabel(), request.getAssociation().getCollection().getDisplayName());
        try {
            repository.insert(request);
            TaxiiAssociation association = request.getAssociation();
            eventService.createEvent(EventType.ASYNC_FETCH_STARTED,
                    "Started async fetch '" + association.getCollection().getDisplayName() + "'(" + association.getServer().getLabel() + ")",
                    association);
        } catch (org.springframework.dao.DuplicateKeyException | DuplicateKeyException e) {
            log.warn("Duplicate recurring fetch found. Will ignore.");
        }
    }

    public void processAsyncFetch(AsyncFetch request) {

        if (request.getAssociation().getServer().getRequiresBasicAuth()) {
            ServerCredentialsUtils.getInstance().loadCredentialsForUser(request.getAssociation().getUser());
        }

        switch (request.getAssociation().getServer().getVersion()) {
            case TAXII21:
                try {
                    processAsyncFetch((Taxii21AsyncFetch) request);
                } catch (Exception e) {
                    request.setStatus(AsyncFetch.Status.ERROR);
                }
                request.setStatus(AsyncFetch.Status.COMPLETE);
                repository.save(request);
                break;
            case TAXII11:
                request.getInitialFetchParams().setFetchUrl(downloadService.getFetchUrl((Taxii11Association) request.getAssociation()));
                try {
                    processAsyncFetch((Taxii11AsyncFetch) request);
                } catch (Exception e) {
                    request.setStatus(AsyncFetch.Status.ERROR);
                }
                request.setStatus(AsyncFetch.Status.COMPLETE);
                repository.save(request);
                break;
            default:
                throw new IllegalStateException("Tried to process an async fetch for a server without a version");
        }
    }

    public void processAsyncFetch(Taxii11AsyncFetch asyncFetch) {
        log.info("--- TAXII 1.1 Async fetch started: '{}' ---", asyncFetch.getId());
        Taxii11PollParameters taxii11PollParameters = asyncFetch.getInitialFetchParams();
        Taxii11Association taxiiAssociation = (Taxii11Association) asyncFetch.getAssociation();
        CountResult countResult = new CountResult();
        int retryCount = 0;
        while (asyncFetch.hasNext() && retryCount < 3) {

            // Pop a chunk from queue
            @SuppressWarnings("unchecked") FetchChunk<Instant> fetchChunk = asyncFetch.next();

            // Set begin and end dates
            taxii11PollParameters.setStartDate(fetchChunk.getBegin().atZone(ZoneId.of("Z")));
            taxii11PollParameters.setEndDate(fetchChunk.getEnd().atZone(ZoneId.of("Z")));

            // TODO throwing null pointer here
            PollResponse response = downloadService.fetchContent(taxiiAssociation, taxii11PollParameters);

            if (response == null) {
                retryCount ++;
                log.debug("Response from async fetch on server '{}', collection '{}' is null", taxiiAssociation.getServer().getLabel(), taxiiAssociation.getCollection().getName());
                eventService.createEvent(EventType.ASYNC_FETCH_ERROR, "Response from server was null", taxiiAssociation);
            } else {
                CountResult collectionCount = downloadService.processTaxii11Content(response, taxiiAssociation);
                countResult.add(collectionCount);

                // Update latest fetch
                if (response.getInclusiveEndTimestamp() != null) {
                    Instant latest = response.getInclusiveEndTimestamp().toGregorianCalendar(TimeZone.getTimeZone("Z"), null, null).toInstant();
                    taxiiAssociation.getCollection().setLatestFetch(latest);
                    asyncFetch.setLastRequested(latest);
                } else {
                    log.debug("No inclusive end found in response, so setting latest fetch to now");
                    taxiiAssociation.getCollection().setLatestFetch(Instant.now());
                    asyncFetch.setLastRequested(Instant.now());
                }
                log.debug("Setting latest fetch for '{}': {}", taxiiAssociation.getCollection().getDisplayName(), taxiiAssociation.getCollection().getLatestFetch());
                // Save the collection to update the latest fetch time
                collectionService.save(taxiiAssociation.getCollection());

                eventService.createEvent(EventType.ASYNC_FETCH_UPDATE, String.format("Fetch for '%s'(%s) retrieved and processed %d content blocks. Found %d duplicates. Saved %d.",
                    taxiiAssociation.getCollection().getName(), taxiiAssociation.getServer().getLabel(),
                    collectionCount.getContentCount(), collectionCount.getContentDuplicate(), collectionCount.getContentSaved()),
                    taxiiAssociation);

                // Save the async fetch request to update the chunks
                repository.save(asyncFetch);
            }
        }

        String feedback;
        if (retryCount >= 3) {
            feedback = "Async fetch match retries attempted.";
            eventService.createEvent(EventType.ASYNC_FETCH_ERROR, feedback, taxiiAssociation);
            asyncFetch.setStatus(AsyncFetch.Status.ERROR);
        } else {
            feedback = String.format("Async fetch complete for '%s'(%s). Retrieved and processed %d content blocks. Found %d duplicates. Saved %d.",
                    taxiiAssociation.getCollection().getName(), taxiiAssociation.getServer().getLabel(),
                    countResult.getContentCount(), countResult.getContentDuplicate(), countResult.getContentSaved());
            eventService.createEvent(EventType.ASYNC_FETCH_COMPLETE, feedback, taxiiAssociation);
            asyncFetch.setStatus(AsyncFetch.Status.COMPLETE);
        }
        asyncFetch.setCountResult(countResult);
        repository.save(asyncFetch);
        collectionService.save(taxiiAssociation.getCollection());
        log.info("--- TAXII 1.1 Async fetch complete '{}' ---", asyncFetch.getId());
    }

    public void processAsyncFetch(Taxii21AsyncFetch asyncFetch) {
        log.info("--- TAXII 2.1 Async fetch started: '{}' ---", asyncFetch.getId());
        Taxii21GetParameters taxii21GetParameters = asyncFetch.getInitialFetchParams();
        Taxii21Association taxiiAssociation = (Taxii21Association) asyncFetch.getAssociation();

        CountResult countResult = new CountResult();
        downloadService.fetchContent(taxiiAssociation, taxii21GetParameters, new FetchChunk<>(0, 0), countResult);
        collectionService.save(taxiiAssociation.getCollection());
        String feedback = String.format("Fetch complete for '%s'(%s). Retrieved and processed %d content blocks. Found %d duplicates. Saved %d.",
                taxiiAssociation.getCollection().getDisplayName(), taxiiAssociation.getServer().getLabel(),
            countResult.getContentCount(), countResult.getContentDuplicate(), countResult.getContentSaved());
        eventService.createEvent(EventType.ASYNC_FETCH_COMPLETE, feedback, taxiiAssociation);
        log.info("--- TAXII 2.1 Async fetch complete '{}' ---", asyncFetch.getId());
    }

    public Optional<AsyncFetch> findExisting(TaxiiAssociation taxiiAssociation) {
        return repository.findOneByAssociationAndStatus(taxiiAssociation, AsyncFetch.Status.FETCHING);
    }

    /**
     * Delete all Async fetches by the server label
     */
    public void deleteAllAsyncFetchesByServerLabel(String serverLabel) {
        log.info("Deleting all recurring fetches for server '{}'", serverLabel);
        getRepository().deleteAllByInitialFetchParamsServerLabelEquals(serverLabel);
    }

    // Dependencies
    public AsyncFetchRequestRepository getRepository() {
        return repository;
    }

    public void setRepository(AsyncFetchRequestRepository repository) {
        this.repository = repository;
    }

    public DownloadService getDownloadService() {
        return downloadService;
    }

    public void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
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
