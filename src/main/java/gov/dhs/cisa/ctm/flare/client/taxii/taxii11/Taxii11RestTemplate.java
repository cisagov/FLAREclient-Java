package gov.dhs.cisa.ctm.flare.client.taxii.taxii11;

import gov.dhs.cisa.ctm.flare.client.api.domain.server.Taxii11Server;
import gov.dhs.cisa.ctm.flare.client.api.domain.server.TaxiiServer;
import gov.dhs.cisa.ctm.flare.client.api.service.ServerService;
import gov.dhs.cisa.ctm.flare.client.api.service.dto.ServerDTO;
import gov.dhs.cisa.ctm.flare.client.error.ErrorConstants;
import gov.dhs.cisa.ctm.flare.client.error.RequestException;
import gov.dhs.cisa.ctm.flare.client.error.StatusMessageResponseException;
import gov.dhs.cisa.ctm.flare.client.taxii.FlareRestTemplate;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiHeaders;

import org.mitre.taxii.messages.xml11.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Taxii11RestTemplate extends FlareRestTemplate {

    private static final Logger log = LoggerFactory.getLogger(ServerService.class);

    public Taxii11RestTemplate(Environment env) {
        super(env);
        // Setup for message conversion
        HttpMessageConverter xmlConverter = new Jaxb2RootElementHttpMessageConverter();
        List<HttpMessageConverter<?>> taxii11converters = new ArrayList<>();
        taxii11converters.add(new StringHttpMessageConverter());
        taxii11converters.add(xmlConverter);
        this.setMessageConverters(taxii11converters);
        this.setInterceptors(Collections.singletonList(new Taxii11HeaderRequestInterceptor()));
    }

    public DiscoveryResponse submitDiscovery(TaxiiServer server) throws StatusMessageResponseException, RequestException {

        // Create and submit Discovery request
        DiscoveryRequest request = new DiscoveryRequest().withMessageId(MessageHelper.generateMessageId());
        String stringResponse = executePost(request, TaxiiHeaders.fromServer(server), server.getServerInformationUrl()).getBody();

        // Deserialize
        Object responseObject;
        try {
            responseObject = unmarshal(stringResponse);
        } catch (JAXBException e) {
            log.error("Response: {}", stringResponse);
            throw new RequestException("Received a response that could not be parsed", server.getLabel(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }

        // Handle response cases
        if (responseObject instanceof DiscoveryResponse) {
            return (DiscoveryResponse) responseObject;
        } else if (responseObject instanceof StatusMessage) {
            throw new StatusMessageResponseException((StatusMessage) responseObject);
        } else {
            throw new RequestException("Received response of unknown or unexpected type from server", server.getLabel(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }
    }

    public DiscoveryResponse submitDiscovery(ServerDTO serverDTO) throws StatusMessageResponseException, RequestException {
        return submitDiscovery(new Taxii11Server(serverDTO));
    }

    public PollResponse submitPoll(TaxiiServer server, PollRequest request, URI uri) throws StatusMessageResponseException, RequestException {

        // Create and submit Poll request
        String stringResponse = executePost(request, TaxiiHeaders.fromServer(server), uri).getBody();

        // Deserialize
        Object responseObject;
        try {
            responseObject = unmarshal(stringResponse);
        } catch (JAXBException e) {
            log.error("Response: {}", stringResponse);
            throw new RequestException("Received a response that could not be parsed", server.getLabel(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }

        // Handle response cases
        if (responseObject instanceof PollResponse) {
            return (PollResponse) responseObject;
        } else if (responseObject instanceof StatusMessage) {
            throw new StatusMessageResponseException((StatusMessage) responseObject);
        } else {
            throw new RequestException("Received response of unknown or unexpected type from server", server.getLabel(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }
    }

    public StatusMessage submitInbox(TaxiiServer server, InboxMessage inboxMessage, URI uri) throws StatusMessageResponseException, RequestException {

        // Create and submit Poll request
        String stringResponse = executePost(inboxMessage, TaxiiHeaders.fromServer(server), uri).getBody();

        // Deserialize
        Object responseObject;
        try {
            responseObject = unmarshal(stringResponse);
        } catch (JAXBException e) {
            log.error("Response: {}", stringResponse);
            throw new RequestException("Received a response that could not be parsed", server.getLabel(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }

        // Handle response cases
        if (responseObject instanceof StatusMessage) {
            return (StatusMessage) responseObject;
        } else {
            throw new RequestException("Received response of unknown or unexpected type from server", server.getLabel(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }
    }

    public CollectionInformationResponse submitCollectionInformationResponse(TaxiiServer server, CollectionInformationRequest collectionInformationRequest, URI uri) throws StatusMessageResponseException, RequestException {

        // Create and submit Poll request
        String stringResponse = executePost(collectionInformationRequest, TaxiiHeaders.fromServer(server), uri).getBody();

        // Deserialize
        Object responseObject;
        try {
            responseObject = unmarshal(stringResponse);
        } catch (JAXBException e) {
            log.error("Response: {}", stringResponse);
            throw new RequestException("Received a response that could not be parsed", server.getLabel(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }

        // Handle response cases
        if (responseObject instanceof CollectionInformationResponse) {
            return (CollectionInformationResponse) responseObject;
        } else if (responseObject instanceof StatusMessage) {
            throw new StatusMessageResponseException((StatusMessage) responseObject);
        } else {
            throw new RequestException("Received response of unknown or unexpected type from server", server.getLabel(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }
    }

    private Object unmarshal(String stringResponse) throws JAXBException {
        TaxiiXmlFactory txf = new TaxiiXmlFactory();
        TaxiiXml taxiiXml = txf.createTaxiiXml();
        Unmarshaller unmarshaller = taxiiXml.getJaxbContext().createUnmarshaller();
        return unmarshaller.unmarshal(new StringReader(stringResponse));
    }

}
