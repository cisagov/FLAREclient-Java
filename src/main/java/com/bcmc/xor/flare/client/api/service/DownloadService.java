package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.async.FetchChunk;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.content.CountResult;
import com.bcmc.xor.flare.client.api.domain.content.Stix1ContentWrapper;
import com.bcmc.xor.flare.client.api.domain.content.Stix2ContentWrapper;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii21GetParameters;
import com.bcmc.xor.flare.client.error.InsufficientInformationException;
import com.bcmc.xor.flare.client.error.InternalServerErrorException;
import com.bcmc.xor.flare.client.error.RequestException;
import com.bcmc.xor.flare.client.error.StatusMessageResponseException;
import com.bcmc.xor.flare.client.taxii.TaxiiHeaders;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Headers;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21RestTemplate;
import com.google.gson.JsonElement;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteResult;
import org.mitre.taxii.messages.xml11.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import xor.bcmc.taxii2.JsonHandler;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Service
public class DownloadService {
    private static final Logger log = LoggerFactory.getLogger(DownloadService.class);

    private TaxiiService taxiiService;

    private EventService eventService;

    private MongoTemplate mongoTemplate;

    @Value("${flare.timeout.default:5000}")
    private int defaultTimeout;

    @Value("${flare.timeout.taxii-21.download:5000}")
    private int taxii21Timeout;

    public DownloadService(TaxiiService taxiiService, EventService eventService, MongoTemplate mongoTemplate) {
        this.taxiiService = taxiiService;
        this.eventService = eventService;
        this.mongoTemplate = mongoTemplate;
    }

    public URI getFetchUrl(Taxii11Association association) {
        // Attempt to get polling address from collection service contact information
        Optional<String> pollingUrl = Optional.empty();
        if (association.getCollection().getCollectionObject().getPollingServices() != null && !association.getCollection().getCollectionObject().getPollingServices().isEmpty()) {
            pollingUrl = association.getCollection().getCollectionObject().getPollingServices().stream()
                .filter(serviceContactInfoType -> serviceContactInfoType.getMessageBindings().contains(Constants.HEADER_TAXII11_MESSAGE_BINDING))
                .map(ServiceContactInfoType::getAddress)
                .findFirst();
        }

        // If couldn't find a polling address from collection information, attempt to get polling address from server service instances
        if (!pollingUrl.isPresent()) {
            if (association.getServer().getServiceInstances() != null) {
                pollingUrl = association.getServer().getServiceInstances(ServiceTypeEnum.POLL).stream()
                    .filter(serviceInstanceType ->
                        serviceInstanceType.getAddress() != null
                            && !serviceInstanceType.getAddress().isEmpty()
                            && serviceInstanceType.getMessageBindings().contains(Constants.HEADER_TAXII11_MESSAGE_BINDING))
                    .map(ServiceInstanceType::getAddress)
                    .findFirst();
            }
        }

        if (!pollingUrl.isPresent()) {
            throw new InsufficientInformationException("No polling services found for Collection " + association.getCollection().getId());
        }

        return URI.create(pollingUrl.get());
    }

    public PollResponse fetchContent(Taxii11Association association, Taxii11PollParameters pollParameters) {
        // Build the poll request
        PollRequest pollRequest = pollParameters.buildPollRequestForCollection(association.getCollection().getName());
        log.info("Polling collection '{}' at URL '{}'. Start: '{}'. End: '{}'. Message ID: '{}'",
            pollRequest.getCollectionName(), pollParameters.getFetchUrl(),
            pollParameters.getStartDate(), pollParameters.getEndDate().toString(), pollRequest.getMessageId());

        // Make request, handle errors
        PollResponse response;
        try {
            response = taxiiService.getTaxii11RestTemplate().submitPoll(association.getServer(), pollRequest, pollParameters.getFetchUrl());
        } catch (RequestException e) {
            eventService.createEvent(EventType.ASYNC_FETCH_ERROR, e.getMessage(), association);
            log.error("Encountered RequestException during chunked polling: {}", e.getMessage());
            throw e;
        } catch (StatusMessageResponseException e) {
            eventService.createEvent(EventType.ASYNC_FETCH_ERROR, e.getMessage(), association);
            log.warn("Encountered StatusMessage response during chunked polling: {}", e.getMessage());
            throw e;
        }
        return response;
    }

    public CountResult fetchContent(Taxii21Association association, Taxii21GetParameters parameters, FetchChunk<Integer> chunk, CountResult countResult) {

        prepareRequest(association, parameters);

        TaxiiHeaders headers = Taxii21Headers.fromServer(association.getServer()).withHeader("Accept", Arrays.asList(Constants.HEADER_STIX21_JSON, Constants.HEADER_TAXII21_JSON));

        if (chunk.getBegin() != 0 && chunk.getEnd() != 0) {
            headers.add("Range", String.format("items %d-%d", chunk.getBegin(), chunk.getEnd()));
        }

        ResponseEntity<String> responseEntity;
        Taxii21RestTemplate restTemplate = taxiiService.getTaxii21RestTemplate();
        try {
            restTemplate.setTimeouts(taxii21Timeout);
            responseEntity = restTemplate.executeGet(headers, parameters.getFetchUrl());
        } catch (RequestException e) {
            eventService.createEvent(EventType.ASYNC_FETCH_ERROR, e.getMessage(), association);
            log.error("Encountered RequestException when fetching content: {}", e.getMessage());
            throw e;
        } finally {
            restTemplate.setTimeouts(defaultTimeout);
        }

        JsonElement jsonContent = JsonHandler.getInstance().getGson().fromJson(responseEntity.getBody(), JsonElement.class);
        if (jsonContent == null) {
            eventService.createEvent(EventType.ASYNC_FETCH_ERROR, "Got a null envelope in response to a GET request", association);
            log.error("Encountered a null response when fetching content for server '{}' and collection '{}'",
                association.getServer().getLabel(), association.getCollection().getDisplayName());
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Envelope is null");
        }

        CountResult thisCount = processTaxii21Content(jsonContent, association);
        countResult.add(thisCount);

        eventService.createEvent(EventType.ASYNC_FETCH_UPDATE, String.format("Fetch for '%s'(%s) retrieved and processed %d content blocks. Found %d duplicates. Saved %d.",
            association.getCollection().getDisplayName(), association.getServer().getLabel(),
            thisCount.getContentCount(), thisCount.getContentDuplicate(), thisCount.getContentSaved()), association);

        association.getCollection().setLatestFetch(Instant.now());
        log.debug("Setting latest fetch for '{}': {}", association.getCollection().getDisplayName(), association.getCollection().getLatestFetch());

        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getStatusCodeValue() == 206) {
            if (responseEntity.getHeaders().containsKey("Content-Range") &&  responseEntity.getHeaders().get("Content-Range") != null) {
                FetchChunk<Integer> next = calculateNextChunk(responseEntity);
                if (!chunk.equals(next)) {
                    fetchContent(association, parameters, next, countResult);
                }
            } else {
                log.error("Received a 206 with no Content-Range header.");
            }
        }

        return countResult;
    }

    private FetchChunk<Integer> calculateNextChunk(ResponseEntity<String> responseEntity) {
        FetchChunk<Integer> nextChunk = new FetchChunk<>();
        List<String> contentRangeHeaderStrings = responseEntity.getHeaders().get("Content-Range");
        if (contentRangeHeaderStrings == null || contentRangeHeaderStrings.size() == 0) {
            throw new IllegalArgumentException("Content-Range cannot be null when calculating next chunk");
        }
        String[] strContentRange = contentRangeHeaderStrings.get(0).split("[ -/\\]]+");
        int parsedFrom = Integer.parseInt(strContentRange[1]);
        int parsedTo = Integer.parseInt(strContentRange[2]);
        int inclusiveRangeWindow = parsedTo - parsedFrom;
        nextChunk.setBegin(parsedTo + 1);
        nextChunk.setEnd(nextChunk.getBegin() + inclusiveRangeWindow);
        int totalItems = Integer.parseInt(strContentRange[3]);
        if (nextChunk.getBegin() >= totalItems) {
            nextChunk.setBegin(parsedFrom);
        }
        if (nextChunk.getEnd() >= totalItems) {
            nextChunk.setEnd(totalItems-1);
        }
        log.debug("Next chunk: {} - {}", nextChunk.getBegin(), nextChunk.getEnd());
        return nextChunk;
    }

    private void prepareRequest(Taxii21Association taxii21Association, Taxii21GetParameters parameters) {
        UriComponentsBuilder fetchUrl;
        // Establish a fetch URL so that query parameters can be added
        fetchUrl = UriComponentsBuilder.fromUri(taxii21Association.getServer().getCollectionObjectsUrl(
            ((Taxii21Association) parameters.getAssociation()).getApiRoot().getEndpoint(),
            ((Taxii21Association) parameters.getAssociation()).getCollection().getCollectionObject().getId()));

        if (parameters.getQueryString() != null)
            fetchUrl = fetchUrl.query(parameters.getQueryString());

        log.debug("Query string: {}", parameters.getQueryString());
        parameters.setFetchUrl(fetchUrl.build().toUri());
    }

    public CountResult processTaxii11Content(Object responseObject, Taxii11Association association) {
        if (!(responseObject instanceof PollResponse)) {
            throw new IllegalArgumentException("TAXII 1.1 can only parse a Poll Response object");
        }

        PollResponse response = (PollResponse) responseObject;

        CountResult results = new CountResult();

        List<Stix1ContentWrapper> content = new ArrayList<>();
        if (response.getContentBlocks().size() > 0) {
            log.info("Processing {} content blocks", response.getContentBlocks().size());
            for (ContentBlock contentBlock : response.getContentBlocks()) {
                if (contentBlock.getContent() != null) {
                    for (Object contentObject : contentBlock.getContent().getContent()) {
                        if (contentObject instanceof String) {
                            if (((String) contentObject).trim().isEmpty()) {
                                log.trace("Discarding empty content");
                                continue;
                            }
                        }
                        Stix1ContentWrapper contentWrapper = new Stix1ContentWrapper(contentObject, association);
                        contentWrapper.setLastRetrieved(Instant.now());
                        content.add(contentWrapper);
                    }
                }
            }
        } else {
            log.info("Response contained no content");
        }

        if (content.size() > 0) {
            BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "content");
            bulkOperations.insert(content);
            @SuppressWarnings("UnusedAssignment") BulkWriteResult writeResult = null; // Note: Removing null initializer will cause exceptions

            try {
                writeResult = bulkOperations.execute();
            } catch (DuplicateKeyException dke)  {
                log.warn("dke="+dke.getCause());
                MongoBulkWriteException mbwe = null;
                mbwe = (MongoBulkWriteException) dke.getCause();

                if (mbwe.getWriteErrors().stream().anyMatch(error -> error.getCode() != 11000)) {
                    // Re-throw any errors that were not 11000 (duplicate key exceptions)
                    throw new InternalServerErrorException(mbwe.getMessage());
                }
                if (log.isTraceEnabled()) {
                    mbwe.getWriteErrors().forEach(error -> log.trace(error.getMessage()));
                }
                writeResult = mbwe.getWriteResult();
                log.warn("Duplicate content encountered when parsing a STIX 1.X package. Will ignore.");
            }

            results.setContentCount(content.size());
            results.setContentSaved(writeResult.getInsertedCount());
            results.setContentDuplicate(content.size() - writeResult.getInsertedCount());
        }

        return results;
    }

    public CountResult processTaxii21Content(Object response, Taxii21Association association) {
        JsonElement envelope = (JsonElement) response;

        if (envelope.isJsonNull() || !envelope.getAsJsonObject().has("objects")) {
            return new CountResult();
        }

        CountResult countResult = new CountResult();

        // For each object in the envelope, create a content wrapper
        List<Stix2ContentWrapper> content = new ArrayList<>();

        envelope.getAsJsonObject().get("objects").getAsJsonArray().forEach(object -> {
            Stix2ContentWrapper contentWrapper = new Stix2ContentWrapper(object.toString(), association);
            contentWrapper.setLastRetrieved(Instant.now());
            content.add(contentWrapper);
        });

        if (content.size() > 0) {
            // Save to the database with a bulk operation, and avoid duplication by catching error code 11000
            BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "content");
            bulkOperations.insert(content);
            @SuppressWarnings("UnusedAssignment") BulkWriteResult writeResult = null;
            try {
                writeResult = bulkOperations.execute();
            } catch (DuplicateKeyException dke)  {
                log.warn("dke="+dke.getCause());
                MongoBulkWriteException mbwe = null;
                mbwe = (MongoBulkWriteException) dke.getCause();

                if (mbwe.getWriteErrors().stream().anyMatch(error -> error.getCode() != 11000)) {
                    // Re-throw any errors that were not 11000 (duplicate key exceptions)
                    throw new InternalServerErrorException(mbwe.getMessage());
                }
                if (log.isTraceEnabled()) {
                    mbwe.getWriteErrors().forEach(error -> log.trace(error.getMessage()));
                }
                writeResult = mbwe.getWriteResult();
                log.warn("Duplicate content encountered when parsing a STIX 2.X envelope. Will ignore.");
            }
            // Respond with feedback to the front-end regarding how much content was processed, saved, and if there were any duplicates
            countResult.setContentCount(content.size());
            countResult.setContentSaved(writeResult.getInsertedCount());
            countResult.setContentDuplicate(content.size() - writeResult.getInsertedCount());
        }
        return countResult;
    }






    // Dependencies
    public TaxiiService getTaxiiService() {
        return taxiiService;
    }

    public void setTaxiiService(TaxiiService taxiiService) {
        this.taxiiService = taxiiService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
