package com.bcmc.xor.flare.client.taxii;

import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii11Collection;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii20Collection;
import com.bcmc.xor.flare.client.api.domain.collection.TaxiiCollection;
import com.bcmc.xor.flare.client.api.domain.server.Taxii11Server;
import com.bcmc.xor.flare.client.api.domain.server.Taxii20Server;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.error.NotFoundException;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20Association;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Objects;

/**
 * An object that associates various other objects with a given TaxiiServer, TaxiiCollection, and User.
 * This logic was being repeated often, so this object was introduced to cut down on some of the repetition there.
 */
public class TaxiiAssociation <Server extends TaxiiServer, Collection extends TaxiiCollection> {

    @DBRef
    private Server server;

    @DBRef
    private Collection collection;

    @DBRef
    private User user;

    protected TaxiiAssociation() {
    }

    protected TaxiiAssociation(Server server, Collection collection, User user) {
        this.server = server;
        this.collection = collection;
        this.user = user;
    }

    public static TaxiiAssociation from(String serverRef, String collectionRef, String userRef, ServerService serverService, CollectionService collectionService, UserService userService) {
        TaxiiServer server = serverService.findOneById(serverRef).orElseThrow(NotFoundException::new);
        TaxiiCollection collection = collectionService.findOneById(collectionRef).orElseThrow(NotFoundException::new);
        User user = userService.getUserWithAuthoritiesByLogin(userRef).orElseThrow(NotFoundException::new);
        switch (server.getVersion()) {
            case TAXII21:
                return new Taxii20Association((Taxii20Server) server, (Taxii20Collection) collection, user);
            case TAXII11:
                return new Taxii11Association((Taxii11Server) server, (Taxii11Collection) collection, user);
            default:
                throw new IllegalStateException("Server does not have a verison: " + server.getId());
        }
    }

    public static TaxiiAssociation from(String label, String collectionId, ServerService serverService, CollectionService collectionService) {
        TaxiiServer server = serverService.findOneByLabel(label).orElseThrow(NotFoundException::new);
        TaxiiCollection collection = collectionService.findOneById(collectionId).orElseThrow(NotFoundException::new);
        switch (server.getVersion()) {
            case TAXII21:
                return new Taxii20Association((Taxii20Server) server, (Taxii20Collection) collection, null);
            case TAXII11:
                return new Taxii11Association((Taxii11Server) server, (Taxii11Collection) collection, null);
            default:
                throw new IllegalStateException("Server does not have a verison: " + server.getId());
        }
    }

    public Server getServer() {
        return server;
    }

    @SuppressWarnings("unused")
    public void setServer(Server server) {
        this.server = server;
    }

    public Collection getCollection() {
        return collection;
    }

    @SuppressWarnings("unused")
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public User getUser() {
        return user;
    }

    @SuppressWarnings("unused")
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxiiAssociation<?, ?> that = (TaxiiAssociation<?, ?>) o;
        return Objects.equals(server, that.server) &&
            Objects.equals(collection, that.collection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, collection);
    }

    @Override
    public String toString() {
        return "TaxiiAssociation{" +
            "server=" + server +
            ", collection=" + collection +
            ", user=" + user +
            '}';
    }
}
