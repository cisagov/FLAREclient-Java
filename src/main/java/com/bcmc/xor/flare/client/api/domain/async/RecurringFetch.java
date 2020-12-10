package com.bcmc.xor.flare.client.api.domain.async;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.parameters.ApiParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii21GetParameters;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = Constants.RepositoryLabels.RECURRING_FETCH)
public class RecurringFetch<Params extends ApiParameters> {

    @Id
    private String id;

    private TaxiiAssociation association;
    private Instant begin;
    private int window;
    private Params apiParameters;

    public RecurringFetch() {
    }

    public RecurringFetch(TaxiiAssociation taxiiAssociation, Instant begin, int window, Params apiParameters) {
        this.association = taxiiAssociation;
        this.begin = begin;
        this.window = window;
        this.apiParameters = apiParameters;
    }

    public RecurringFetch(Params apiParameters) {
        this.association = apiParameters.getAssociation();
        if (apiParameters instanceof Taxii21GetParameters) {
            this.begin = ((Taxii21GetParameters) apiParameters).getAddedAfter().toInstant();
        } else if (apiParameters instanceof Taxii11PollParameters) {
            this.begin = ((Taxii11PollParameters) apiParameters).getStartDate().toInstant();
        }
        this.window = apiParameters.getWindow();
        this.apiParameters = apiParameters;
    }

    public TaxiiAssociation getAssociation() {
        return association;
    }

    public void setAssociation(TaxiiAssociation association) {
        this.association = association;
    }

    public Instant getBegin() {
        return begin;
    }

    public void setBegin(Instant begin) {
        this.begin = begin;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public Params getApiParameters() {
        return apiParameters;
    }

    @SuppressWarnings("unused")
    public void setApiParameters(Params apiParameters) {
        this.apiParameters = apiParameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RecurringFetch{" +
            "id='" + id + '\'' +
            ", association=" + association +
            ", begin=" + begin +
            ", window=" + window +
            ", apiParameters=" + apiParameters +
            '}';
    }
}
