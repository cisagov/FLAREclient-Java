package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.domain.content.AbstractContentWrapper;
import com.bcmc.xor.flare.client.api.repository.ContentRepository;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.bcmc.xor.flare.client.util.PaginationUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/servers/{serverLabel}/collections/{collectionId}/view")
class ContentResource {

    private final ServerService serverService;
    private final CollectionService collectionService;
    private final ContentRepository contentRepository;

    public ContentResource(ServerService serverService, CollectionService collectionService, ContentRepository contentRepository) {
        this.serverService = serverService;
        this.collectionService = collectionService;
        this.contentRepository = contentRepository;
    }

    @GetMapping
    public ResponseEntity<List<AbstractContentWrapper>> getContentWrapper(@PathVariable String serverLabel, @PathVariable String collectionId, Pageable pageable) {
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);
        Page<AbstractContentWrapper> page = contentRepository.findAllByAssociation(pageable, association);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<AbstractContentWrapper> getContentWrapperById(@PathVariable String serverLabel, @PathVariable String collectionId, @PathVariable String contentId) {
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);
        Optional<AbstractContentWrapper> content = contentRepository.findByIdAndAssociation(contentId, association);
        return ResponseUtil.wrapOrNotFound(content);
    }
}
