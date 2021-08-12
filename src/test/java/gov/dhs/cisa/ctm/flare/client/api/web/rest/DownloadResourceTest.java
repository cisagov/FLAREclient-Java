package gov.dhs.cisa.ctm.flare.client.api.web.rest;

import gov.dhs.cisa.ctm.flare.client.TestData;
import gov.dhs.cisa.ctm.flare.client.api.FlareclientApp;
import gov.dhs.cisa.ctm.flare.client.api.service.CollectionService;
import gov.dhs.cisa.ctm.flare.client.api.service.ServerService;
import gov.dhs.cisa.ctm.flare.client.api.service.scheduled.RecurringFetchService;
import gov.dhs.cisa.ctm.flare.client.api.service.scheduled.async.AsyncFetchRequestService;
import gov.dhs.cisa.ctm.flare.client.api.web.rest.DownloadResource;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DownloadResourceTest {

    @Autowired
    DownloadResource downloadResource;

    @MockBean
    private ServerService serverService;

    @MockBean
    private CollectionService collectionService;

    @MockBean
    private AsyncFetchRequestService asyncFetchRequestService;

    @MockBean
    private RecurringFetchService recurringFetchService;

    @Before
    public void init() {
        downloadResource = new DownloadResource(serverService, collectionService, asyncFetchRequestService, recurringFetchService);
        MockitoAnnotations.initMocks(this);

        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        when(collectionService.findOneById(TestData.taxii11Collection.getId())).thenReturn(Optional.of(TestData.taxii11Collection));
        when(serverService.findOneByLabel(TestData.taxii21Server.getLabel())).thenReturn(Optional.of(TestData.taxii21Server));
        when(collectionService.findOneById(TestData.taxii21Collection.getId())).thenReturn(Optional.of(TestData.taxii21Collection));
    }

    @Test
    public void testFetchTaxii11Content() {
        ResponseEntity<String> response = downloadResource.fetchContent(
            TestData.taxii11Server.getLabel(),
            TestData.taxii11Collection.getId(), TestData.pollParameters);
        verify(asyncFetchRequestService).startAsyncFetch(TestData.taxii11Association, TestData.pollParameters);
        assertNotNull(response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testFetchTaxii21Content() {
        ResponseEntity<String> response = downloadResource.fetchContent(
            TestData.taxii21Server.getLabel(),
            TestData.taxii21Collection.getId(), TestData.getParameters);
        verify(asyncFetchRequestService).startAsyncFetch(TestData.taxii21Association, TestData.getParameters);
        assertNotNull(response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
