package gov.dhs.cisa.ctm.flare.client.api.repository;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.api.domain.auth.Authority;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Authority entity.
 */
@Repository(Constants.RepositoryLabels.AUTHORITY)
public interface AuthorityRepository extends MongoRepository<Authority, String> {
}
