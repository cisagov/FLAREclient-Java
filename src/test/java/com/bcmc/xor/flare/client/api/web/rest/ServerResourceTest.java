package com.bcmc.xor.flare.client.api.web.rest;


import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.security.SecurityUtils;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.api.service.dto.ServerCredentialDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServersDTO;
import com.bcmc.xor.flare.client.error.ErrorConstants;
import com.bcmc.xor.flare.client.error.ServerNotFoundException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(SecurityUtils.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PowerMockIgnore("javax.management.*")
public class ServerResourceTest {

    private ServerResource serverResource;
    private User user;

    @MockBean
    private ServerService serverService;
    @MockBean
    private UserService userService;

    @Before
    public void init() {
        serverResource = new ServerResource(serverService, userService);
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

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
        when(serverService.findOneByLabel(serverDTO.getLabel())).thenReturn(Optional.empty());
        when(serverService.updateServer(serverDTO)).thenReturn(TestData.taxii11Server);

        ResponseEntity<TaxiiServer> response = serverResource.createOrUpdateServer(serverDTO);
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
        serverDTO.setRequiresBasicAuth(false);

        when(serverService.updateServer(serverDTO)).thenReturn(TestData.taxii20Server);

        ResponseEntity<TaxiiServer> response = serverResource.createOrUpdateServer(serverDTO);
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
        ResponseEntity<String> response = serverResource.deleteServer(TestData.taxii11Server.getLabel());
        verify(serverService).deleteServer(TestData.taxii11Server.getLabel());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testDeleteServer_ServerNotFoundException() {
        exceptionRule.expect(ServerNotFoundException.class);
        exceptionRule.expectMessage(ErrorConstants.SERVER_NOT_FOUND);
        when(serverService.findOneByLabel(anyString())).thenReturn(null);
        ResponseEntity<String> response = serverResource.deleteServer(null);
    }

    @Test
    public void testRefreshServer() {
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        ResponseEntity<ServerDTO> response = serverResource.refreshServer(TestData.taxii11Server.getLabel());
        verify(serverService).refreshServer(TestData.taxii11Server.getLabel());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testRefreshServer_ServerNotFoundException() {
        exceptionRule.expect(ServerNotFoundException.class);
        exceptionRule.expectMessage(ErrorConstants.SERVER_NOT_FOUND);
        when(serverService.findOneByLabel(anyString())).thenReturn(null);
        ResponseEntity<ServerDTO> response = serverResource.refreshServer(null);
    }

    @Test
    public void testAddServerCredential() {
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        ResponseEntity<ServerDTO> response = serverResource.addServerCredential(
            TestData.taxii11Server.getLabel(),
            new ServerCredentialDTO(TestData.taxii11Server.getLabel(), "user", "password"));
        verify(serverService).addServerCredential(TestData.taxii11Server.getLabel(), "user", "password");
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testAddServerCredential_ServerNotFoundException() {
        exceptionRule.expect(ServerNotFoundException.class);
        exceptionRule.expectMessage(ErrorConstants.SERVER_NOT_FOUND);
        when(serverService.findOneByLabel(anyString())).thenReturn(null);
        ResponseEntity<ServerDTO> response = serverResource.addServerCredential(null, new ServerCredentialDTO());
    }

    @Test
    public void testDeleteServerCredential() {
        PowerMockito.mockStatic(SecurityUtils.class);
        when(SecurityUtils.getCurrentUserLogin()).thenReturn(Optional.of("user"));
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        when(userService.getUserWithAuthoritiesByLogin(anyString())).thenReturn(Optional.ofNullable(TestData.user));
        ResponseEntity<ServerDTO> response = serverResource.deleteServerCredential(TestData.taxii11Server.getLabel());
        verify(serverService).removeServerCredential(TestData.taxii11Server.getLabel());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
