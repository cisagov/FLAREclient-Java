package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.domain.parameters.UploadedFile;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.UploadService;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/servers/{serverLabel}/collections/{collectionId}/upload")
class UploadResource {

    private static final Logger log = LoggerFactory.getLogger(UploadResource.class);


    private final ServerService serverService;
    private final CollectionService collectionService;

    private final UploadService uploadService;

    public UploadResource(ServerService serverService, CollectionService collectionService, UploadService uploadService) {
        this.serverService = serverService;
        this.uploadService = uploadService;
        this.collectionService = collectionService;
    }

    @PostMapping
    public ResponseEntity<String> publish(@RequestBody Map<String, UploadedFile> fileMap, @PathVariable String serverLabel, @PathVariable String collectionId) {
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);
        return uploadService.publish(association, fileMap);
    }
}
