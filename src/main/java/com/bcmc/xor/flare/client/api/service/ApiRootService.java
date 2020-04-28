package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.domain.server.ApiRoot;
import com.bcmc.xor.flare.client.api.repository.ApiRootRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@SuppressWarnings("unused")
@Service
public class ApiRootService {

    private ApiRootRepository apiRootRepository;

    public ApiRootService(ApiRootRepository apiRootRepository) {
        this.apiRootRepository = apiRootRepository;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ApiRoot save(ApiRoot apiRoot) {
        return apiRootRepository.save(apiRoot);
    }

    public void deleteAll(Iterable<ApiRoot> apiRoots) {
        apiRootRepository.deleteAll(apiRoots);
    }

    public ApiRootRepository getApiRootRepository() {
        return apiRootRepository;
    }

    public void setApiRootRepository(ApiRootRepository apiRootRepository) {
        this.apiRootRepository = apiRootRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(ApiRootService.class);

}
