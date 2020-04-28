package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.api.repository.StatusRepository;
import com.bcmc.xor.flare.client.api.service.dto.StatusDTO;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Status service
 */
@SuppressWarnings("unused")
@Service
public class StatusService {

    private StatusRepository statusRepository;

    private CacheManager cacheManager;

    public StatusService(StatusRepository statusRepository, CacheManager cacheManager) {
        this.statusRepository = statusRepository;
        this.cacheManager = cacheManager;
    }

    public List<Status> getPending() {
        return statusRepository.findAllByStatusIgnoreCase("pending");
    }

    public Optional<Status> getStatusById(String id) {
        return statusRepository.findOneById(id);
    }

    public Page<StatusDTO> getAllStatus(Pageable pageable) {
        return statusRepository.findAll(pageable).map(StatusDTO::new);
    }
    public Status save(Status status) {
        if (status != null) {
            statusRepository.save(status);
            this.clearStatusCaches(status);
        }
        return status;
    }

    private void clearStatusCaches(Status status) {
        Objects.requireNonNull(cacheManager.getCache(StatusRepository.STATUS_BY_ID_CACHE)).evict(status.getId());
    }

    public StatusRepository getStatusRepository() {
        return statusRepository;
    }

    public void setStatusRepository(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
}
