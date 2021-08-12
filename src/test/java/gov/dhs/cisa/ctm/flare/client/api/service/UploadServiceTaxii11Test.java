package gov.dhs.cisa.ctm.flare.client.api.service;

import com.google.common.collect.Sets;

import gov.dhs.cisa.ctm.flare.client.TestData;
import gov.dhs.cisa.ctm.flare.client.api.FlareclientApp;
import gov.dhs.cisa.ctm.flare.client.api.domain.collection.Taxii11Collection;
import gov.dhs.cisa.ctm.flare.client.api.domain.parameters.UploadedFile;
import gov.dhs.cisa.ctm.flare.client.api.domain.server.Taxii11Server;
import gov.dhs.cisa.ctm.flare.client.api.repository.CollectionRepository;
import gov.dhs.cisa.ctm.flare.client.api.repository.ServerRepository;
import gov.dhs.cisa.ctm.flare.client.api.service.CollectionService;
import gov.dhs.cisa.ctm.flare.client.api.service.EventService;
import gov.dhs.cisa.ctm.flare.client.api.service.ServerService;
import gov.dhs.cisa.ctm.flare.client.api.service.StatusService;
import gov.dhs.cisa.ctm.flare.client.api.service.TaxiiService;
import gov.dhs.cisa.ctm.flare.client.api.service.UploadService;
import gov.dhs.cisa.ctm.flare.client.api.service.UserService;
import gov.dhs.cisa.ctm.flare.client.taxii.taxii11.Taxii11Association;
import gov.dhs.cisa.ctm.flare.client.taxii.taxii11.Taxii11RestTemplate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.taxii.messages.xml11.CollectionRecordType;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class UploadServiceTaxii11Test {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private TaxiiService taxiiService;

    @MockBean
    private EventService eventService;

    @MockBean
    private Taxii11RestTemplate taxii11RestTemplate;

    @Autowired
    private StatusService statusService;

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
        taxiiService.setTaxii11RestTemplate(taxii11RestTemplate);
        uploadService.setEventService(eventService);
        uploadService.setTaxiiService(taxiiService);
        uploadService.setStatusService(statusService);
        uploadService.setServerService(serverService);
        uploadService.setCollectionService(collectionService);
        uploadService.setUserService(userService);
        MockitoAnnotations.initMocks(this);

        serverRepository.save(TestData.taxii21Server);
        collectionRepository.save(TestData.taxii21Collection);
        TestData.setLoggedInUser(securityContext, userService);
    }

    @Test
    public void getUploadUrlFromCollection() {
        assertEquals(TestData.inboxServiceInstance.getAddress(), uploadService.getUploadUrl(TestData.taxii11Association).toString());
    }

    @Test
    public void getUploadUrlFromServer() {
        Taxii11Server server = new Taxii11Server();
        server.setServiceInstances(Sets.newHashSet(TestData.inboxServiceInstance));
        CollectionRecordType collectionRecordType = new CollectionRecordType()
            .withCollectionName("test");
        Taxii11Collection collection = new Taxii11Collection(collectionRecordType, TestData.taxii11Server.getId());
        server.setCollections(Sets.newHashSet(TestData.taxii11Collection));

        Taxii11Association association = new Taxii11Association(TestData.taxii11Server, collection, null);

        assertEquals(TestData.inboxServiceInstance.getAddress(), uploadService.getUploadUrl(association).toString());
    }

    @Test
    public void publish() {

        when(taxiiService.getTaxii11RestTemplate().submitInbox(eq(TestData.taxii11Server), any(), eq(URI.create(TestData.inboxServiceInstance.getAddress()))))
            .thenReturn(TestData.successStatusMessage);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setContent(TestData.rawStix111);
        uploadedFile.setFilename("test.xml");
        uploadedFile.setHash(1);

        Map<String, UploadedFile> fileMap = new HashMap<>();
        fileMap.put("test.xml", uploadedFile);

        String response = uploadService.publish(TestData.taxii11Association, fileMap);

        assertEquals(TestData.successStatusMessage.getMessage(), response);

    }

    @Test
    public void publishDocumentInvalid() {

        when(taxiiService.getTaxii11RestTemplate().submitInbox(eq(TestData.taxii11Server), any(), eq(URI.create(TestData.inboxServiceInstance.getAddress()))))
            .thenReturn(TestData.failureStatusMessage);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setContent("Not Xml");
        uploadedFile.setFilename("test.xml");
        uploadedFile.setHash(1);

        Map<String, UploadedFile> fileMap = new HashMap<>();
        fileMap.put("test.xml", uploadedFile);

        String response = uploadService.publish(TestData.taxii11Association, fileMap);

        assertEquals(TestData.failureStatusMessage.getMessage(), response);
    }
}
