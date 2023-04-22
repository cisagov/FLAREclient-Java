package com.bcmc.xor.flare.client.api.service.scheduled.async;

import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.error.AuthenticationFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Service
public class AsyncServerInfoUpdateService {

    private static final Logger log = LoggerFactory.getLogger(AsyncServerInfoUpdateService.class);

    private ServerService serverService;

    private UserService userService;

    public AsyncServerInfoUpdateService(ServerService serverService, UserService userService) {
        this.serverService = serverService;
        this.userService = userService;
    }

    @Async("taskExecutor")
    @Scheduled(fixedRateString = "${flare.server-info-update-interval}")
    public synchronized void asyncServerInformationUpdates() {

        List<String> usersToRemove = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : ServerCredentialsUtils.getInstance().getServerCredentialsMap().entrySet()) {
            for (String serverLabel : entry.getValue().keySet()) {
                if (serverService.exists(serverLabel)) {
                    try {
                        serverService.refreshServer(serverLabel);
                    } catch (AuthenticationFailureException e) {
                        userService.getUserWithAuthoritiesByLogin(entry.getKey()).ifPresent(user -> {
                            log.warn("Got an authentication failure performing an async update with credentials... deleting these credentials because they're invalid");
                            user.getServerCredentials().remove(serverLabel);
                            usersToRemove.add(user.getLogin());
                            userService.updateUser(user);
                        });
                    }
                }
            }
        }
        for (String user : usersToRemove) {
            ServerCredentialsUtils.getInstance().clearCredentialsForUser(user);
        }
    }


    public ServerService getServerService() {
        return serverService;
    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
