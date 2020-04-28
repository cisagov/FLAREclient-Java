package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.TaxiiService;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20Association;
import com.bcmc.xor.flare.client.util.HeaderUtil;
import org.slf4j.Logger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xor.bcmc.taxii2.JsonHandler;

/**
 * REST controller for managing Event.
 */
@RestController
@RequestMapping("/api/servers")
class TaxiiManifestResource {

    private static final Logger log = LoggerFactory.getLogger(TaxiiManifestResource.class);

    private final TaxiiService taxiiService;
    private final ServerService serverService;
    private final CollectionService collectionService;

    public TaxiiManifestResource(TaxiiService taxiiService, ServerService serverService,
            CollectionService collectionService) {
        this.taxiiService = taxiiService;
        this.serverService = serverService;
        this.collectionService = collectionService;
    }

    @GetMapping("/{serverLabel}/collections/{collectionId}/manifest")
    public ResponseEntity<Map<String, String>> fetchManifestResource(@PathVariable String serverLabel,
            @PathVariable String collectionId, HttpServletRequest request) {

                
        log.debug("Received request: {}", request.getQueryString());
        log.debug("Received fetch Manifest request");
        log.debug("Server Label: {}", serverLabel);
        log.debug("Collection ID: {}", collectionId);

        Taxii20Association association = (Taxii20Association) Taxii20Association.from(serverLabel,
                collectionId, serverService, collectionService);
        final String manifestEndpoint = "collections/"
                + association.getCollection().getCollectionObject().getId() + "/manifest/";

        try {
            String manifest = taxiiService.getTaxii20RestTemplate().getManifest(
                association.getServer(),
                new URI(association.getApiRoot().getUrl().toString() + manifestEndpoint));

            if (request.getQueryString() != null && request.getQueryString().length() > 0) {
                manifest = taxiiService.getTaxii20RestTemplate().getManifest(
                    association.getServer(),
                    new URI(association.getApiRoot().getUrl().toString() + manifestEndpoint + "?" + request.getQueryString()));
            } 
            log.debug("Returning ManifestResource {}", JsonHandler.getInstance().toJson(manifest));
            Map<String, String> response = new HashMap<>();
            response.put(association.getCollection().getId(), manifest);
            return ResponseEntity.status(HttpStatus.OK).headers(HeaderUtil.createAlert("Fetching Manifest Resource.", null)).body(response);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(HeaderUtil.createFailureAlert("Error Fetching Manifest Resource:", e.toString())).build();

        }
    }


}
