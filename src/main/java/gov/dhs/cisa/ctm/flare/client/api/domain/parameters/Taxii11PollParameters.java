package gov.dhs.cisa.ctm.flare.client.api.domain.parameters;

import com.fasterxml.jackson.annotation.JsonTypeName;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.error.InternalServerErrorException;

import org.mitre.taxii.messages.xml11.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TAXII 1.1 Fetch parameters
 */
@JsonTypeName("TAXII11")
public class Taxii11PollParameters extends ApiParameters implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Taxii11PollParameters.class);

    @Field("content_bindings")
    private List<String> contentBindings;

    @Field("start_date")
    private ZonedDateTime startDate;

    @Field("end_date")
    private ZonedDateTime endDate;

    public Taxii11PollParameters() {
    }

    private List<String> getContentBindings() {
        if (contentBindings == null) {
            return new ArrayList<>(Constants.ContentBindings.contentBindingMap.keySet());
        } else {
            return contentBindings;
        }
    }

    public void setContentBindings(List<String> contentBindings) {
        this.contentBindings = contentBindings;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    private XMLGregorianCalendar getStartGregorian() {
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
        return datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(getStartDate()));
    }

    private XMLGregorianCalendar getEndGregorian() {
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
        return datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(getEndDate()));
    }

    public PollRequest buildPollRequestForCollection(String collectionName) {
        log.debug("Building poll request for collection '{}'", collectionName);
        PollParametersType pollParametersType = new PollParametersType();
        pollParametersType.withContentBindings(this.getContentBindings().stream()
            .map(contentBinding -> new ContentBindingIDType(null, Constants.ContentBindings.contentBindingMap.get(contentBinding)))
            .collect(Collectors.toList()));
        pollParametersType.withResponseType(ResponseTypeEnum.FULL);

        return new PollRequest(
            null,
            MessageHelper.generateMessageId(),
            this.getStartGregorian(),
            this.getEndGregorian(),
            pollParametersType,
            null,
            null,
            collectionName);
    }
}
