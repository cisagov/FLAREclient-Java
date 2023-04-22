package com.bcmc.xor.flare.client.api.repository;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.server.Taxii11Server;
import com.bcmc.xor.flare.client.api.domain.server.Taxii21Server;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the Server entity.
 */
@SuppressWarnings("unused")
@Repository(Constants.RepositoryLabels.SERVER)
public interface ServerRepository extends MongoRepository<TaxiiServer, String> {

    String SERVERS_BY_LABEL_CACHE = "serversByLabel";
    String SERVERS_BY_ID_CACHE = "serversById";

    @Cacheable(cacheNames = SERVERS_BY_ID_CACHE)
    Optional<TaxiiServer> findOneById(String id);

    @Cacheable(cacheNames = SERVERS_BY_LABEL_CACHE)
    Optional<TaxiiServer> findOneByLabelIgnoreCase(String label);

    List<TaxiiServer> findAllByLabelIgnoreCase(String label);

    @Cacheable(cacheNames = SERVERS_BY_ID_CACHE)
    Optional<Taxii11Server> findOneTaxii11ById(String id);

    @Cacheable(cacheNames = SERVERS_BY_LABEL_CACHE)
    Optional<Taxii11Server> findOneTaxii11ByLabelIgnoreCase(String label);

    @Cacheable(cacheNames = SERVERS_BY_ID_CACHE)
    Optional<Taxii21Server> findOneTaxii21ById(String id);

    @Cacheable(cacheNames = SERVERS_BY_LABEL_CACHE)
    Optional<Taxii21Server> findOneTaxii21ByLabelIgnoreCase(String label);

    boolean existsByLabelIgnoreCase(String label);

}
