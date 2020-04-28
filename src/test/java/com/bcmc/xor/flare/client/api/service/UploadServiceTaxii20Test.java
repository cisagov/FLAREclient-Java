package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.parameters.UploadedFile;
import com.bcmc.xor.flare.client.api.repository.CollectionRepository;
import com.bcmc.xor.flare.client.api.repository.ServerRepository;
import com.bcmc.xor.flare.client.api.repository.StatusRepository;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20RestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class UploadServiceTaxii20Test {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private TaxiiService taxiiService;

    @MockBean
    private EventService eventService;

    @MockBean
    private Taxii20RestTemplate taxii20RestTemplate;

    @Autowired
    private StatusService statusService;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ServerService serverService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    ServerRepository serverRepository;

    @Autowired
    CollectionRepository collectionRepository;

    @MockBean
    SecurityContext securityContext;

    @Autowired
    private UserService userService;

    @Before
    public void init() {
        taxiiService.setTaxii20RestTemplate(taxii20RestTemplate);
        uploadService.setEventService(eventService);
        uploadService.setTaxiiService(taxiiService);
        uploadService.setStatusService(statusService);
        uploadService.setServerService(serverService);
        uploadService.setCollectionService(collectionService);
        uploadService.setUserService(userService);

        statusService.setStatusRepository(statusRepository);
        statusService.setCacheManager(cacheManager);

        MockitoAnnotations.initMocks(this);

        serverRepository.save(TestData.taxii20Server);
        collectionRepository.save(TestData.taxii20Collection);
        TestData.setLoggedInUser(securityContext, userService);
    }

    @Test
    public void getUploadUrl() {
        assertEquals(
            TestData.taxii20Association.getServer().getCollectionObjectsUrl(TestData.apiRoot.getEndpoint(), TestData.taxii20Association.getCollection().getCollectionObject().getId()),
            uploadService.getUploadUrl(TestData.taxii20Association));
    }

    @Test
    public void publish() {

        when(taxiiService.getTaxii20RestTemplate().postBundle(eq(TestData.taxii20Server), any(), any()))
            .thenReturn(TestData.taxii20Status);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setContent(TestData.rawStix20);
        uploadedFile.setFilename("test.json");
        uploadedFile.setHash(1);

        Map<String, UploadedFile> fileMap = new HashMap<>();
        fileMap.put("test.json", uploadedFile);

        ResponseEntity<String> response = uploadService.publish(TestData.taxii20Association, fileMap);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Successfully published 1 bundle(s).", response.getBody());

    }

    @Test
    public void publishFailure() {

        when(taxiiService.getTaxii20RestTemplate().postBundle(eq(TestData.taxii20Server), any(), any()))
            .thenReturn(null);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setContent(TestData.rawStix20);
        uploadedFile.setFilename("test.json");
        uploadedFile.setHash(1);

        Map<String, UploadedFile> fileMap = new HashMap<>();
        fileMap.put("test.json", uploadedFile);

        ResponseEntity<String> response = uploadService.publish(TestData.taxii20Association, fileMap);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Failed to published 1 bundle(s).", response.getBody());

    }
}
