package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11RestTemplate;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21RestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@SuppressWarnings("unused")
@Service
public class TaxiiService {

    private Taxii11RestTemplate taxii11RestTemplate;

    private Taxii21RestTemplate taxii21RestTemplate;

    public TaxiiService(Environment environment) {
        this.taxii11RestTemplate = new Taxii11RestTemplate(environment);
        this.taxii21RestTemplate = new Taxii21RestTemplate(environment);
    }

    public Taxii11RestTemplate getTaxii11RestTemplate() {
        return taxii11RestTemplate;
    }

    public void setTaxii11RestTemplate(Taxii11RestTemplate taxii11RestTemplate) {
        this.taxii11RestTemplate = taxii11RestTemplate;
    }

    public Taxii21RestTemplate getTaxii21RestTemplate() {
        return taxii21RestTemplate;
    }

    public void setTaxii21RestTemplate(Taxii21RestTemplate taxii21RestTemplate) {
        this.taxii21RestTemplate = taxii21RestTemplate;
    }
}
