package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.async.FetchChunk;
import com.bcmc.xor.flare.client.api.domain.audit.Event;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.content.CountResult;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii20GetParameters;
import com.bcmc.xor.flare.client.api.repository.CollectionRepository;
import com.bcmc.xor.flare.client.api.repository.ServerRepository;
import com.bcmc.xor.flare.client.error.RequestException;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20RestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class DownloadServiceTaxii20Test {

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private TaxiiService taxiiService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private EventService eventService;

    @MockBean
    private Taxii20RestTemplate taxii20RestTemplate;

    @Autowired
    ServerRepository serverRepository;

    @Autowired
    CollectionRepository collectionRepository;

    @Autowired
    UserService userService;

    @MockBean
    SecurityContext securityContext;

    @Before
    public void init() {
        taxiiService.setTaxii20RestTemplate(taxii20RestTemplate);
        downloadService.setTaxiiService(taxiiService);
        downloadService.setEventService(eventService);
        downloadService.setMongoTemplate(mongoTemplate);
        MockitoAnnotations.initMocks(this);

        serverRepository.save(TestData.taxii20Server);
        collectionRepository.save(TestData.taxii20Collection);
        TestData.setLoggedInUser(securityContext, userService);

        mongoTemplate.remove(new Query(), "content");
    }

    @Test
    public void fetchContentSuccess() {
        // Establish Spy for parameters
        Taxii20GetParameters spyParameters = Mockito.spy(TestData.getParameters);

        // Respond with a bundle response for mocked external request
        when(taxiiService.getTaxii20RestTemplate().executeGet(any(), any())).thenReturn(TestData.taxii20GetResponse);

        // Verify PollResponse is returned
        CountResult countResult = downloadService.fetchContent(TestData.taxii20Association, TestData.getParameters,
            new FetchChunk<>(0, 0), new CountResult());

        assertEquals(4, countResult.getContentCount());
    }

    @Test
    public void fetchContentSuccessWithRange() {
        // Establish Spy for parameters
        Taxii20GetParameters spyParameters = Mockito.spy(TestData.getParameters);

        // Mock response headers from server, returning pages of items
        HttpHeaders responseHeaders1 = new HttpHeaders();
        HttpHeaders responseHeaders2 = new HttpHeaders();
        HttpHeaders responseHeaders3 = new HttpHeaders();
        responseHeaders1.set("Content-Range", "items 0-3/12");
        responseHeaders2.set("Content-Range", "items 4-7/12");
        responseHeaders3.set("Content-Range", "items 8-11/12");

        // Respond with sequential bundle responses, with headers to denote pages
        when(taxiiService.getTaxii20RestTemplate().executeGet(any(), any()))
            .thenReturn(
                ResponseEntity.status(206).headers(responseHeaders1).body(TestData.rawStix20),
                ResponseEntity.status(206).headers(responseHeaders2).body(TestData.rawStix20),
                ResponseEntity.status(206).headers(responseHeaders3).body(TestData.rawStix20));


        // Verify PollResponse is returned
        CountResult countResult = downloadService.fetchContent(TestData.taxii20Association, TestData.getParameters,
            new FetchChunk<>(0, 0), new CountResult());

        assertEquals(12, countResult.getContentCount());
    }

    @Test(expected = RequestException.class)
    public void fetchContentFailureRequestException() {
        // Respond with a PollResponse for mocked external request
        RequestException requestException = new RequestException("test", "test", "test");
        when(taxiiService.getTaxii20RestTemplate().executeGet(any(), any()))
            .thenThrow(requestException);

        // Mock event service creation
        when(eventService.createEvent(eq(EventType.ASYNC_FETCH_ERROR), eq(requestException.getMessage()), eq(TestData.taxii20Association)))
            .thenReturn(Event.from(EventType.ASYNC_FETCH_ERROR, requestException.getMessage(), TestData.taxii20Server.getLabel(), TestData.taxii20Collection.getDisplayName()));

        // Make method call
        downloadService.fetchContent(TestData.taxii20Association, TestData.getParameters, new FetchChunk<>(0, 0), new CountResult());

        // Verify that an event was created for the failure
        verify(Mockito.spy(eventService)).createEvent(eq(EventType.ASYNC_FETCH_ERROR), eq(requestException.getMessage()), eq(TestData.taxii11Association));

        // Expect exception to be thrown
    }

    @Test(expected = HttpClientErrorException.class)
    public void fetchContentResponseNull() {
        // Respond with an empty response body
        when(taxiiService.getTaxii20RestTemplate().executeGet(any(), any())).thenReturn(ResponseEntity.ok(""));

        // Make method call
        downloadService.fetchContent(TestData.taxii20Association, TestData.getParameters, new FetchChunk<>(0, 0), new CountResult());

        // Verify that an event was created for the failure
        verify(Mockito.spy(eventService)).createEvent(eq(EventType.ASYNC_FETCH_ERROR), any(), eq(TestData.taxii20Association));

        // Expect exception to be thrown
    }

    @Test
    public void processTaxii20Content() {

        // Call method under test
        CountResult result = downloadService.processTaxii20Content(TestData.jsonStix20, TestData.taxii20Association);

        assertEquals(4, result.getContentCount());
        assertEquals(4, result.getContentSaved());
    }

    @Test
    public void processTaxii20ContentDuplicate() {

        // Call method under test
        downloadService.processTaxii20Content(TestData.jsonStix20, TestData.taxii20Association);
        CountResult secondResult = downloadService.processTaxii20Content(TestData.jsonStix20, TestData.taxii20Association);

        assertEquals(4, secondResult.getContentCount());
        assertEquals(4, secondResult.getContentDuplicate());
    }
}
