package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.domain.parameters.UploadedFile;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
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
        Map<String, UploadedFile> fileMap = new HashMap<>();
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFilename("1");
        uploadedFile.setContent(TestData.rawStix21);

        PowerMockito.mockStatic(TaxiiAssociation.class);
        when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii21Association);
        when(uploadService.publish(any(TaxiiAssociation.class), any(HashMap.class))).thenReturn("Success");

        ResponseEntity<String> response = uploadResource.publish(fileMap,  "testServerLabel", "testCollectionId");
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Success"));
    }

    @Test
    public void testPublishStix20_invalid() throws Exception {
        Map<String, UploadedFile> fileMap = new HashMap<>();
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFilename("1");
        uploadedFile.setContent(TestData.rawStix21);

        PowerMockito.mockStatic(TaxiiAssociation.class);
        when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii21Association);
        when(uploadService.publish(any(TaxiiAssociation.class), any(HashMap.class))).thenReturn("Failed");

        ResponseEntity<String> response = uploadResource.publish(fileMap, "testServerLabel", "testCollectionId");
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Failed"));
    }

    @Test
    public void testPublishStix11() throws Exception {
        Map<String, UploadedFile> fileMap = new HashMap<>();
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFilename("1");
        uploadedFile.setContent(TestData.rawStix111);

        PowerMockito.mockStatic(TaxiiAssociation.class);
        when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii11Association);
        when(uploadService.publish(any(TaxiiAssociation.class), any(HashMap.class))).thenReturn("Success");

        ResponseEntity<String> response = uploadResource.publish(fileMap, "testServerLabel", "testCollectionId");
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Success"));
    }

    @Test
    public void testPublishStix11_invalid()  {
        Map<String, UploadedFile> fileMap = new HashMap<>();
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFilename("1");
        uploadedFile.setContent(TestData.rawStix111);
        PowerMockito.mockStatic(TaxiiAssociation.class);
        when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii11Association);
        when(uploadService.publish(any(TaxiiAssociation.class), any(HashMap.class))).thenReturn("Fail");

        ResponseEntity<String> response = uploadResource.publish(fileMap, "testServerLabel", "testCollectionId");
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Fail"));
    }
}
