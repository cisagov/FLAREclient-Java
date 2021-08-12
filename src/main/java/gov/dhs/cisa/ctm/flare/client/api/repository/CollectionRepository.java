package gov.dhs.cisa.ctm.flare.client.api.repository;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.api.domain.collection.Taxii11Collection;
import gov.dhs.cisa.ctm.flare.client.api.domain.collection.Taxii21Collection;
import gov.dhs.cisa.ctm.flare.client.api.domain.collection.TaxiiCollection;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the Server entity.
 */
@SuppressWarnings("unused")
@Repository(Constants.RepositoryLabels.TAXII_COLLECTION)
public interface CollectionRepository extends MongoRepository<TaxiiCollection, String> {

    String COLLECTIONS_BY_ID_CACHE = "collectionsById";

    @Cacheable(cacheNames = COLLECTIONS_BY_ID_CACHE)
    Optional<TaxiiCollection> findOneById(String id);

    @Cacheable(cacheNames = COLLECTIONS_BY_ID_CACHE)
    Optional<Taxii11Collection> findOneTaxii11ById(String id);

    @Cacheable(cacheNames = COLLECTIONS_BY_ID_CACHE)
    Optional<Taxii21Collection> findOneTaxii21ById(String id);

    Optional<TaxiiCollection> findOneByDisplayName(String displayName);

    List<TaxiiCollection> findByDisplayNameIn(List<String> displayNames);
}
