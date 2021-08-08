package gov.dhs.cisa.ctm.flare.client.taxii.taxii21;

import gov.dhs.cisa.ctm.flare.client.api.domain.auth.User;
import gov.dhs.cisa.ctm.flare.client.api.domain.collection.Taxii21Collection;
import gov.dhs.cisa.ctm.flare.client.api.domain.server.ApiRoot;
import gov.dhs.cisa.ctm.flare.client.api.domain.server.Taxii21Server;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;

import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Iterator;

public class Taxii21Association extends TaxiiAssociation<Taxii21Server, Taxii21Collection> {

    @DBRef
    private ApiRoot apiRoot;

    public Taxii21Association() {
    }

    public Taxii21Association(Taxii21Server server, Taxii21Collection collection, User user) {
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

    public Taxii21Association(Taxii21Server server, Taxii21Collection collection, User user, ApiRoot apiRoot) {
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
