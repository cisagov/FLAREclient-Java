package com.bcmc.xor.flare.client.taxii.taxii11;

import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii11Collection;
import com.bcmc.xor.flare.client.api.domain.server.Taxii11Server;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;

public class Taxii11Association extends TaxiiAssociation<Taxii11Server, Taxii11Collection> {

    public Taxii11Association(Taxii11Server server, Taxii11Collection collection, User user) {
        super(server, collection, user);
    }
}
