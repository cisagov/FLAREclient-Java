package com.bcmc.xor.flare.client.taxii.taxii21;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.taxii.TaxiiHeaders;

public class Taxii21Headers extends TaxiiHeaders {
    public Taxii21Headers() {
        this.add("Accept", Constants.HEADER_TAXII21_JSON_VERSION_21);
        this.add("Content-Type", Constants.HEADER_TAXII21_JSON_VERSION_21);
    }
}
