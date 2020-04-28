package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.dto.EventDTO;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.bcmc.xor.flare.client.util.PaginationUtil;
import com.codahale.metrics.annotation.Timed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * REST controller for managing Event.
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/api")
public class EventResource {

    private static final Logger log = LoggerFactory.getLogger(EventResource.class);

    private final ServerService serverService;
    private final CollectionService collectionService;
    private final EventService eventService;

    public EventResource(ServerService serverService, CollectionService collectionService, EventService eventService) {
        this.serverService = serverService;
        this.collectionService = collectionService;
        this.eventService = eventService;
    }

    /**
     * GET /events : get all events.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all events
     */
    @GetMapping("/events")
    @Timed
    public ResponseEntity<List<EventDTO>> getAllEvents(Pageable pageable) {
        final Page<EventDTO> page = eventService.getAllEvents(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/events");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET /events/:id : get the "event".
     *
     * @param id the id of the event to find
     * @return the ResponseEntity with status 200 (OK) and with body the event, or with status 404 (Not Found)
     */
    @GetMapping("/events/{id}")
    @Timed
    public ResponseEntity<EventDTO> getEvent(@PathVariable String id) {
        log.debug("REST request to get Event : {}", id);
        return ResponseUtil.wrapOrNotFound(
            eventService.getEventById(id)
                .map(EventDTO::new));
    }

    @GetMapping("/servers/{serverLabel}/collections/{collectionId}/activities")
    @Timed
    public ResponseEntity<List<EventDTO>> getAllEventsByCollection(@PathVariable String serverLabel, @PathVariable String collectionId, Pageable pageable) {
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);
        final Page<EventDTO> page = eventService.getEventsByCollectionAndServer(pageable, association.getCollection().getDisplayName(), serverLabel);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/servers/{serverLabel}/collections/{collectionId}/activities");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
