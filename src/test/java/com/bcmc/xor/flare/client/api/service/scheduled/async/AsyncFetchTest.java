package com.bcmc.xor.flare.client.api.service.scheduled.async;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.async.AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.async.Taxii11AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.async.Taxii20AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.content.CountResult;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.repository.AsyncFetchRequestRepository;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.DownloadService;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20Association;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.taxii.messages.xml11.AnyMixedContentType;
import org.mitre.taxii.messages.xml11.ContentBlock;
import org.mitre.taxii.messages.xml11.ContentInstanceType;
import org.mitre.taxii.messages.xml11.PollResponse;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class AsyncFetchTest {

    private static final Logger log = LoggerFactory.getLogger(AsyncFetchTest.class);

    private AsyncFetchRequestService asyncFetchRequestService;

    @MockBean
    private AsyncFetchRequestRepository repository;

    @MockBean
    private CollectionService collectionService;

    @MockBean
    private DownloadService downloadService;

    @MockBean
    private UserService userService;

    @MockBean
    private EventService eventService;

    private static Taxii11AsyncFetch asyncFetch11;
    private static Taxii20AsyncFetch asyncFetch20;
    private static List<AsyncFetch> asyncFetches;



    @BeforeClass
    public static void init() throws Exception {
        asyncFetch11 = Mockito.spy(new Taxii11AsyncFetch(TestData.pollParameters, 60000));
        asyncFetch20 = Mockito.spy(new Taxii20AsyncFetch(TestData.getParameters));
        asyncFetches = Arrays.asList(asyncFetch11, asyncFetch20);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        asyncFetchRequestService = Mockito.spy(new AsyncFetchRequestService(repository, collectionService, downloadService, userService, eventService));
        asyncFetchRequestService.setFetchChunkWindow(60000);
    }

    @Test
    public void startAsyncFetch() {
        asyncFetchRequestService.startAsyncFetch(TestData.taxii11Association, TestData.pollParameters);
        verify(asyncFetchRequestService, times(1)).startAsyncFetch(any(Taxii11AsyncFetch.class));
        asyncFetchRequestService.startAsyncFetch(TestData.taxii20Association, TestData.getParameters);
        verify(asyncFetchRequestService, times(1)).startAsyncFetch(any(Taxii20AsyncFetch.class));
    }

    @Test
    public void performAsyncFetchRequest() {
        when(repository.findAllByStatus(AsyncFetch.Status.PENDING)).thenReturn(asyncFetches);
        asyncFetchRequestService.performAsyncFetchRequest();
        verify(asyncFetchRequestService, times(1)).processAsyncFetch(asyncFetch11);
        verify(asyncFetchRequestService, times(1)).processAsyncFetch(asyncFetch20);
    }

    @Test
    public void processAsyncFetch11() {

        // All of the code below is to emulate subsequent poll responses, increasing the 'window' of retrieved content
        XMLGregorianCalendarImpl timestamp1 = (XMLGregorianCalendarImpl) XMLGregorianCalendarImpl.createDateTime(2018, 1, 1, 1, 1, 1);
        XMLGregorianCalendarImpl timestamp2 = (XMLGregorianCalendarImpl) XMLGregorianCalendarImpl.createDateTime(2018, 1, 1, 1, 2, 1);
        XMLGregorianCalendarImpl timestamp3 = (XMLGregorianCalendarImpl) XMLGregorianCalendarImpl.createDateTime(2018, 1, 1, 1, 3, 1);

        ContentBlock contentBlock1 = new ContentBlock()
            .withContentBinding(new ContentInstanceType(null, Constants.stix1ContentBindingsStringMap.get("1.1.1")))
            .withTimestampLabel(timestamp1)
            .withContent(new AnyMixedContentType().withContent(TestData.rawStix111));

        ContentBlock contentBlock2 = new ContentBlock()
            .withContentBinding(new ContentInstanceType(null, Constants.stix1ContentBindingsStringMap.get("1.1.1")))
            .withTimestampLabel(timestamp2)
            .withContent(new AnyMixedContentType().withContent(TestData.rawStix111));

        ContentBlock contentBlock3 = new ContentBlock()
            .withContentBinding(new ContentInstanceType(null, Constants.stix1ContentBindingsStringMap.get("1.1.1")))
            .withTimestampLabel(timestamp3)
            .withContent(new AnyMixedContentType().withContent(TestData.rawStix111));

        PollResponse response1 = new PollResponse()
            .withContentBlocks(contentBlock1)
            .withCollectionName(TestData.taxii11Collection.getName())
            .withMessageId(UUID.randomUUID().toString())
            .withInResponseTo(UUID.randomUUID().toString())
            .withMore(false)
            .withInclusiveEndTimestamp(timestamp1);

        PollResponse response2 = new PollResponse()
            .withContentBlocks(contentBlock2)
            .withCollectionName(TestData.taxii11Collection.getName())
            .withMessageId(UUID.randomUUID().toString())
            .withInResponseTo(UUID.randomUUID().toString())
            .withMore(false)
            .withInclusiveEndTimestamp(timestamp2);

        PollResponse response3 = new PollResponse()
            .withContentBlocks(contentBlock3)
            .withCollectionName(TestData.taxii11Collection.getName())
            .withMessageId(UUID.randomUUID().toString())
            .withInResponseTo(UUID.randomUUID().toString())
            .withMore(false)
            .withInclusiveEndTimestamp(timestamp3);


        when(downloadService.fetchContent(any(), any())).thenReturn(response1, response2, response3);
        when(downloadService.processTaxii11Content(any(), eq(TestData.taxii11Association))).thenReturn(new CountResult(1, 0, 1));

        asyncFetchRequestService.processAsyncFetch(asyncFetch11);

        // Verify 3 chunks are created based on the window supplied.
        // Window is 60,0000 millis
        // so 3 chunks should be iterated
        assertEquals(asyncFetch11.getEnd(), asyncFetch11.getLastRequested());
        assertEquals(asyncFetch11.getStatus(), AsyncFetch.Status.COMPLETE);
        verify(asyncFetch11, times(3)).next();
        verify(repository, times(4)).save(any());
    }

    @Test
    public void processAsyncFetch20() {
        asyncFetchRequestService.processAsyncFetch(asyncFetch20);
        verify(downloadService).fetchContent(any(Taxii20Association.class), any(), any(), any());
    }
}
