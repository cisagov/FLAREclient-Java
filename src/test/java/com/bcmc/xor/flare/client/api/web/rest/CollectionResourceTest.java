package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.collection.TaxiiCollection;
import com.bcmc.xor.flare.client.api.repository.ContentRepository;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.dto.CollectionsDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class CollectionResourceTest {

    private final Logger log = LoggerFactory.getLogger(CollectionResourceTest.class);

    @Autowired
    CollectionResource collectionResource;

    @MockBean
    private ServerService serverService;
    @MockBean
    private CollectionService collectionService;
    @MockBean
    private ContentRepository contentRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        when(collectionService.findOneById(TestData.taxii11Collection.getId())).thenReturn(Optional.of(TestData.taxii11Collection));
    }

    @Test
    public void getAllCollectionsTest() {
        ResponseEntity<CollectionsDTO> response = collectionResource.getAllCollections(TestData.taxii11Server.getLabel());
        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getAllIds().size());
        assertTrue(response.getBody().getById().containsKey(TestData.taxii11Collection.getId()));
    }

    @Test
    public void getCollectionDetailsTest() {
        ResponseEntity<TaxiiCollection> response = collectionResource.getCollectionDetails(TestData.taxii11Server.getLabel(), TestData.taxii11Collection.getId());
        TaxiiCollection collectionResponse = response.getBody();
        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(TestData.taxii11Collection, collectionResponse);
    }

    @Test
    public void getCollectionTest() {
        ResponseEntity<TaxiiCollection> response = collectionResource.getCollection(TestData.taxii11Server.getLabel(), TestData.taxii11Collection.getId());
        TaxiiCollection collectionResponse = response.getBody();
        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(TestData.taxii11Collection, collectionResponse);
    }

}
