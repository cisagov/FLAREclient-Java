package gov.dhs.cisa.ctm.flare.client.api.web.rest;

import gov.dhs.cisa.ctm.flare.client.api.domain.parameters.UploadedFile;
import gov.dhs.cisa.ctm.flare.client.api.service.CollectionService;
import gov.dhs.cisa.ctm.flare.client.api.service.ServerService;
import gov.dhs.cisa.ctm.flare.client.api.service.UploadService;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<String> publish(@RequestBody Map<String,UploadedFile> fileMap, @PathVariable String serverLabel, @PathVariable String collectionId) {
        log.debug("REST request to publish file(s) with serverLabel {} and collectionId {}", serverLabel, collectionId);
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);
        String feedback = uploadService.publish(association, fileMap);
        if (StringUtils.containsIgnoreCase(feedback,"FAIL")) {
            return new ResponseEntity<>(feedback, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }
}
