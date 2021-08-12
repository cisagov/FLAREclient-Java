package gov.dhs.cisa.ctm.flare.client.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.api.domain.content.AbstractContentWrapper;
import gov.dhs.cisa.ctm.flare.client.api.domain.content.Stix1ContentWrapper;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;

@SuppressWarnings("unused")
@Repository(Constants.RepositoryLabels.CONTENT)
public interface ContentRepository extends MongoRepository<AbstractContentWrapper, String> {

	@Query(fields = "{content_object: 0, association: 0}")
	Page<AbstractContentWrapper> findAllByAssociation(Pageable pageable, TaxiiAssociation association);

	// Will return content object!
	@Query(fields = "{association: 0}")
	Optional<AbstractContentWrapper> findByIdAndAssociation(String id, TaxiiAssociation association);

	Long countByAssociation(TaxiiAssociation association);

	@Query(value = "{ 'validation_result.status' : \"PENDING\" }")
	List<AbstractContentWrapper> findAllPending();

	@Query(value = "{ 'validation_result.status' : \"PENDING\", '_class': \"gov.dhs.cisa.ctm.flare.client.api.domain.content.Stix1ContentWrapper\"}")
	List<Stix1ContentWrapper> findAllPending11Content();

	void deleteByAssociation(TaxiiAssociation association);

}
