package com.bcmc.xor.flare.client.api.domain.server;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii11Collection;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import org.mitre.taxii.messages.xml11.ServiceInstanceType;
import org.mitre.taxii.messages.xml11.ServiceTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;

@Document(collection = Constants.RepositoryLabels.SERVER)
public class Taxii11Server extends TaxiiServer {

    private static final Logger log = LoggerFactory.getLogger(Taxii11Server.class);

    @Field("service_instances")
    private HashSet<ServiceInstanceType> serviceInstances;

    @DBRef
    @Field("collection")
    private HashSet<Taxii11Collection> collections;

    @Field("has_received_subscription_information")
    private boolean hasReceivedSubscriptionInformation = false;

    @Field("last_received_subscription_information")
    private Instant lastReceivedSubscriptionInformation = null;

    public Taxii11Server() {
        setVersion(Constants.TaxiiVersion.TAXII11);
    }

    public Taxii11Server(ServerDTO serverDTO) {
        super(serverDTO);
        setVersion(Constants.TaxiiVersion.TAXII11);
    }

    public boolean isHasReceivedSubscriptionInformation() {
        return hasReceivedSubscriptionInformation;
    }

    public void setHasReceivedSubscriptionInformation(boolean hasReceivedSubscriptionInformation) {
        this.hasReceivedSubscriptionInformation = hasReceivedSubscriptionInformation;
    }

    public Instant getLastReceivedSubscriptionInformation() {
        return lastReceivedSubscriptionInformation;
    }

    public void setLastReceivedSubscriptionInformation(Instant lastReceivedSubscriptionInformation) {
        this.lastReceivedSubscriptionInformation = lastReceivedSubscriptionInformation;
    }

    public HashSet<ServiceInstanceType> getServiceInstances() {
        return serviceInstances;
    }

    @SuppressWarnings("unused")
    public HashSet<ServiceInstanceType> getServiceInstances(ServiceTypeEnum serviceTypeEnum) {
        return serviceInstances;
    }

    public void setServiceInstances(HashSet<ServiceInstanceType> serviceInstances) {
        this.serviceInstances = serviceInstances;
    }

    public HashSet<Taxii11Collection> getCollections() {
        return collections;
    }

    public Taxii11Collection getCollectionByName(String collectionName) {
        for (Taxii11Collection collection : collections) {
            if (collection.getName().equals(collectionName))
                return collection;
        }
        return null;
    }

    public void setCollections(HashSet<Taxii11Collection> collections) {
        this.collections = collections;
    }

    @Override
    public Constants.TaxiiVersion getVersion() {
        return Constants.TaxiiVersion.TAXII11;
    }

    @Override
    public URI getServerInformationUrl() {
        return this.getUrl();
    }

    @Override
    public String toString() {
        return "Taxii11Server{" +
            "serviceInstances=" + serviceInstances +
            ", collections=" + collections +
            ", hasReceivedSubscriptionInformation=" + hasReceivedSubscriptionInformation +
            ", lastReceivedSubscriptionInformation=" + lastReceivedSubscriptionInformation +
            "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxiiServer that = (TaxiiServer) o;
        return Objects.equals(getId(), that.getId()) &&
            Objects.equals(getLabel(), that.getLabel());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getLabel());
    }
}
