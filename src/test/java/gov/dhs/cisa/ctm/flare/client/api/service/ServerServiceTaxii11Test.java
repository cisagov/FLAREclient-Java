package gov.dhs.cisa.ctm.flare.client.api.service;

import gov.dhs.cisa.ctm.flare.client.TestData;
import gov.dhs.cisa.ctm.flare.client.api.FlareclientApp;
import gov.dhs.cisa.ctm.flare.client.api.domain.server.Taxii11Server;
import gov.dhs.cisa.ctm.flare.client.api.domain.server.TaxiiServer;
import gov.dhs.cisa.ctm.flare.client.api.repository.EventRepository;
import gov.dhs.cisa.ctm.flare.client.api.repository.ServerRepository;
import gov.dhs.cisa.ctm.flare.client.api.repository.UserRepository;
import gov.dhs.cisa.ctm.flare.client.api.security.ServerCredentialsUtils;
import gov.dhs.cisa.ctm.flare.client.api.service.ApiRootService;
import gov.dhs.cisa.ctm.flare.client.api.service.CollectionService;
import gov.dhs.cisa.ctm.flare.client.api.service.EventService;
import gov.dhs.cisa.ctm.flare.client.api.service.ServerService;
import gov.dhs.cisa.ctm.flare.client.api.service.TaxiiService;
import gov.dhs.cisa.ctm.flare.client.api.service.UserService;
import gov.dhs.cisa.ctm.flare.client.api.service.dto.ServerDTO;
import gov.dhs.cisa.ctm.flare.client.api.service.dto.ServersDTO;
import gov.dhs.cisa.ctm.flare.client.taxii.taxii11.Taxii11RestTemplate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.taxii.messages.xml11.ServiceTypeEnum;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static gov.dhs.cisa.ctm.flare.client.TestData.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Taxii 1.1 Test Class for the ServerService.
 *
 * @see ServerService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class ServerServiceTaxii11Test {

    private final Logger log = LoggerFactory.getLogger(ServerServiceTaxii11Test.class);

    // Service
    @Autowired
    private ServerService serverService;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaxiiService taxiiService;

    @MockBean
    private Taxii11RestTemplate taxii11RestTemplate;

    @Autowired
    private ApiRootService apiRootService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @MockBean
    SecurityContext securityContext;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        serverService.setServerRepository(serverRepository);
        serverService.setUserService(userService);
        taxiiService.setTaxii11RestTemplate(taxii11RestTemplate);
        serverService.setTaxiiService(taxiiService);
        serverService.setApiRootService(apiRootService);
        serverService.setCollectionService(collectionService);
        serverService.setCacheManager(cacheManager);
        eventService.setEventRepository(eventRepository);
        serverService.setEventService(eventService);

        serverRepository.deleteAll();
        userRepository.deleteAll();

        userService.setUserRepository(userRepository);
        TestData.setLoggedInUser(securityContext, userService);
    }

    @After
    public void tearDown() throws Exception {
        serverRepository.deleteAll();
    }

    @Test
    public void testCreateTaxii11Server() {
        ServerDTO taxii11ServerDTO = new ServerDTO();
        taxii11ServerDTO.setLabel(taxii11Server.getLabel());
        taxii11ServerDTO.setRequiresBasicAuth(false);
        taxii11ServerDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii11RestTemplate().submitDiscovery(taxii11ServerDTO)).thenReturn(TestData.discoveryResponse);

        serverRepository.deleteAll();
        assertTrue(serverRepository.findAll().isEmpty());
        serverService.createServer(taxii11ServerDTO);
        assertFalse(serverRepository.findAll().isEmpty());

        Optional<Taxii11Server> maybeServer = serverRepository.findOneTaxii11ByLabelIgnoreCase(taxii11Server.getLabel());
        assertTrue(maybeServer.isPresent());
        assertEquals(taxii11Server.getLabel(), maybeServer.get().getLabel());
        assertFalse(maybeServer.get().getServiceInstances().isEmpty());
        assertTrue(maybeServer.get().getServiceInstances(ServiceTypeEnum.INBOX).contains(inboxServiceInstance));
        assertTrue(maybeServer.get().getServiceInstances(ServiceTypeEnum.POLL).contains(pollServiceInstance));
        assertTrue(maybeServer.get().getServiceInstances(ServiceTypeEnum.DISCOVERY).contains(discoveryServiceInstance));
        assertTrue(maybeServer.get().getServiceInstances(ServiceTypeEnum.COLLECTION_MANAGEMENT).contains(collectionManagementServiceInstance));
    }

//    @Test
//    public void testCreateTaxii11ServerWithBasicAuth() {
//        ServerDTO taxii11ServerDTO = new ServerDTO();
//        taxii11ServerDTO.setLabel(taxii11Server.getLabel());
//        taxii11ServerDTO.setRequiresBasicAuth(true);
//        taxii11ServerDTO.setUsername("user");
//        taxii11ServerDTO.setPassword("pass");
//        taxii11ServerDTO.setUrl("https://example.com");
//
//        when(taxiiService.getTaxii11RestTemplate().submitDiscovery(taxii11ServerDTO)).thenReturn(TestData.discoveryResponse);
//
//        serverRepository.deleteAll();
//        assertTrue(serverRepository.findAll().isEmpty());
//        serverService.createServer(taxii11ServerDTO);
//        assertFalse(serverRepository.findAll().isEmpty());
//
//        Optional<Taxii11Server> maybeServer = serverRepository.findOneTaxii11ByLabelIgnoreCase(taxii11Server.getLabel());
//        assertTrue(maybeServer.isPresent());
//        assertEquals(taxii11Server.getLabel(), maybeServer.get().getLabel());
//        assertFalse(maybeServer.get().getServiceInstances().isEmpty());
//        assertTrue(maybeServer.get().getServiceInstances(ServiceTypeEnum.INBOX).contains(inboxServiceInstance));
//        assertTrue(maybeServer.get().getServiceInstances(ServiceTypeEnum.POLL).contains(pollServiceInstance));
//        assertTrue(maybeServer.get().getServiceInstances(ServiceTypeEnum.DISCOVERY).contains(discoveryServiceInstance));
//        assertTrue(maybeServer.get().getServiceInstances(ServiceTypeEnum.COLLECTION_MANAGEMENT).contains(collectionManagementServiceInstance));
//        assertFalse(ServerCredentialsUtils.getInstance().getServerCredentialsMap().get("user").get(taxii11Server.getLabel()).isEmpty());
//    }

    @Test
    public void testRefreshTaxii11Server() {

        when(taxiiService.getTaxii11RestTemplate().submitDiscovery(any(Taxii11Server.class))).thenReturn(TestData.discoveryResponse);
        when(taxiiService.getTaxii11RestTemplate().submitCollectionInformationResponse(any(Taxii11Server.class), any(), any())).thenReturn(TestData.collectionInformationResponse);

        Taxii11Server server = newTaxii11Server("RefreshServer");
        serverRepository.save(server);
        TaxiiServer refreshedServer = serverService.refreshServer(server.getLabel());
        assertTrue(refreshedServer.hasReceivedServerInformation());
        assertTrue(refreshedServer.hasReceivedCollectionInformation());
        assertFalse(refreshedServer.getCollections().isEmpty());
    }

    @Test
    public void testUpdateTaxii11ServerCreate() {
        ServerDTO taxii11ServerDTO = new ServerDTO();
        taxii11ServerDTO.setLabel(taxii11Server.getLabel());
        taxii11ServerDTO.setRequiresBasicAuth(false);
        taxii11ServerDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii11RestTemplate().submitDiscovery(any(ServerDTO.class))).thenReturn(TestData.discoveryResponse);
        when(taxiiService.getTaxii11RestTemplate().submitCollectionInformationResponse(any(Taxii11Server.class), any(), any())).thenReturn(TestData.collectionInformationResponse);

        TaxiiServer updatedServer = serverService.updateServer(taxii11ServerDTO);
        assertTrue(updatedServer.hasReceivedServerInformation());
        assertTrue(updatedServer.hasReceivedCollectionInformation());
        assertFalse(updatedServer.getCollections().isEmpty());
    }

    @Test
    public void testUpdateTaxii11ServerUpdate() {
        ServerDTO taxii11ServerDTO = new ServerDTO();
        taxii11ServerDTO.setLabel(taxii11Server.getLabel());
        taxii11ServerDTO.setRequiresBasicAuth(false);
        taxii11ServerDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii11RestTemplate().submitDiscovery(any(ServerDTO.class))).thenReturn(TestData.discoveryResponse);
        when(taxiiService.getTaxii11RestTemplate().submitCollectionInformationResponse(any(Taxii11Server.class), any(), any())).thenReturn(TestData.collectionInformationResponse);

        TaxiiServer createdServer = serverService.createServer(taxii11ServerDTO);

        taxii11ServerDTO.setLabel("Changed Label");
        TaxiiServer updatedServer = serverService.updateServer(taxii11ServerDTO);

        assertTrue(updatedServer.getLastUpdated().isAfter(createdServer.getLastUpdated()));
        assertEquals(updatedServer.getLabel(), "Changed Label");
    }

    @Test
    public void testDeleteTaxii11Server() {
        ServerDTO taxii11ServerDTO = new ServerDTO();
        taxii11ServerDTO.setLabel(taxii11Server.getLabel());
        taxii11ServerDTO.setRequiresBasicAuth(false);
        taxii11ServerDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii11RestTemplate().submitDiscovery(taxii11ServerDTO)).thenReturn(TestData.discoveryResponse);

        serverService.createServer(taxii11ServerDTO);

        serverService.deleteServer(taxii11ServerDTO.getLabel());
        assertFalse(serverRepository.findOneByLabelIgnoreCase(taxii11ServerDTO.getLabel()).isPresent());
    }

    @Test
    public void testGetAllServers() {
        Taxii11Server server1 = newTaxii11Server("Server1");
        Taxii11Server server2 = newTaxii11Server("Server2");

        serverRepository.save(server1);
        serverRepository.save(server2);

        ServersDTO serversDTO = serverService.getAllServers();
        assertTrue(serversDTO.getByLabel().containsKey("Server1"));
        assertEquals(serversDTO.getByLabel().get("Server1").getLabel(), server1.getLabel());
        assertTrue(serversDTO.getByLabel().containsKey("Server2"));
        assertEquals(serversDTO.getByLabel().get("Server2").getLabel(), server2.getLabel());
    }

    @Test
    public void testGetServerByLabel() {
        Taxii11Server server1 = newTaxii11Server("Server1");
        serverRepository.save(server1);
        Optional<? extends TaxiiServer> maybeServer = serverService.findOneByLabel("Server1");
        assertTrue(maybeServer.isPresent());
        assertEquals(server1.getLabel(), maybeServer.get().getLabel());
    }

    @Test
    public void testGetServerById() {
        Taxii11Server server1 = newTaxii11Server("Server1");
        serverRepository.save(server1);
        Optional<? extends TaxiiServer> maybeServer = serverService.findOneById(server1.getId());
        assertTrue(maybeServer.isPresent());
        assertEquals(server1.getId(), maybeServer.get().getId());
    }

    private Taxii11Server newTaxii11Server(String label) {
        Taxii11Server server = new Taxii11Server();
        server.setId(UUID.randomUUID().toString());
        server.setLabel(label);
        server.setAvailable(true);
        server.setHasReceivedServerInformation(true);
        server.setHasReceivedCollectionInformation(true);
        server.setRequiresBasicAuth(false);
        server.setUrl(URI.create("http://test.com"));
        return server;
    }
}
