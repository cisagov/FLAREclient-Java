package com.bcmc.xor.flare.client.taxii.taxii20;

import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii20Collection;
import com.bcmc.xor.flare.client.api.domain.server.ApiRoot;
import com.bcmc.xor.flare.client.api.domain.server.Taxii20Server;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Iterator;

public class Taxii20Association extends TaxiiAssociation<Taxii20Server, Taxii20Collection> {

    @DBRef
    private ApiRoot apiRoot;

    public Taxii20Association() {
    }

    public Taxii20Association(Taxii20Server server, Taxii20Collection collection, User user) {
        super(server, collection, user);
        if (server.getApiRootObjects() != null && !server.getApiRootObjects().isEmpty()) {
            Iterator<ApiRoot> apiRootIterator = server.getApiRootObjects().iterator();
            while (apiRootIterator.hasNext() && this.getApiRoot() == null) {
                ApiRoot apiRoot = apiRootIterator.next();
                if (apiRoot.getEndpoint().equals(collection.getApiRootRef())) {
                    this.setApiRoot(apiRoot);
                }
            }
        }
    }

    public Taxii20Association(Taxii20Server server, Taxii20Collection collection, User user, ApiRoot apiRoot) {
        super(server, collection, user);
        this.setApiRoot(apiRoot);
    }

    public ApiRoot getApiRoot() {
        return apiRoot;
    }

    public void setApiRoot(ApiRoot apiRoot) {
        this.apiRoot = apiRoot;
    }



}
