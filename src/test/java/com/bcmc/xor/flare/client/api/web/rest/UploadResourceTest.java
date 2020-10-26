package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.TaxiiService;
import com.bcmc.xor.flare.client.api.service.UploadService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(TaxiiAssociation.class)
@PowerMockIgnore("javax.management.*")
public class UploadResourceTest {

    @MockBean
    UploadResource uploadResource;

    @MockBean
    ServerService serverService;

    @MockBean
    CollectionService collectionService;

    @MockBean
    UploadService uploadService;

    @MockBean
    TaxiiService taxiiService;

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        uploadResource = new UploadResource(serverService,collectionService,uploadService);
    }

    @Test
    public void testPublishStix20() throws Exception {
        MockMultipartFile file = new MockMultipartFile("rawStix20","rawStix20", TestData.rawStix20, TestData.rawStix20.getBytes());

        PowerMockito.mockStatic(TaxiiAssociation.class);
        when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii20Association);
        when(uploadService.publish(any(TaxiiAssociation.class), any(HashMap.class))).thenReturn("Success");

        ResponseEntity<String> response = uploadResource.publish(file, "testFileName", "testServerLabel", "testCollectionId");
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Success"));
    }

    @Test
    public void testPublishStix20_invalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("rawStix20","rawStix20", TestData.rawStix20, "Bad data".getBytes());

        PowerMockito.mockStatic(TaxiiAssociation.class);
        when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii20Association);
        when(uploadService.publish(any(TaxiiAssociation.class), any(HashMap.class))).thenReturn("Failed");

        ResponseEntity<String> response = uploadResource.publish(file, "testFileName", "testServerLabel", "testCollectionId");
        assertTrue(response.getStatusCode().value() == 500);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Failed"));
    }

    @Test
    public void testPublishStix11() throws Exception {
        MockMultipartFile file = new MockMultipartFile("rawStix11","rawStix11", TestData.rawStix111, TestData.rawStix111.getBytes());

        PowerMockito.mockStatic(TaxiiAssociation.class);
        when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii11Association);
        when(uploadService.publish(any(TaxiiAssociation.class), any(HashMap.class))).thenReturn("Success");

        ResponseEntity<String> response = uploadResource.publish(file, "testFileName", "testServerLabel", "testCollectionId");
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Success"));
    }

    @Test
    public void testPublishStix11_invalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("rawStix11","rawStix11", TestData.rawStix111, TestData.rawStix111.getBytes());

        PowerMockito.mockStatic(TaxiiAssociation.class);
        when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii11Association);
        when(uploadService.publish(any(TaxiiAssociation.class), any(HashMap.class))).thenReturn("Fail");

        ResponseEntity<String> response = uploadResource.publish(file, "testFileName", "testServerLabel", "testCollectionId");
        assertTrue(response.getStatusCode().value() == 500);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Fail"));
    }
}
