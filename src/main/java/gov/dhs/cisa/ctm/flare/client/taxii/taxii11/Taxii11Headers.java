package gov.dhs.cisa.ctm.flare.client.taxii.taxii11;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiHeaders;

import org.springframework.http.MediaType;

public class Taxii11Headers extends TaxiiHeaders {
    public Taxii11Headers() {
        this.set("Accept", MediaType.APPLICATION_XML_VALUE);
        this.set("Content-Type", MediaType.APPLICATION_XML_VALUE);
        this.set("X-TAXII-Content-Type", Constants.HEADER_TAXII11_MESSAGE_BINDING);
        this.set("X-TAXII-Accept", Constants.HEADER_TAXII11_ACCEPT);
        this.set("X-TAXII-Services", Constants.HEADER_TAXII11_SERVICES);
        this.set("X-TAXII-Protocol", Constants.HEADER_TAXII11_PROTOCOL);
    }
}
