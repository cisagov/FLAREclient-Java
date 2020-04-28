package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.domain.collection.TaxiiCollection;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.repository.ContentRepository;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.dto.CollectionsDTO;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/servers/{serverLabel}/collections")
class CollectionResource {

    private static final Logger log = LoggerFactory.getLogger(CollectionResource.class);
    private final ServerService serverService;
    private final CollectionService collectionService;
    private final ContentRepository contentRepository;

    public CollectionResource(ServerService serverService, CollectionService collectionService, ContentRepository contentRepository) {
        this.serverService = serverService;
        this.collectionService = collectionService;
        this.contentRepository = contentRepository;
    }

    @GetMapping
    public ResponseEntity<CollectionsDTO> getAllCollections(@PathVariable String serverLabel) {
        Optional<? extends TaxiiServer> server = serverService.findOneByLabel(serverLabel);
        if (server.isPresent()) {
            if (server.get().getCollections() != null) {
                for (TaxiiCollection taxiiCollection : server.get().getCollections()) {
                    TaxiiAssociation association = TaxiiAssociation.from(serverLabel, taxiiCollection.getId(), serverService, collectionService);
                    taxiiCollection.setContentVolume(contentRepository.countByAssociation(association));
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(new CollectionsDTO(server.get().getCollections()));
        } else {
            log.info("Server {} not found", serverLabel);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{collectionId}/details")
    public ResponseEntity<TaxiiCollection> getCollectionDetails(@PathVariable String serverLabel, @PathVariable String collectionId) {
        Optional<? extends TaxiiServer> server = serverService.findOneByLabel(serverLabel);
        if (server.isPresent()) {
            CollectionsDTO collections = new CollectionsDTO(server.get().getCollections());
            if (collections.getAllIds().contains(collectionId)) {
                TaxiiCollection taxiiCollection = collections.getById().get(collectionId);
                TaxiiAssociation association = TaxiiAssociation.from(serverLabel, taxiiCollection.getId(), serverService, collectionService);
                taxiiCollection.setContentVolume(contentRepository.countByAssociation(association));
                return ResponseEntity.status(HttpStatus.OK).body(taxiiCollection);
            } else {
                log.info("Server {} found but no collection with ID {} found", serverLabel, collectionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            log.info("Server {} not found", serverLabel);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<TaxiiCollection> getCollection(@PathVariable String serverLabel, @PathVariable String collectionId) {
        Optional<? extends TaxiiServer> server = serverService.findOneByLabel(serverLabel);
        if (server.isPresent()) {
            CollectionsDTO collections = new CollectionsDTO(server.get().getCollections());
            if (collections.getAllIds().contains(collectionId)) {
                TaxiiCollection taxiiCollection = collections.getById().get(collectionId);
                TaxiiAssociation association = TaxiiAssociation.from(serverLabel, taxiiCollection.getId(), serverService, collectionService);
                taxiiCollection.setContentVolume(contentRepository.countByAssociation(association));
                return ResponseEntity.status(HttpStatus.OK).body(taxiiCollection);
            } else {
                log.info("Server {} found but no collection with ID {} found", serverLabel, collectionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            log.info("Server {} not found", serverLabel);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}