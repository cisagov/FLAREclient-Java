package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.server.Taxii20Server;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.repository.EventRepository;
import com.bcmc.xor.flare.client.api.repository.ServerRepository;
import com.bcmc.xor.flare.client.api.repository.UserRepository;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServersDTO;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20RestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit4.SpringRunner;
import xor.bcmc.taxii2.resources.Collections;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static com.bcmc.xor.flare.client.TestData.taxii20Discovery;
import static com.bcmc.xor.flare.client.TestData.taxii20Server;
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
public class ServerServiceTaxii20Test {

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
    private Taxii20RestTemplate taxii20RestTemplate;

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
    public void setup() {
        serverService.setServerRepository(serverRepository);
        serverService.setUserService(userService);
        taxiiService.setTaxii20RestTemplate(taxii20RestTemplate);
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

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateTaxii20Server() {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii20Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii20RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii20Discovery);
        when(taxiiService.getTaxii20RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii20ApiRoot);
        when(taxiiService.getTaxii20RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii20CollectionObject)));

        serverRepository.deleteAll();
        assertTrue(serverRepository.findAll().isEmpty());
        serverService.createServer(serverDTO);
        assertFalse(serverRepository.findAll().isEmpty());

        Optional<Taxii20Server> maybeServer = serverRepository.findOneTaxii20ByLabelIgnoreCase(taxii20Server.getLabel());
        assertTrue(maybeServer.isPresent());
        assertEquals("label", taxii20Server.getLabel(), maybeServer.get().getLabel());
        assertEquals("api roots", new HashSet<>(taxii20Discovery.getApiRoots()), maybeServer.get().getApiRoots());
        assertEquals("description", taxii20Discovery.getDescription(), maybeServer.get().getDescription());
        assertEquals("title", taxii20Discovery.getTitle(), maybeServer.get().getTitle());
        assertEquals("contact", taxii20Discovery.getContact(), maybeServer.get().getContact());
        assertEquals("default", taxii20Discovery.getDefaultApiRoot(), maybeServer.get().getDefaultApiRoot());
        assertFalse(maybeServer.get().getCollections().isEmpty());
        assertTrue(maybeServer.get().hasReceivedServerInformation());

    }

    @Test
    public void testCreateTaxii20ServerWithBasicAuth() {

        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii20Server.getLabel());
        serverDTO.setRequiresBasicAuth(true);
        serverDTO.setUsername("user");
        serverDTO.setPassword("pass");
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii20RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii20Discovery);
        when(taxiiService.getTaxii20RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii20ApiRoot);
        when(taxiiService.getTaxii20RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii20CollectionObject)));

        serverRepository.deleteAll();
        assertTrue(serverRepository.findAll().isEmpty());
        serverService.createServer(serverDTO);
        assertFalse(serverRepository.findAll().isEmpty());

        Optional<Taxii20Server> maybeServer = serverRepository.findOneTaxii20ByLabelIgnoreCase(taxii20Server.getLabel());
        assertTrue(maybeServer.isPresent());
        assertFalse(ServerCredentialsUtils.getInstance().getServerCredentialsMap().get("user").get(taxii20Server.getLabel()).isEmpty());
    }

    @Test
    public void testRefreshTaxii20Server() {

        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii20Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii20RestTemplate().discovery(any(Taxii20Server.class))).thenReturn(TestData.taxii20Discovery);
        when(taxiiService.getTaxii20RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii20ApiRoot);
        when(taxiiService.getTaxii20RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii20CollectionObject)));


        Taxii20Server server = newTaxii20Server("RefreshServer");
        serverRepository.save(server);
        TaxiiServer refreshedServer = serverService.refreshServer(server.getLabel());
        assertTrue(refreshedServer.hasReceivedServerInformation());
        assertTrue(refreshedServer.hasReceivedCollectionInformation());
        assertFalse(refreshedServer.getCollections().isEmpty());
    }

    @Test
    public void testUpdateTaxii20ServerCreate() {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii20Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii20RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii20Discovery);
        when(taxiiService.getTaxii20RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii20ApiRoot);
        when(taxiiService.getTaxii20RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii20CollectionObject)));

        TaxiiServer updatedServer = serverService.updateServer(serverDTO);
        assertTrue(updatedServer.hasReceivedServerInformation());
        assertTrue(updatedServer.hasReceivedCollectionInformation());
        assertFalse(updatedServer.getCollections().isEmpty());
    }

    @Test
    public void testUpdateTaxii20ServerUpdate() {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii20Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii20RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii20Discovery);
        when(taxiiService.getTaxii20RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii20ApiRoot);
        when(taxiiService.getTaxii20RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii20CollectionObject)));

        TaxiiServer createdServer = serverService.createServer(serverDTO);

        serverDTO.setLabel("Changed Label");
        TaxiiServer updatedServer = serverService.updateServer(serverDTO);

        assertTrue(updatedServer.getLastUpdated().isAfter(createdServer.getLastUpdated()));
        assertEquals(updatedServer.getLabel(), "Changed Label");
    }

    @Test
    public void testDeleteTaxii20Server() {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii20Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii20RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii20Discovery);
        when(taxiiService.getTaxii20RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii20ApiRoot);
        when(taxiiService.getTaxii20RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii20CollectionObject)));

        TaxiiServer createdServer = serverService.createServer(serverDTO);

        serverService.deleteServer(serverDTO.getLabel());
        assertFalse(serverRepository.findOneByLabelIgnoreCase(serverDTO.getLabel()).isPresent());
    }

    @Test
    public void testGetAllServers() {
        Taxii20Server server1 = newTaxii20Server("Server1");
        Taxii20Server server2 = newTaxii20Server("Server2");

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
        Taxii20Server server1 = newTaxii20Server("Server1");
        serverRepository.save(server1);
        Optional<? extends TaxiiServer> maybeServer = serverService.findOneByLabel("Server1");
        assertTrue(maybeServer.isPresent());
        assertEquals(server1.getLabel(), maybeServer.get().getLabel());
    }

    @Test
    public void testGetServerById() {
        Taxii20Server server1 = newTaxii20Server("Server1");
        serverRepository.save(server1);
        Optional<? extends TaxiiServer> maybeServer = serverService.findOneById(server1.getId());
        assertTrue(maybeServer.isPresent());
        assertEquals(server1.getId(), maybeServer.get().getId());
    }

    private Taxii20Server newTaxii20Server(String label) {
        Taxii20Server server = new Taxii20Server();
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
