package gov.dhs.cisa.ctm.flare.client.api.repository;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.api.domain.async.RecurringFetch;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository(Constants.RepositoryLabels.RECURRING_FETCH)
public interface RecurringFetchRepository extends MongoRepository<RecurringFetch, String> {
    Optional<RecurringFetch> findByAssociation(TaxiiAssociation association);
    
    void deleteAllByApiParametersServerLabelEquals(String serverLabel); 
}
