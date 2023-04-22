package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.api.repository.StatusRepository;
import com.bcmc.xor.flare.client.api.service.dto.StatusDTO;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Status service
 */
@SuppressWarnings("unused")
@Service
public class StatusService {
    private static final Logger log = LoggerFactory.getLogger(StatusService.class);

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

	/**
	 * Delete items associated with the specified server
	 * 
	 * @param server the server label
	 */
	public void deleteByServer(String server) {
		log.info("Deleting Status objects for server '{}'", server);
		logCount("Starting to delete status objects for server " + server);

		// Note that statusRepository.deleteAllByAssociationServerLabelEquals(server);
		// throws exception because "Associations can only be pointed to directly or via
		// their id property!"

		try {
			List<Status> statusObjectsAll = statusRepository.findAll();
			log.info("Count of statusObjectsAll status objects found: {}", statusObjectsAll.size());
			for (Status status : statusObjectsAll) {
				log.debug("Found Status  status = {}", status);
				log.debug("status.getAssociation().getServer().getId() = [{}] ",
						status.getAssociation().getServer().getId());
				log.debug("status.getAssociation().getServer().getLabel() = [{}] ",
						status.getAssociation().getServer().getLabel());
				log.debug("server = [{}] ", server);
				if (server.equals(status.getAssociation().getServer().getLabel())) {
					log.debug("Found Status object-with-match-for server '{}' status = {}", server, status);
					log.debug("Clear cache for the above  Status  ");
					clearStatusCaches(status);
					statusRepository.deleteById(status.getId());
				}
			}
		} catch (Exception e) {
			log.warn("Exception while deleting status objects for server: " + server, e);
		}

		logCount("Finished deleting status objects for server " + server);
	}

	/**
	 *
	 * Clear error count for status fetch. This can be used to resume
	 * fetching statuses after failing to retrieve them.
	 *
	 * @param id
	 */
	public boolean clearErrorCount(String id)  {
		Optional<Status> possibleStatus = statusRepository.findOneById(id);
		if (possibleStatus.isPresent()) {
			Status status = possibleStatus.get();
			status.setErrorCount(0);
			log.info("[ ] Setting error count to 0 for status(id={}.", status.getId());
			statusRepository.save(status);
			log.info("[*] Set error count to 0 for status(id={}).", status.getId());
			return true;
		} else {
			return false;
		}
	}

	// method is for debugging
	public void logCount(String message) {
		try {
			List<Status> statusObjectsAll = statusRepository.findAll();
			log.info("{} Count of all status objects found: {}", message, statusObjectsAll.size());
		} catch (Exception e) {
			log.debug(message, e);
		}
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
