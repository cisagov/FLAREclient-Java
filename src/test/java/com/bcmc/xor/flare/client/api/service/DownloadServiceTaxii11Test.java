package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.audit.Event;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii11Collection;
import com.bcmc.xor.flare.client.api.domain.content.CountResult;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.domain.server.Taxii11Server;
import com.bcmc.xor.flare.client.api.repository.CollectionRepository;
import com.bcmc.xor.flare.client.api.repository.ServerRepository;
import com.bcmc.xor.flare.client.error.InsufficientInformationException;
import com.bcmc.xor.flare.client.error.RequestException;
import com.bcmc.xor.flare.client.error.StatusMessageResponseException;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11RestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.taxii.messages.xml11.CollectionRecordType;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class DownloadServiceTaxii11Test {

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private TaxiiService taxiiService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private EventService eventService;

    @MockBean
    private Taxii11RestTemplate taxii11RestTemplate;

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
        taxiiService.setTaxii11RestTemplate(taxii11RestTemplate);
        downloadService.setTaxiiService(taxiiService);
        downloadService.setEventService(eventService);
        downloadService.setMongoTemplate(mongoTemplate);
        MockitoAnnotations.initMocks(this);

        serverRepository.save(TestData.taxii21Server);
        collectionRepository.save(TestData.taxii21Collection);
        TestData.setLoggedInUser(securityContext, userService);

        mongoTemplate.remove(new Query(), "content");
    }

    @Test
    public void getFetchUrl() {
        assertEquals(TestData.inboxServiceBindingsType.getAddress(), downloadService.getFetchUrl(TestData.taxii11Association).toString());
    }

    @Test(expected = InsufficientInformationException.class)
    public void getFetchUrlInsufficient() {
        Taxii11Association association = new Taxii11Association(
            new Taxii11Server(),
            new Taxii11Collection(new CollectionRecordType(), "none"),
            new User());

        // Expect exception
        downloadService.getFetchUrl(association);
    }

    @Test
    public void fetchContentSuccess() {
        // Establish Spy for parameters
        Taxii11PollParameters spyParameters = Mockito.spy(TestData.pollParameters);

        // Respond with a PollResponse for mocked external request
        when(taxiiService.getTaxii11RestTemplate().submitPoll(any(), any(), any())).thenReturn(TestData.pollResponse);

        // Verify PollResponse is returned
        assertEquals(TestData.pollResponse, downloadService.fetchContent(TestData.taxii11Association, spyParameters));

        // Verify that request is being built with supplied parameters
        verify(spyParameters).buildPollRequestForCollection(TestData.taxii11Collection.getName());
    }

    @Test(expected = RequestException.class)
    public void fetchContentFailureRequestException() {
        // Respond with a PollResponse for mocked external request
        RequestException requestException = new RequestException("test", "test", "test");
        when(taxiiService.getTaxii11RestTemplate().submitPoll(any(), any(), any()))
            .thenThrow(requestException);

        // Mock event service creation
        when(eventService.createEvent(eq(EventType.ASYNC_FETCH_ERROR), eq(requestException.getMessage()), eq(TestData.taxii11Association)))
            .thenReturn(Event.from(EventType.ASYNC_FETCH_ERROR, requestException.getMessage(), TestData.taxii11Server.getLabel(), TestData.taxii11Collection.getDisplayName()));

        // Make method call
        downloadService.fetchContent(TestData.taxii11Association, TestData.pollParameters);
        // Expect exception to be thrown
    }

    @Test(expected = StatusMessageResponseException.class)
    public void fetchContentFailureStatusMessageResponseException() {
        // Respond with a PollResponse for mocked external request
        StatusMessageResponseException exception = new StatusMessageResponseException(TestData.failureStatusMessage);
        when(taxiiService.getTaxii11RestTemplate().submitPoll(any(), any(), any()))
            .thenThrow(exception);

        // Mock event service creation
        when(eventService.createEvent(eq(EventType.ASYNC_FETCH_ERROR), eq(exception.getMessage()), eq(TestData.taxii11Association)))
            .thenReturn(Event.from(EventType.ASYNC_FETCH_ERROR, exception.getMessage(), TestData.taxii11Server.getLabel(), TestData.taxii11Collection.getDisplayName()));

        // Make method call
        downloadService.fetchContent(TestData.taxii11Association, TestData.pollParameters);
        // Expect exception to be thrown
    }

    @Test
    public void processTaxii11Content() {

        // Call method under test
        CountResult result = downloadService.processTaxii11Content(TestData.pollResponse, TestData.taxii11Association);

        assertEquals(1, result.getContentCount());
        assertEquals(1, result.getContentSaved());
    }
//
//    @Test
//    public void processTaxii11ContentDuplicate() {
//
//        // Call method under test
//        downloadService.processTaxii11Content(TestData.pollResponse, TestData.taxii11Association);
//        CountResult secondResult = downloadService.processTaxii11Content(TestData.pollResponse, TestData.taxii11Association);
//
//        assertEquals(1, secondResult.getContentCount());
//        assertEquals(1, secondResult.getContentDuplicate());
//    }

}
