package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.domain.server.TemporaryServer;
import com.bcmc.xor.flare.client.api.security.SecurityUtils;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.dto.ServerCredentialDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServersDTO;
import com.bcmc.xor.flare.client.error.BadRequestAlertException;
import com.bcmc.xor.flare.client.error.ErrorConstants;
import com.bcmc.xor.flare.client.error.InternalServerErrorException;
import com.bcmc.xor.flare.client.util.HeaderUtil;
import com.bcmc.xor.flare.client.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api")
class ServerResource {

    private static final Logger log = LoggerFactory.getLogger(ServerResource.class);
    private final ServerService serverService;

    public ServerResource(ServerService serverService) {
        this.serverService = serverService;
    }

    @PostMapping("/servers")
    public ResponseEntity<TaxiiServer> createOrUpdateServer(@Valid @RequestBody ServerDTO serverDTO) throws URISyntaxException, InternalServerErrorException {

        // Check basic auth credentials; add them to ServerCredentialsUtils map
        if (serverDTO.getRequiresBasicAuth()) {
            if (serverDTO.getUsername() == null || serverDTO.getPassword() == null
                || serverDTO.getUsername().isEmpty() || serverDTO.getPassword().isEmpty()) {
                throw new BadRequestAlertException("Server requires basic auth but username and/or password not supplied",
                    serverDTO.getLabel(), ErrorConstants.ERR_BAD_REQUEST);
            } else {
                serverService.addServerCredential(serverDTO.getLabel(), serverDTO.getUsername(), serverDTO.getPassword());
            }
        }

        TaxiiServer server = serverService.updateServer(serverDTO);
        if (server instanceof TemporaryServer) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(String.format("'%s' failed creation. Ensure server details are correct.", serverDTO.getLabel()), serverDTO.getLabel())).body(null);
        }
        return ResponseEntity
            .created(new URI("/api/servers/" + server.getId()))
            .headers(HeaderUtil.createAlert( String.format("'%s' created or updated", server.getLabel()), server.getLabel()))
            .body(server);

    }

    @GetMapping("/servers")
    public ResponseEntity<ServersDTO> getAllServers() {
        return new ResponseEntity<>(serverService.getAllServers(), HttpStatus.OK);
    }

    @GetMapping("/servers/{label}")
    public ResponseEntity<ServerDTO> getServer(@PathVariable String label) {
        return ResponseUtil.wrapOrNotFound(serverService.findOneByLabel(label).map(ServerDTO::new), null);
    }

    @DeleteMapping("/servers/{label}")
    public ResponseEntity<Void> deleteServer(@PathVariable String label) {
        serverService.deleteServer(label);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert(String.format("Deleted server '%s'", label), label)).build();
    }

    @PostMapping("/servers/{label}/refresh")
    public ResponseEntity<ServerDTO> refreshServer(@PathVariable String label) {
        log.debug("Refreshing server information for '{}'", label);
        serverService.refreshServer(label);
        return getServer(label);
    }

    @PutMapping("/servers/{label}/credentials")
    public ResponseEntity<ServerDTO> addServerCredential(@PathVariable String label, @RequestBody ServerCredentialDTO serverCredential) {
        log.debug("REST Request to add server credential for user '{}' and server '{}'", SecurityUtils.getCurrentUserLogin(), label);
        serverService.addServerCredential(label, serverCredential.getUsername(), serverCredential.getPassword());
        serverService.refreshServer(label);
        return getServer(label);
    }

    @DeleteMapping("/servers/{label}/credentials")
    public ResponseEntity<ServerDTO> deleteServerCredential(@PathVariable String label) {
        log.debug("REST Request to delete server credential for user '{}' and server '{}'", SecurityUtils.getCurrentUserLogin(), label);
        serverService.removeServerCredential(label);
        serverService.refreshServer(label);
        return getServer(label);
    }
}
