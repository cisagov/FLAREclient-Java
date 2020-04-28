package com.bcmc.xor.flare.client.api.repository;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.async.RecurringFetch;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository(Constants.RepositoryLabels.RECURRING_FETCH)
public interface RecurringFetchRepository extends MongoRepository<RecurringFetch, String> {
    Optional<RecurringFetch> findByAssociation(TaxiiAssociation association);
}
