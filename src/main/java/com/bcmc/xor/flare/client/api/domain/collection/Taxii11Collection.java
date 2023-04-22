package com.bcmc.xor.flare.client.api.domain.collection;

import com.bcmc.xor.flare.client.api.config.Constants;
import org.mitre.taxii.messages.xml11.CollectionRecordType;
import org.mitre.taxii.messages.xml11.SubscriptionRecordType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * TAXII 1.1 Implementation of a TaxiiCollection
 */
@Document(collection = Constants.RepositoryLabels.TAXII_COLLECTION)
public class Taxii11Collection extends TaxiiCollection {

    private static final Logger log = LoggerFactory.getLogger(Taxii11Collection.class);

    @Field("collection_object")
    private CollectionRecordType collectionObject;

    @Field("subscription_information")
    private List<SubscriptionRecordType> subscriptionInformation;

    public Taxii11Collection(CollectionRecordType collectionObject, String serverRef) {
        this.collectionObject = collectionObject;
        this.setServerRef(serverRef);
        this.setDisplayName(collectionObject.getCollectionName());
        this.setTaxiiVersion(Constants.TaxiiVersion.TAXII11);
    }

    public CollectionRecordType getCollectionObject() {
        return collectionObject;
    }

    public void setCollectionObject(CollectionRecordType collectionObject) {
        this.collectionObject = collectionObject;
    }

    public List<SubscriptionRecordType> getSubscriptionInformation() {
        return subscriptionInformation;
    }

    public void setSubscriptionInformation(List<SubscriptionRecordType> subscriptionInformation) {
        this.subscriptionInformation = subscriptionInformation;
    }

    public String getName() {
        return this.collectionObject.getCollectionName();
    }
}
