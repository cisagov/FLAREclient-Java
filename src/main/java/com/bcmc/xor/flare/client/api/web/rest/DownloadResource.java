package com.bcmc.xor.flare.client.api.web.rest;


import com.bcmc.xor.flare.client.api.domain.async.RecurringFetch;
import com.bcmc.xor.flare.client.api.domain.parameters.ApiParameters;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.scheduled.RecurringFetchService;
import com.bcmc.xor.flare.client.api.service.scheduled.async.AsyncFetchRequestService;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.bcmc.xor.flare.client.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@RestController
@RequestMapping("/api/servers/{serverLabel}/collections/{collectionId}/download")
class DownloadResource {

    private static final Logger log = LoggerFactory.getLogger(DownloadResource.class);

    private final ServerService serverService;
    private final CollectionService collectionService;
    private final AsyncFetchRequestService asyncFetchRequestService;
    private final RecurringFetchService recurringFetchService;

    public DownloadResource(ServerService serverService, CollectionService collectionService, AsyncFetchRequestService asyncFetchRequestService, RecurringFetchService recurringFetchService) {
        this.serverService = serverService;
        this.collectionService = collectionService;
        this.asyncFetchRequestService = asyncFetchRequestService;
        this.recurringFetchService = recurringFetchService;
    }

    @PostMapping
    public <T extends ApiParameters> ResponseEntity<String> fetchContent(@PathVariable String serverLabel, @PathVariable String collectionId, @RequestBody T fetchParams) {
        log.debug("Received fetch content request");
        log.debug("Server Label: {}", serverLabel);
        log.debug("Collection ID: {}", collectionId);
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);

        if (fetchParams.getServerLabel() == null || fetchParams.getServerLabel().isEmpty()) {
            fetchParams.setServerLabel(association.getServer().getLabel());
        }
        fetchParams.setAssociation(association);

        if (fetchParams.getRepeat()) {
            recurringFetchService.startRecurringFetch(new RecurringFetch(fetchParams));
        } else {
            asyncFetchRequestService.startAsyncFetch(association, fetchParams);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Started async fetch", null)).body("Started async fetch");
    }
}

