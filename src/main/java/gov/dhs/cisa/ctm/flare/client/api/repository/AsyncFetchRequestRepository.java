package gov.dhs.cisa.ctm.flare.client.api.repository;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.api.domain.async.AsyncFetch;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Repository(Constants.RepositoryLabels.ASYNC_FETCH)
public interface AsyncFetchRequestRepository extends MongoRepository<AsyncFetch, String> {

	// FIND Methods
    Optional<AsyncFetch> findOneById(String id);

    Optional<AsyncFetch> findOneByAssociationAndStatus(TaxiiAssociation association, AsyncFetch.Status status);

    List<AsyncFetch> findAllByStatus(AsyncFetch.Status status);

	// DELETE Method
    void deleteAllByInitialFetchParamsServerLabelEquals(String serverLabel); 
}
