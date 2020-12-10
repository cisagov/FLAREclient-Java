package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.parameters.UploadedFile;
import com.bcmc.xor.flare.client.api.repository.CollectionRepository;
import com.bcmc.xor.flare.client.api.repository.ServerRepository;
import com.bcmc.xor.flare.client.api.repository.StatusRepository;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21RestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
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
public class UploadServiceTaxii21Test {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private TaxiiService taxiiService;

    @MockBean
    private EventService eventService;

    @MockBean
    private Taxii21RestTemplate taxii21RestTemplate;

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
        taxiiService.setTaxii21RestTemplate(taxii21RestTemplate);
        uploadService.setEventService(eventService);
        uploadService.setTaxiiService(taxiiService);
        uploadService.setStatusService(statusService);
        uploadService.setServerService(serverService);
        uploadService.setCollectionService(collectionService);
        uploadService.setUserService(userService);

        statusService.setStatusRepository(statusRepository);
        statusService.setCacheManager(cacheManager);

        MockitoAnnotations.initMocks(this);

        serverRepository.save(TestData.taxii21Server);
        collectionRepository.save(TestData.taxii21Collection);
        TestData.setLoggedInUser(securityContext, userService);
    }

    @Test
    public void getUploadUrl() {
        assertEquals(
            TestData.taxii21Association.getServer().getCollectionObjectsUrl(TestData.apiRoot.getEndpoint(), TestData.taxii21Association.getCollection().getCollectionObject().getId()),
            uploadService.getUploadUrl(TestData.taxii21Association));
    }

    @Test
    public void publish() {

        when(taxiiService.getTaxii21RestTemplate().postBundle(eq(TestData.taxii21Server), any(), any()))
            .thenReturn(TestData.taxii21Status);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setContent(TestData.rawStix21);
        uploadedFile.setFilename("test.json");
        uploadedFile.setHash(1);

        Map<String, UploadedFile> fileMap = new HashMap<>();
        fileMap.put("test.json", uploadedFile);

        String response = uploadService.publish(TestData.taxii21Association, fileMap);

        assertEquals("Successfully published 1 bundle(s).", response);

    }

    @Test
    public void publishFailure() {

        when(taxiiService.getTaxii21RestTemplate().postBundle(eq(TestData.taxii21Server), any(), any()))
            .thenReturn(null);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setContent(TestData.rawStix21);
        uploadedFile.setFilename("test.json");
        uploadedFile.setHash(1);

        Map<String, UploadedFile> fileMap = new HashMap<>();
        fileMap.put("test.json", uploadedFile);

        String response = uploadService.publish(TestData.taxii21Association, fileMap);

        assertEquals("Failed to published 1 bundle(s).", response);

    }
}
