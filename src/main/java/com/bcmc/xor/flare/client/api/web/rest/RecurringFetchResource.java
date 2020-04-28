package com.bcmc.xor.flare.client.api.web.rest;


import com.bcmc.xor.flare.client.api.domain.async.RecurringFetch;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.scheduled.RecurringFetchService;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/servers/{serverLabel}/collections/{collectionId}/download")
class RecurringFetchResource {

    private static final Logger log = LoggerFactory.getLogger(RecurringFetchResource.class);

    private final ServerService serverService;
    private final CollectionService collectionService;
    private final RecurringFetchService recurringFetchService;

    public RecurringFetchResource(ServerService serverService, CollectionService collectionService, RecurringFetchService recurringFetchService) {
        this.serverService = serverService;
        this.collectionService = collectionService;
        this.recurringFetchService = recurringFetchService;
    }

    @GetMapping("/recurring")
    public ResponseEntity<RecurringFetch> getRecurringFetch(@PathVariable String serverLabel, @PathVariable String collectionId) {
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);
        log.debug("Received request to get recurring fetch for server {} and collection {}", serverLabel, association.getCollection().getDisplayName());
        return recurringFetchService.findByAssociation(association).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok().build());
    }

    @DeleteMapping("/recurring")
    public ResponseEntity<String> deleteRecurringFetch(@PathVariable String serverLabel, @PathVariable String collectionId) {
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);
        log.debug("Received request to delete recurring fetch for server {} and collection {}", serverLabel, association.getCollection().getDisplayName());
        return recurringFetchService.deleteRecurringFetch(association);
    }
}

