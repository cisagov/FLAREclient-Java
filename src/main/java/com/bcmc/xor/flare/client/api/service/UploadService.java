package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.parameters.UploadedFile;
import com.bcmc.xor.flare.client.api.domain.server.ApiRoot;
import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.mitre.taxii.messages.xml11.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import xor.flare.utils.util.DocumentUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Upload service
 */
@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private EventService eventService;

    private TaxiiService taxiiService;

    private StatusService statusService;

    private ServerService serverService;

    private CollectionService collectionService;

    private UserService userService;

    public UploadService(EventService eventService, TaxiiService taxiiService, StatusService statusService, ServerService serverService, CollectionService collectionService, UserService userService) {
        this.eventService = eventService;
        this.taxiiService = taxiiService;
        this.statusService = statusService;
        this.serverService = serverService;
        this.collectionService = collectionService;
        this.userService = userService;
    }

    public String publish(TaxiiAssociation association, Map<String, UploadedFile> fileMap) {
        switch (association.getServer().getVersion()) {
            case TAXII21:
                return publish((Taxii21Association) association, fileMap);
            case TAXII11:
                return publish((Taxii11Association) association, fileMap);
            default:
                throw new IllegalStateException("Tried to perform a publish with a server that does not have a version");
        }
    }

    public String publish(Taxii11Association association, Map<String, UploadedFile> fileMap) {

        URI url = getUploadUrl(association);

        List<ContentBlock> contentBlocks = new ArrayList<>();

        for (Map.Entry<String, UploadedFile> uploadedFile: fileMap.entrySet()) {
            if (uploadedFile.getValue().getContent().isEmpty()) {
                continue;
            }

            org.w3c.dom.Document document = null;
            try {
                document = DocumentUtils.stringToDocument(uploadedFile.getValue().getContent());
            } catch (IOException | SAXException e) {
                log.error(e.getMessage());
            }

            if (document == null) {
                eventService.createEvent(EventType.PUBLISH_FAILED, String.format("Failed to parse XML file %s.", uploadedFile.getKey()),
                    association);
                continue;
            }

            ContentBlock contentBlock = new ContentBlock();
            contentBlock.withContent(new AnyMixedContentType(Collections.singletonList(uploadedFile.getValue().getContent())));
            // Establish content binding for this package
            ContentInstanceType contentInstanceType = new ContentInstanceType(null,
                // If package has "version", get it, otherwise, default to 1.1.1 (because we have to validate against some schema, so we'll assume most common 1X data)
                Constants.ContentBindings.contentBindingMap.get(document.getDocumentElement().hasAttribute("version") ?
                    document.getDocumentElement().getAttribute("version") :
                    "1.1.1"));
            contentBlock.withContentBinding(contentInstanceType);
            // Set timestamp label from document, or don't
            contentBlock.withTimestampLabel(document.getDocumentElement().hasAttribute("version") ?
                new XMLGregorianCalendarImpl(GregorianCalendar.from(ZonedDateTime.parse(document.getDocumentElement().getAttribute("timestamp")))) : null);
            // Add to list of content blocks
            contentBlocks.add(contentBlock);
        }

        InboxMessage inboxMessage = (new InboxMessage())
            .withMessageId(MessageHelper.generateMessageId())
            .withDestinationCollectionNames(association.getCollection().getName())
            .withContentBlocks(contentBlocks)
            .withRecordCount((new RecordCountType()).withValue(BigInteger.valueOf((long)contentBlocks.size())));

        log.debug("Attempting to publish to server '{}', collection '{}' at '{}'", association.getServer().getLabel(),
            association.getCollection().getDisplayName(), url.toString());
        // Make exchange and attempt to get response as TAXII 1.1 StatusMessage, return feedback to the front-end based on response
        StatusMessage response = taxiiService.getTaxii11RestTemplate().submitInbox(association.getServer(), inboxMessage, url);

        log.info("Received Status Message in response to TAXII 1.1 publish");
        log.info("Status Message: {} - {}", response.getStatusType(), response.getMessage());
        return response.getMessage();
    }

    public String publish(Taxii21Association association, Map<String, UploadedFile> fileMap) {
        URI url = getUploadUrl(association);
        for (Map.Entry<String, UploadedFile> uploadedFile: fileMap.entrySet()) {
            String bundle = uploadedFile.getValue().getContent();
            Status response;
            response = taxiiService.getTaxii21RestTemplate().postBundle(association.getServer(), url, bundle);
            if (response == null) {
                String feedback = String.format("Failed to published %d bundle(s).", fileMap.values().size());
                eventService.createEvent(EventType.PUBLISH_FAILED, feedback, association);
                return feedback;
            }

            response.setAssociation(association);
            statusService.save(response);
            eventService.createEvent(EventType.STATUS_CREATED, String.format("Created Status with ID '%s'. Overall status: %s", response.getId(), response.getStatus()), association);
        }
        String feedback = String.format("Successfully published %d bundle(s).", fileMap.values().size());
        eventService.createEvent(EventType.PUBLISHED, feedback, association);
        return feedback;
    }

    public URI getUploadUrl(Taxii11Association association) {
        // The logic below is a result of the TAXII 1.1 Specification. If the logic is unclear, it is recommended that you review the TAXII 1.1 Specification.
        // Any TAXII 1.1 Collection can have *zero to many* "Receiving Inbox Services", and any TAXII 1.1 Server can have *zero to many* "Service Instances" that can be "INBOX" services
        // Based on this, we first attempt to find *any* address in the Collection's set of services, and if that fails, we attempt to find *any* address in the Server's set of
        // services. This is a lazy approach, and we will not retry if the first address fails.
        Optional<String> url =
            // Assume there will probably only be one "receiving inbox service", find the first one that has an address
            association.getCollection().getCollectionObject().getReceivingInboxServices().stream()
                .filter(inboxService ->
                    inboxService.getAddress() != null                                                                        // Filter by address is non-null
                        && !inboxService.getAddress().isEmpty()                                                              // Filter by address is non-empty
                        && inboxService.getMessageBindings().contains(Constants.HEADER_TAXII11_MESSAGE_BINDING)).findFirst() // Filter by "is version 1.1"
                .map(InboxServiceBindingsType::getAddress);                                                                  // Get address

        // If we cannot determine the Inbox address from the collection itself, try the Server the collection is associated with
        if (!url.isPresent()) {
            if (association.getServer().getServiceInstances() == null
                || association.getServer().getServiceInstances(ServiceTypeEnum.INBOX) == null
                || association.getServer().getServiceInstances(ServiceTypeEnum.INBOX).isEmpty()) {
                throw new IllegalStateException(String.format("No inbox services found for collection '%s'", association.getCollection().getName()));
            } else {
                // Get 'INBOX' Service Instances and find any address
                url = association.getServer().getServiceInstances(ServiceTypeEnum.INBOX).stream()
                    .filter(instance -> instance.getAddress() != null
                        && !instance.getAddress().isEmpty()
                        && instance.getMessageBindings().contains(Constants.HEADER_TAXII11_MESSAGE_BINDING)).findFirst().map(ServiceInstanceType::getAddress);
            }
        }

        // If we still cannot determine an Inbox URL, throw an IllegalStateException
        if (!url.isPresent()) {
            throw new IllegalStateException(String.format("No inbox services found for collection '%s'", association.getCollection().getName()));
        }

        return URI.create(url.get());
    }

    public URI getUploadUrl(Taxii21Association association) {
        ApiRoot apiRoot = association.getServer().getApiRootObjects().stream().filter(apiRootObj -> apiRootObj.getEndpoint().equals(association.getCollection().getApiRootRef())).findFirst()
            .orElseThrow(IllegalStateException::new);
        return association.getServer().getCollectionObjectsUrl(apiRoot.getEndpoint(), association.getCollection().getCollectionObject().getId());
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public TaxiiService getTaxiiService() {
        return taxiiService;
    }

    public void setTaxiiService(TaxiiService taxiiService) {
        this.taxiiService = taxiiService;
    }

    public StatusService getStatusService() {
        return statusService;
    }

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public ServerService getServerService() {
        return serverService;
    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    public CollectionService getCollectionService() {
        return collectionService;
    }

    public void setCollectionService(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
