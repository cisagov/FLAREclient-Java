package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.audit.Event;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.repository.EventRepository;
import com.bcmc.xor.flare.client.api.service.dto.EventDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Class for EventService
 *
 * @See EventService
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class EventServiceTest {

    //Repositories
    @Autowired
    EventRepository eventRepository;

    //Services
    @Autowired
    EventService eventService;

    @Autowired
    CacheManager cacheManager;

    @Before
    public void init() {
        eventService.setEventRepository(eventRepository);
        eventRepository.deleteAll();
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Create Event and assert the event is saved to the repository
     */
    @Test
    public void testCreateEvent() {
        Event event = eventService.createEvent(EventType.SERVER_ADDED, TestData.event.getDetails(), TestData.taxii11Association);

        Optional<Event> maybeEvent = eventRepository.findOneById(event.getId());

        assertTrue(maybeEvent.isPresent());
        assertEquals(EventType.SERVER_ADDED, maybeEvent.get().getType());
        assertEquals(TestData.taxii11Association.getServer().getLabel(), maybeEvent.get().getServer());
        assertEquals(TestData.taxii11Association.getCollection().getDisplayName(), maybeEvent.get().getTaxiiCollection());
        assertEquals(TestData.event.getDetails(), maybeEvent.get().getDetails());
    }

    @Test
    public void testGetAllEvents() {
        Event event = eventService.createEvent(EventType.SERVER_ADDED, TestData.event.getDetails(), TestData.taxii11Association);
        Event event2 = eventService.createEvent(EventType.SERVER_DELETED, TestData.event.getDetails(), TestData.taxii11Association);

        Page<EventDTO> page = eventService.getAllEvents(PageRequest.of(0, 10, Sort.by("type")));

        assertEquals(event.getType(), page.getContent().get(0).getType());
        assertEquals(event.getServer(), page.getContent().get(0).getServer());
        assertEquals(event.getTaxiiCollection(), page.getContent().get(0).getTaxiiCollection());
        assertEquals(event.getDetails(), page.getContent().get(0).getDetails());
        assertEquals(event.getTime(), page.getContent().get(0).getTime());

        assertEquals(event2.getType(), page.getContent().get(1).getType());
        assertEquals(event2.getServer(), page.getContent().get(1).getServer());
        assertEquals(event2.getTaxiiCollection(), page.getContent().get(1).getTaxiiCollection());
        assertEquals(event2.getDetails(), page.getContent().get(1).getDetails());
        assertEquals(event2.getTime(), page.getContent().get(1).getTime());
    }

    /**
     * Retrieve Event Object by Id and assert it is the expected Event Object
     */
    @Test
    public void testGetEventById() {
        Event event = eventService.createEvent(EventType.SERVER_ADDED, TestData.event.getDetails(), TestData.taxii11Association);

        Optional<Event> maybeEvent = eventService.getEventById(event.getId());

        assertTrue(maybeEvent.isPresent());
        assertEquals(EventType.SERVER_ADDED, maybeEvent.get().getType());
        assertEquals(TestData.taxii11Association.getServer().getLabel(), maybeEvent.get().getServer());
        assertEquals(TestData.taxii11Association.getCollection().getDisplayName(), maybeEvent.get().getTaxiiCollection());
        assertEquals(TestData.event.getDetails(), maybeEvent.get().getDetails());
    }

//    @Test
//    public void testGetEventByCollection() {
//        Event event = eventService.createEvent(EventType.SERVER_ADDED, TestData.event.getDetails(), TestData.taxii11Association);
//        Event event2 = eventService.createEvent(EventType.SERVER_DELETED, TestData.event.getDetails(), TestData.taxii11Association);
//
//        Page<EventDTO> page = eventService.getEventsByCollection(PageRequest.of(0, 10, Sort.by("type")),
//            TestData.taxii11Association.getCollection().getDisplayName(), TestData.taxii11Association.getServer().getLabel());
//
//        assertEquals(event.getType(), page.getContent().get(0).getType());
//        assertEquals(event.getServer(), page.getContent().get(0).getServer());
//        assertEquals(event.getTaxiiCollection(), page.getContent().get(0).getTaxiiCollection());
//        assertEquals(event.getDetails(), page.getContent().get(0).getDetails());
//        assertEquals(event.getTime(), page.getContent().get(0).getTime());
//
//        assertEquals(event2.getType(), page.getContent().get(1).getType());
//        assertEquals(event2.getServer(), page.getContent().get(1).getServer());
//        assertEquals(event2.getTaxiiCollection(), page.getContent().get(1).getTaxiiCollection());
//        assertEquals(event2.getDetails(), page.getContent().get(1).getDetails());
//        assertEquals(event2.getTime(), page.getContent().get(1).getTime());
//    }
}
