package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.domain.async.RecurringFetch;
import com.bcmc.xor.flare.client.api.domain.parameters.ApiParameters;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.scheduled.RecurringFetchService;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


/**
 * Test class for the Recurring Fetch REST controller.
 *
 * @see RecurringFetchResource
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(TaxiiAssociation.class)
@PowerMockIgnore("javax.management.*")
public class RecurringFetchResourceTest {

    @MockBean
    RecurringFetchResource recurringFetchResource;

    @MockBean
    ServerService serverService;

    @MockBean
    CollectionService collectionService;

    @MockBean
    RecurringFetchService recurringFetchService;

    @MockBean
    RecurringFetch recurringFetch;

    @MockBean
    TaxiiAssociation taxiiAssociation;

    @MockBean
    ApiParameters apiParameters;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        recurringFetchResource = new RecurringFetchResource(serverService, collectionService, recurringFetchService);
        recurringFetch = new RecurringFetch(taxiiAssociation, Instant.now(), 5000, apiParameters);
        PowerMockito.mockStatic(TaxiiAssociation.class);
        PowerMockito.when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii11Association);
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        when(collectionService.findOneById(TestData.taxii11Collection.getId())).thenReturn(Optional.of(TestData.taxii11Collection));
        when(serverService.findOneByLabel(TestData.taxii21Server.getLabel())).thenReturn(Optional.of(TestData.taxii21Server));
        when(collectionService.findOneById(TestData.taxii21Collection.getId())).thenReturn(Optional.of(TestData.taxii21Collection));
    }

    @Test
    public void getRecurringFetch() throws Exception {
        when(recurringFetchService.findByAssociation(any(TaxiiAssociation.class))).thenReturn(Optional.ofNullable(recurringFetch));
        ResponseEntity<RecurringFetch> response = recurringFetchResource.getRecurringFetch(
                TestData.taxii11Server.getLabel(),
                TestData.taxii11Collection.getId());
        assertNotNull(response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void getRecurringFetchNotFound() {
        when(recurringFetchService.findByAssociation(any(TaxiiAssociation.class))).thenReturn(Optional.ofNullable(null));
        ResponseEntity<RecurringFetch> response = recurringFetchResource.getRecurringFetch(
                TestData.taxii11Server.getLabel(),
                TestData.taxii11Collection.getId());
        assertNotNull(response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void deleteRecurringFetch() {
        when(recurringFetchService.deleteRecurringFetch(any(TaxiiAssociation.class))).thenReturn(ResponseEntity.ok().body("Deleted recurring fetch."));
        ResponseEntity<String> response = recurringFetchResource.deleteRecurringFetch(
                TestData.taxii11Server.getLabel(),
                TestData.taxii11Collection.getId());
        assertNotNull(response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void deleteRecurringFetchNotFound() {
        String feedback = String.format("No recurring fetch exists for server '%s' and collection '%s'",
                TestData.taxii11Server.getLabel(), TestData.taxii11Collection.getDisplayName());
        when(recurringFetchService.deleteRecurringFetch(any(TaxiiAssociation.class))).thenReturn(ResponseEntity.badRequest().body(feedback));
        ResponseEntity<String> response = recurringFetchResource.deleteRecurringFetch(
                TestData.taxii11Server.getLabel(),
                TestData.taxii11Collection.getId());
        assertNotNull(response.getBody());
        assertTrue(response.getStatusCode().is4xxClientError());
    }

}
