package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.TaxiiService;
import com.bcmc.xor.flare.client.error.ManifestNotSupportedException;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xor.bcmc.taxii2.JsonHandler;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping(value = "/{serverLabel}/collections/{collectionId}/manifest")
    public ResponseEntity<Map<String, String>> fetchManifestResource(@PathVariable String serverLabel,
                                                                     @PathVariable String collectionId,
                                                                     @Nullable @RequestParam Map<String, String> filters) {

        log.debug("Received fetch Manifest request");
        log.debug("Server Label: {}", serverLabel);
        log.debug("Collection ID: {}", collectionId);
        StringBuilder queryFilters = new StringBuilder();
        if (filters != null && !filters.isEmpty()) {
            log.debug("Received query filters: {}", filters.toString());

            for (Map.Entry<String, String> entry : filters.entrySet()) {
                 queryFilters.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            queryFilters.deleteCharAt(queryFilters.length()-1);
            log.debug("queryFilters={}", queryFilters.toString());
        }

        Optional<TaxiiServer> taxiiServer = serverService.getServerRepository().findOneByLabelIgnoreCase(serverLabel);
        if (taxiiServer.isPresent()) {
            if (taxiiServer.get().getVersion().equals(Constants.TaxiiVersion.TAXII11)) {
                throw new ManifestNotSupportedException();
            }
        }

        Taxii21Association association = (Taxii21Association) Taxii21Association.from(serverLabel,
                collectionId, serverService, collectionService);
        final String manifestEndpoint = "collections/"
                + association.getCollection().getCollectionObject().getId() + "/manifest/";

        Map<String, String> response = new HashMap<>();
        try {
            String manifest = taxiiService.getTaxii21RestTemplate().getManifest(
                association.getServer(),
                new URI(association.getApiRoot().getUrl().toString() + manifestEndpoint));

            if (StringUtils.isNotBlank(queryFilters.toString())) {
                manifest = taxiiService.getTaxii21RestTemplate().getManifest(
                        association.getServer(),
                        new URI(association.getApiRoot().getUrl().toString() + manifestEndpoint + "?" + queryFilters));
            }

            log.debug("Returning ManifestResource {}", JsonHandler.toJson(manifest));
            response.put(association.getCollection().getId(), manifest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
            response.put("Failed to find endpoint.  Error message:", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}
