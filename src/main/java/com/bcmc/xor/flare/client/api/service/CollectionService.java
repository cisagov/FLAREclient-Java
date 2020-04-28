package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.domain.collection.Taxii11Collection;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii20Collection;
import com.bcmc.xor.flare.client.api.domain.collection.TaxiiCollection;
import com.bcmc.xor.flare.client.api.repository.CollectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@SuppressWarnings("unused")
@Service
public class CollectionService {

    private static final Logger log = LoggerFactory.getLogger(CollectionService.class);

    private CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public void deleteAll(Iterable<? extends TaxiiCollection> taxiiCollection) {
        collectionRepository.deleteAll(taxiiCollection);
    }

    public Optional<TaxiiCollection> findOneByDisplayName(String displayName) {
        return collectionRepository.findOneByDisplayName(displayName);
    }

    public Optional<TaxiiCollection> findOneById(String id) {
        return collectionRepository.findOneById(id);
    }

    public Optional<Taxii11Collection> findOneTaxii11ById(String id) {
        return collectionRepository.findOneTaxii11ById(id);
    }

    public Optional<Taxii20Collection> findOneTaxii20ById(String id) {
        return collectionRepository.findOneTaxii20ById(id);
    }

    public <T extends TaxiiCollection> T save(T collection) {
        return collectionRepository.save(collection);
    }

    public CollectionRepository getCollectionRepository() {
        return collectionRepository;
    }

    public void setCollectionRepository(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }
}
