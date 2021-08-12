package gov.dhs.cisa.ctm.flare.client.api.service.dto;

import java.util.HashMap;
import java.util.HashSet;

import gov.dhs.cisa.ctm.flare.client.api.domain.collection.TaxiiCollection;

public class CollectionsDTO {
    private HashMap<String, TaxiiCollection> byId = new HashMap<>();
    private HashSet<String> allIds = new HashSet<>();

    public <T extends TaxiiCollection> CollectionsDTO(HashSet<T> collectionSet) {
        if (collectionSet == null || collectionSet.isEmpty()) {
            return;
        }
        for (T collection: collectionSet) {
            this.byId.put(collection.getId(), collection);
            this.allIds.add(collection.getId());
        }
    }

    public HashMap<String, ? extends TaxiiCollection> getById() {
        return byId;
    }

    public void setById(HashMap<String, TaxiiCollection> byId) {
        this.byId = byId;
    }

    public HashSet<String> getAllIds() {
        return allIds;
    }

    public void setAllIds(HashSet<String> allIds) {
        this.allIds = allIds;
    }
}
