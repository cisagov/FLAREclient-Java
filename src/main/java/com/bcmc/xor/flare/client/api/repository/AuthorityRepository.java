package com.bcmc.xor.flare.client.api.repository;

import com.bcmc.xor.flare.client.api.domain.auth.Authority;
import com.bcmc.xor.flare.client.api.config.Constants;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Authority entity.
 */
@Repository(Constants.RepositoryLabels.AUTHORITY)
public interface AuthorityRepository extends MongoRepository<Authority, String> {
}
