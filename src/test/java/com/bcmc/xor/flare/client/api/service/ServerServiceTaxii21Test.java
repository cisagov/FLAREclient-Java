package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.server.Taxii21Server;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.repository.EventRepository;
import com.bcmc.xor.flare.client.api.repository.ServerRepository;
import com.bcmc.xor.flare.client.api.repository.UserRepository;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServersDTO;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21RestTemplate;
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

import static com.bcmc.xor.flare.client.TestData.taxii21Discovery;
import static com.bcmc.xor.flare.client.TestData.taxii21Server;
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
public class ServerServiceTaxii21Test {

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
    private Taxii21RestTemplate taxii21RestTemplate;

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
        taxiiService.setTaxii21RestTemplate(taxii21RestTemplate);
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
    public void testCreateTaxii21Server() {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii21Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii21RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii21Discovery);
        when(taxiiService.getTaxii21RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii21ApiRoot);
        when(taxiiService.getTaxii21RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii21CollectionObject)));

        serverRepository.deleteAll();
        assertTrue(serverRepository.findAll().isEmpty());
        serverService.createServer(serverDTO);
        assertFalse(serverRepository.findAll().isEmpty());

        Optional<Taxii21Server> maybeServer = serverRepository.findOneTaxii21ByLabelIgnoreCase(taxii21Server.getLabel());
        assertTrue(maybeServer.isPresent());
        assertEquals("label", taxii21Server.getLabel(), maybeServer.get().getLabel());
        assertEquals("api roots", new HashSet<>(taxii21Discovery.getApiRoots()), maybeServer.get().getApiRoots());
        assertEquals("description", taxii21Discovery.getDescription(), maybeServer.get().getDescription());
        assertEquals("title", taxii21Discovery.getTitle(), maybeServer.get().getTitle());
        assertEquals("contact", taxii21Discovery.getContact(), maybeServer.get().getContact());
        assertEquals("default", taxii21Discovery.getDefaultApiRoot(), maybeServer.get().getDefaultApiRoot());
        assertFalse(maybeServer.get().getCollections().isEmpty());
        assertTrue(maybeServer.get().hasReceivedServerInformation());

    }

    @Test
    public void testCreateTaxii21ServerWithBasicAuth() {

        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii21Server.getLabel());
        serverDTO.setRequiresBasicAuth(true);
        serverDTO.setUsername("user");
        serverDTO.setPassword("pass");
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii21RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii21Discovery);
        when(taxiiService.getTaxii21RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii21ApiRoot);
        when(taxiiService.getTaxii21RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii21CollectionObject)));


        serverRepository.deleteAll();
        assertTrue(serverRepository.findAll().isEmpty());
        serverService.createServer(serverDTO);
        assertFalse(serverRepository.findAll().isEmpty());

        Optional<Taxii21Server> maybeServer = serverRepository.findOneTaxii21ByLabelIgnoreCase(taxii21Server.getLabel());
        assertTrue(maybeServer.isPresent());
        assertFalse(ServerCredentialsUtils.getInstance().getServerCredentialsMap().get("user").get(taxii21Server.getLabel()).isEmpty());
    }

    @Test
    public void testRefreshTaxii21Server() {

        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii21Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii21RestTemplate().discovery(any(Taxii21Server.class))).thenReturn(TestData.taxii21Discovery);
        when(taxiiService.getTaxii21RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii21ApiRoot);
        when(taxiiService.getTaxii21RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii21CollectionObject)));


        Taxii21Server server = newTaxii21Server("RefreshServer");
        serverRepository.save(server);
        TaxiiServer refreshedServer = serverService.refreshServer(server.getLabel());
        assertTrue(refreshedServer.hasReceivedServerInformation());
        assertTrue(refreshedServer.hasReceivedCollectionInformation());
        assertFalse(refreshedServer.getCollections().isEmpty());
    }

    @Test
    public void testUpdateTaxii21ServerCreate() {
        serverService.deleteServer(taxii21Server.getLabel());
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii21Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii21RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii21Discovery);
        when(taxiiService.getTaxii21RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii21ApiRoot);
        when(taxiiService.getTaxii21RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii21CollectionObject)));

        TaxiiServer updatedServer = serverService.updateServer(serverDTO);
        assertTrue(updatedServer.hasReceivedServerInformation());
        assertTrue(updatedServer.hasReceivedCollectionInformation());
        assertFalse(updatedServer.getCollections().isEmpty());
      }

    @Test
    public void testUpdateTaxii21ServerUpdate() {
        serverService.deleteServer(taxii21Server.getLabel());
        ServerDTO createServerDTO = new ServerDTO();
        createServerDTO.setLabel(taxii21Server.getLabel());
        createServerDTO.setRequiresBasicAuth(false);
        createServerDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii21RestTemplate().discovery(createServerDTO)).thenReturn(TestData.taxii21Discovery);
        when(taxiiService.getTaxii21RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii21ApiRoot);
        when(taxiiService.getTaxii21RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii21CollectionObject)));

        TaxiiServer createdServer = serverService.createServer(createServerDTO);

        ServerDTO updateServerDTO = new ServerDTO();
        updateServerDTO.setId(createdServer.getId());
        updateServerDTO.setLabel("Changed Label");
        updateServerDTO.setUrl(String.valueOf(createdServer.getUrl()));

        TaxiiServer updatedServer = serverService.updateServer(updateServerDTO);

        assertTrue(updatedServer.getLastUpdated().isAfter(createdServer.getLastUpdated()));
        assertEquals(updatedServer.getLabel(), "Changed Label");
    }

    @Test
    public void testDeleteTaxii21Server() {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(taxii21Server.getLabel());
        serverDTO.setRequiresBasicAuth(false);
        serverDTO.setUrl("https://example.com");

        when(taxiiService.getTaxii21RestTemplate().discovery(serverDTO)).thenReturn(TestData.taxii21Discovery);
        when(taxiiService.getTaxii21RestTemplate().getApiRoot(any(), any())).thenReturn(TestData.taxii21ApiRoot);
        when(taxiiService.getTaxii21RestTemplate().getCollections(any(), any())).thenReturn(new Collections(Arrays.asList(TestData.taxii21CollectionObject)));

        TaxiiServer createdServer = serverService.createServer(serverDTO);

        serverService.deleteServer(serverDTO.getLabel());
        assertFalse(serverRepository.findOneByLabelIgnoreCase(serverDTO.getLabel()).isPresent());
    }

    @Test
    public void testGetAllServers() {
        Taxii21Server server1 = newTaxii21Server("Server1");
        Taxii21Server server2 = newTaxii21Server("Server2");

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
        Taxii21Server server1 = newTaxii21Server("Server1");
        serverRepository.save(server1);
        Optional<? extends TaxiiServer> maybeServer = serverService.findOneByLabel("Server1");
        assertTrue(maybeServer.isPresent());
        assertEquals(server1.getLabel(), maybeServer.get().getLabel());
    }

    @Test
    public void testGetServerById() {
        Taxii21Server server1 = newTaxii21Server("Server1");
        serverRepository.save(server1);
        Optional<? extends TaxiiServer> maybeServer = serverService.findOneById(server1.getId());
        assertTrue(maybeServer.isPresent());
        assertEquals(server1.getId(), maybeServer.get().getId());
    }

    private Taxii21Server newTaxii21Server(String label) {
        Taxii21Server server = new Taxii21Server();
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
