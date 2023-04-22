package com.bcmc.xor.flare.client.api.repository;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.server.ApiRoot;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for the Server entity.
 */
@SuppressWarnings("unused")
@Repository(Constants.RepositoryLabels.API_ROOT)
public interface ApiRootRepository extends MongoRepository<ApiRoot, String> {

    Optional<TaxiiServer> findOneById(String id);

}
