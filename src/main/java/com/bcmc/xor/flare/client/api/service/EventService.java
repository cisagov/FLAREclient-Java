package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.domain.audit.Event;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.repository.EventRepository;
import com.bcmc.xor.flare.client.api.service.dto.EventDTO;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
 
/**
 * A service for managing events ({@link Event})
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(EventType eventType, String details) {
        if (eventType == null || details == null || details.isEmpty()) {
            throw new IllegalArgumentException("An event must have a type and details");
        }
        log.debug("Event Created: {} - {}", eventType.toString(), details);
        return eventRepository.save(Event.from(eventType, details, null, null));
    }

    public Event createEvent(EventType eventType, String details, String serverLabel) {
        if (eventType == null || details == null || details.isEmpty()) {
            throw new IllegalArgumentException("An event must have a type and details");
        }
        log.debug("Event Created: ['{}'] {} - {}", serverLabel, eventType.toString(), details);
        return eventRepository.save(Event.from(eventType, details, serverLabel, null));
    }

    public Event createEvent(EventType eventType, String details, TaxiiAssociation association) {
        if (eventType == null || details == null || details.isEmpty()) {
            throw new IllegalArgumentException("An event must have a type and details");
        }
        log.debug("Event Created: ['{}'/'{}'] {} - {}",
            association != null ? association.getServer().getLabel() : "null",
            association != null ? association.getCollection().getDisplayName() : "null",
            eventType.toString(), details);
        return eventRepository.save(Event.from(eventType, details,
            association != null ? association.getServer().getLabel() : null,
            association != null ? association.getCollection().getDisplayName() : null));
    }

    public Page<EventDTO> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable).map(EventDTO::new);
    }

    public Optional<Event> getEventById(String id) {
        return eventRepository.findOneById(id);
    }

    public List<Event> getEventsByServer(String server) {
        return eventRepository.findAllByServer(server);
    }

    public Page<EventDTO> getEventsByCollectionAndServer(Pageable pageable, String collection, String server){
        log.debug("Collection: {}, Server: {}", collection, server);
        return eventRepository.findByTaxiiCollectionAndServer(pageable, collection, server).map(EventDTO::new);
    }

    public void deleteAll(Iterable<Event> serverEvents) {
        eventRepository.deleteAll(serverEvents);
    }

    // Dependencies
    public EventRepository getEventRepository() {
        return eventRepository;
    }

    public void setEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
}
