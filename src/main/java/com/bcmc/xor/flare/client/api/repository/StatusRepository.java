package com.bcmc.xor.flare.client.api.repository;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.status.Status;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the Status entity.
 */
@Repository(Constants.RepositoryLabels.STATUS)
public interface StatusRepository extends MongoRepository<Status, String> {

    String STATUS_BY_ID_CACHE = "statusById";

    @Cacheable(cacheNames = STATUS_BY_ID_CACHE)
    Optional<Status> findOneById(String id);

    List<Status> findAllByStatusIgnoreCase(String status);


}
