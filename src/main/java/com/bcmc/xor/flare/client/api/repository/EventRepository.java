package com.bcmc.xor.flare.client.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.audit.Event;

/**
 * Spring Data MongoDB repository for the Event entity.
 */
@Repository(Constants.RepositoryLabels.EVENT)
public interface EventRepository extends MongoRepository<Event, String> {

    String EVENTS_BY_ID_CACHE = "eventsById";

    @Cacheable(cacheNames = EVENTS_BY_ID_CACHE)
    Optional<Event> findOneById(String id);

    List<Event> findAllByServer(String server);

    Page<Event> findByTaxiiCollectionAndServer(Pageable pageable, String taxiiCollection, String server);
    
    void deleteByServer(String server);

}
