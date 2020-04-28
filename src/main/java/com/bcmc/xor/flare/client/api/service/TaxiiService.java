package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11RestTemplate;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20RestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@SuppressWarnings("unused")
@Service
public class TaxiiService {

    private Taxii11RestTemplate taxii11RestTemplate;

    private Taxii20RestTemplate taxii20RestTemplate;

    public TaxiiService(Environment environment) {
        this.taxii11RestTemplate = new Taxii11RestTemplate(environment);
        this.taxii20RestTemplate = new Taxii20RestTemplate(environment);
    }

    public Taxii11RestTemplate getTaxii11RestTemplate() {
        return taxii11RestTemplate;
    }

    public void setTaxii11RestTemplate(Taxii11RestTemplate taxii11RestTemplate) {
        this.taxii11RestTemplate = taxii11RestTemplate;
    }

    public Taxii20RestTemplate getTaxii20RestTemplate() {
        return taxii20RestTemplate;
    }

    public void setTaxii20RestTemplate(Taxii20RestTemplate taxii20RestTemplate) {
        this.taxii20RestTemplate = taxii20RestTemplate;
    }
}
