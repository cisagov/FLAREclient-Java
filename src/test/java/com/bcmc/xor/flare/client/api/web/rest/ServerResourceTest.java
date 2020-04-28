package com.bcmc.xor.flare.client.api.web.rest;


import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.dto.ServerCredentialDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServersDTO;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServerResourceTest {

    private ServerResource serverResource;

    @MockBean
    private ServerService serverService;

    @Before
    public void init() {
        serverResource = new ServerResource(serverService);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateTaxii11ServerNoAuth() throws Exception {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(TestData.taxii11Server.getLabel());
        serverDTO.setUrl(TestData.taxii11Server.getUrl().toString());
        serverDTO.setRequiresBasicAuth(false);

        when(serverService.updateServer(serverDTO)).thenReturn(TestData.taxii11Server);

        ResponseEntity<TaxiiServer> response = serverResource.createOrUpdateServer(serverDTO);

        assertEquals(TestData.taxii11Server.getLabel(), response.getBody().getLabel());
        assertEquals(TestData.taxii11Server.getUrl(), response.getBody().getUrl());
        assertEquals(TestData.taxii11Server.getVersion(), response.getBody().getVersion());
    }

    @Test
    public void testCreateTaxii11ServerAuth() throws Exception {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(TestData.taxii11Server.getLabel());
        serverDTO.setUrl(TestData.taxii11Server.getUrl().toString());
        serverDTO.setRequiresBasicAuth(true);
        serverDTO.setUsername("user");
        serverDTO.setPassword("user");

        when(serverService.updateServer(serverDTO)).thenReturn(TestData.taxii11Server);

        ResponseEntity<TaxiiServer> response = serverResource.createOrUpdateServer(serverDTO);
        verify(serverService).addServerCredential(TestData.taxii11Server.getLabel(),
            serverDTO.getUsername(), serverDTO.getPassword());
        assertEquals(TestData.taxii11Server.getLabel(), response.getBody().getLabel());
        assertEquals(TestData.taxii11Server.getUrl(), response.getBody().getUrl());
        assertEquals(TestData.taxii11Server.getVersion(), response.getBody().getVersion());
    }

    @Test
    public void testCreateTaxii20ServerNoAuth() throws Exception {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(TestData.taxii20Server.getLabel());
        serverDTO.setUrl(TestData.taxii20Server.getUrl().toString());
        serverDTO.setRequiresBasicAuth(false);

        when(serverService.updateServer(serverDTO)).thenReturn(TestData.taxii20Server);

        ResponseEntity<TaxiiServer> response = serverResource.createOrUpdateServer(serverDTO);

        assertEquals(TestData.taxii20Server.getLabel(), response.getBody().getLabel());
        assertEquals(TestData.taxii20Server.getUrl(), response.getBody().getUrl());
        assertEquals(TestData.taxii20Server.getVersion(), response.getBody().getVersion());
    }

    @Test
    public void testCreateTaxii20ServerAuth() throws Exception {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setLabel(TestData.taxii20Server.getLabel());
        serverDTO.setUrl(TestData.taxii20Server.getUrl().toString());
        serverDTO.setRequiresBasicAuth(true);
        serverDTO.setUsername("user");
        serverDTO.setPassword("user");

        when(serverService.updateServer(serverDTO)).thenReturn(TestData.taxii20Server);

        ResponseEntity<TaxiiServer> response = serverResource.createOrUpdateServer(serverDTO);
        verify(serverService).addServerCredential(TestData.taxii20Server.getLabel(),
            serverDTO.getUsername(), serverDTO.getPassword());
        assertEquals(TestData.taxii20Server.getLabel(), response.getBody().getLabel());
        assertEquals(TestData.taxii20Server.getUrl(), response.getBody().getUrl());
        assertEquals(TestData.taxii20Server.getVersion(), response.getBody().getVersion());
    }

    @Test
    public void testGetAllServers() {
        ServersDTO serversDTO = new ServersDTO(Collections.singletonList(TestData.taxii11Server));
        when(serverService.getAllServers()).thenReturn(serversDTO);
        ResponseEntity<ServersDTO> response = serverResource.getAllServers();
        assertTrue(response.getBody().getByLabel().containsKey(TestData.taxii11Server.getLabel()));
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testGetServer() {
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        ResponseEntity<ServerDTO> response = serverResource.getServer(TestData.taxii11Server.getLabel());

        assertEquals(TestData.taxii11Server.getLabel(), response.getBody().getLabel());
        assertEquals(TestData.taxii11Server.getVersion(), response.getBody().getVersion());
        assertEquals(TestData.taxii11Server.getUrl().toString(), response.getBody().getUrl());
    }

    @Test
    public void testDeleteServer() {
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        ResponseEntity<Void> response = serverResource.deleteServer(TestData.taxii11Server.getLabel());
        verify(serverService).deleteServer(TestData.taxii11Server.getLabel());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testRefreshServer() {
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        ResponseEntity<ServerDTO> response = serverResource.refreshServer(TestData.taxii11Server.getLabel());
        verify(serverService).refreshServer(TestData.taxii11Server.getLabel());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testAddServerCredential() {
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        ResponseEntity<ServerDTO> response = serverResource.addServerCredential(
            TestData.taxii11Server.getLabel(),
            new ServerCredentialDTO(TestData.taxii11Server.getLabel(), "user", "user"));
        verify(serverService).addServerCredential(TestData.taxii11Server.getLabel(), "user", "user");
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testDeleteServerCredential() {
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        ResponseEntity<ServerDTO> response = serverResource.deleteServerCredential(TestData.taxii11Server.getLabel());
        verify(serverService).removeServerCredential(TestData.taxii11Server.getLabel());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
