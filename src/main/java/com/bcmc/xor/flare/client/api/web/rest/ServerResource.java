package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.domain.server.TemporaryServer;
import com.bcmc.xor.flare.client.api.security.SecurityUtils;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.api.service.dto.ServerCredentialDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServersDTO;
import com.bcmc.xor.flare.client.error.*;
import com.bcmc.xor.flare.client.util.HeaderUtil;
import com.bcmc.xor.flare.client.util.ResponseUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api")
class ServerResource {

    private static final Logger log = LoggerFactory.getLogger(ServerResource.class);
    private final ServerService serverService;
    private final UserService userService;

    public ServerResource(ServerService serverService, UserService userService) {
        this.serverService = serverService;
        this.userService = userService;
    }

    @PostMapping("/servers")
    public ResponseEntity<TaxiiServer> createOrUpdateServer(@Valid @RequestBody ServerDTO serverDTO) throws URISyntaxException, InternalServerErrorException {

        // Check basic auth credentials; add them to ServerCredentialsUtils map
        if (serverDTO.getRequiresBasicAuth()) {
            Map<String, Object> badParameterMap = new HashMap<>();
            if (StringUtils.isBlank(serverDTO.getUsername())) {
                badParameterMap.put("userName", ErrorConstants.USERNAME_REQUIRED_PARAM);
            }
            if (StringUtils.isBlank(serverDTO.getPassword())) {
                badParameterMap.put("password", ErrorConstants.PASSWORD_REQUIRED_PARAM);
            }
            if (!badParameterMap.isEmpty()) {throw new FlareClientIllegalArgumentException(badParameterMap);}

            serverService.addServerCredential(serverDTO.getLabel(), serverDTO.getUsername(), serverDTO.getPassword());

        }

        TaxiiServer server = serverService.updateServer(serverDTO);
        if (server instanceof TemporaryServer) {
            return new ResponseEntity(String.format("'%s' failed creation. Ensure server details are correct.", serverDTO.getLabel()), HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<String> deleteServer(@PathVariable String label) {
        log.debug("REST Request to delete server for '{}'", label);
        if (!serverService.findOneByLabel(label).isPresent()) {
            return new ResponseEntity<>("Unable to find server '" + label + "' for deletion", HttpStatus.BAD_REQUEST);
        }
        serverService.deleteServer(label);
        return new ResponseEntity<>("Successfully deleted server '" + label + "'", HttpStatus.OK);
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
        Optional<String> optCurrentUser = SecurityUtils.getCurrentUserLogin();
        String currentUser = "";
        if (optCurrentUser.isPresent()) {
            currentUser = optCurrentUser.get();
        }
        log.debug("REST Request to delete server credential for user '{}' and server '{}'", currentUser, label);
        Optional<User> user = userService.getUserWithAuthoritiesByLogin(currentUser);
        if (!user.isPresent()) {throw new UserNotFoundException();}
        if (user.get().getServerCredentials().isEmpty()) {throw new ServerCredentialsNotFoundException();}
        serverService.removeServerCredential(label);
        serverService.refreshServer(label);
        return getServer(label);
    }
}
