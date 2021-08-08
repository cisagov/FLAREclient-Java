package gov.dhs.cisa.ctm.flare.client.taxii.taxii11;

import gov.dhs.cisa.ctm.flare.client.api.domain.auth.User;
import gov.dhs.cisa.ctm.flare.client.api.domain.collection.Taxii11Collection;
import gov.dhs.cisa.ctm.flare.client.api.domain.server.Taxii11Server;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;

public class Taxii11Association extends TaxiiAssociation<Taxii11Server, Taxii11Collection> {

    public Taxii11Association(Taxii11Server server, Taxii11Collection collection, User user) {
        super(server, collection, user);
    }
}
