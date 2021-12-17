package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.audit.Event;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.dto.EventDTO;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventResourceTest {

    @Autowired
    private EventResource eventResource;

    @MockBean
    private ServerService serverService;

    @MockBean
    private CollectionService collectionService;

    @MockBean
    private EventService eventService;

    private static Event event;
    private static EventDTO eventDTO;
    private static List<EventDTO> eventDTOList;
    private static Page<EventDTO> eventsPage;

    private static Pageable page = PageRequest.of(0, 10, Sort.by("id"));

    @BeforeClass
    public static void setUp() throws Exception {
        event = Event.from(EventType.SERVER_ADDED, "Server added", TestData.taxii11Server.getLabel(), TestData.taxii11Collection.getDisplayName());
        eventDTO = new EventDTO(event);
        eventDTOList = new ArrayList<>();
        eventDTOList.add(eventDTO);
        eventsPage = new PageImpl<>(eventDTOList, page, 1);
    }

    @Before
    public void init() {
        eventResource = new EventResource(serverService, collectionService, eventService);
        MockitoAnnotations.initMocks(this);

        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        when(collectionService.findOneById(TestData.taxii11Collection.getId())).thenReturn(Optional.of(TestData.taxii11Collection));

    }

    @Test
    public void testGetAllEvents() {
        when(eventService.getAllEvents(page)).thenReturn(eventsPage);
        ResponseEntity<List<EventDTO>> response = eventResource.getAllEvents(page);
        assertEquals(eventDTO.getType(), response.getBody().get(0).getType());
        assertEquals(eventDTO.getServer(), response.getBody().get(0).getServer());
        assertEquals(eventDTO.getTaxiiCollection(), response.getBody().get(0).getTaxiiCollection());
        assertEquals(eventDTO.getDetails(), response.getBody().get(0).getDetails());
        assertEquals(eventDTO.getTime(), response.getBody().get(0).getTime());
    }

    @Test
    public void testGetEventById() {
        when(eventService.getEventById(eventDTO.getId())).thenReturn(Optional.of(event));

        ResponseEntity<EventDTO> response = eventResource.getEvent(event.getId());

        assertEquals(TestData.taxii11Server.getLabel(), response.getBody().getServer());
        assertEquals(TestData.taxii11Collection.getDisplayName(), response.getBody().getTaxiiCollection());
        assertEquals(event.getDetails(), response.getBody().getDetails());
    }

//    @Test
//    public void testGetAllEventsByCollection() {
//        when(eventService.getEventsByCollection(page, TestData.taxii11Collection.getDisplayName(), TestData.taxii11Association.getServer().getLabel())).thenReturn(eventsPage);
//
//        ResponseEntity<List<EventDTO>> response = eventResource.getAllEventsByCollection(
//            TestData.taxii11Server.getLabel(),
//            TestData.taxii11Collection.getId(),
//            page);
//
//        assertEquals(eventDTO.getType(), response.getBody().get(0).getType());
//        assertEquals(eventDTO.getServer(), response.getBody().get(0).getServer());
//        assertEquals(eventDTO.getTaxiiCollection(), response.getBody().get(0).getTaxiiCollection());
//        assertEquals(eventDTO.getDetails(), response.getBody().get(0).getDetails());
//        assertEquals(eventDTO.getTime(), response.getBody().get(0).getTime());
//    }
}
