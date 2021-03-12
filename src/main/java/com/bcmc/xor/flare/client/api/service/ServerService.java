package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii11Collection;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii21Collection;
import com.bcmc.xor.flare.client.api.domain.collection.TaxiiCollection;
import com.bcmc.xor.flare.client.api.domain.server.*;
import com.bcmc.xor.flare.client.api.repository.ContentRepository;
import com.bcmc.xor.flare.client.api.repository.ServerRepository;
import com.bcmc.xor.flare.client.api.security.SecurityUtils;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import com.bcmc.xor.flare.client.api.service.dto.ServersDTO;
import com.bcmc.xor.flare.client.api.service.dto.UserDTO;
import com.bcmc.xor.flare.client.api.service.scheduled.RecurringFetchService;
import com.bcmc.xor.flare.client.api.service.scheduled.async.AsyncFetchRequestService;
import com.bcmc.xor.flare.client.error.*;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21RestTemplate;
import com.mongodb.client.result.DeleteResult;

import org.apache.commons.lang3.StringUtils;
import org.mitre.taxii.messages.xml11.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import xor.bcmc.taxii2.JsonHandler;
import xor.bcmc.taxii2.resources.Collections;
import xor.bcmc.taxii2.resources.Discovery;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServerService {

    private static final Logger log = LoggerFactory.getLogger(ServerService.class);

    private ServerRepository serverRepository;
    
    @Autowired
    private ContentRepository contentRepository;

    private UserService userService;

    private TaxiiService taxiiService;

    private ApiRootService apiRootService;

    private CollectionService collectionService;

    private CacheManager cacheManager;

    private EventService eventService;

    @Autowired
    private RecurringFetchService recurringFetchService;

    @Autowired
    private AsyncFetchRequestService asyncFetchRequestService;

	private MongoTemplate mongoTemplate;

    public ServerService(ServerRepository serverRepository, UserService userService,
                         TaxiiService taxiiService, ApiRootService apiRootService,
                         CollectionService collectionService, CacheManager cacheManager,
                         EventService eventService, MongoTemplate mongoTemplate) {        
    	this.serverRepository = serverRepository;
        this.userService = userService;
        this.taxiiService = taxiiService;
        this.apiRootService = apiRootService;
        this.collectionService = collectionService;
        this.cacheManager = cacheManager;
        this.eventService = eventService;
        this.mongoTemplate = mongoTemplate;

    }

    // TAXII 1.1 ---------

    /**
     * Update the server information for a TAXII 1.1 server
     *
     * @param server            the server to update
     * @param discoveryResponse the discovery response object that will be used to update the server's information
     */
    private void updateServerInformation(Taxii11Server server, DiscoveryResponse discoveryResponse) {
        log.info("Requesting Discovery information for server '{}' ({})", server.getLabel(), server.getId());

        // Based on the response, update the passed Server object
        if (server.getServiceInstances() == null || server.getServiceInstances().isEmpty()) {
            server.setServiceInstances(new HashSet<>());
        }

        boolean receivedServerInformation = false;
        if (discoveryResponse != null && discoveryResponse.getServiceInstances() != null) {
            server.getServiceInstances().addAll(discoveryResponse.getServiceInstances());
            if (!discoveryResponse.getServiceInstances().isEmpty()) {
                receivedServerInformation = true;
            }
        }

        server.setHasReceivedServerInformation(receivedServerInformation);
        if (receivedServerInformation) {
            log.info("Got server information for server '{}' ({})", server.getLabel(), server.getId());
            server.setLastReceivedServerInformation(Instant.now());
        } else {
            log.warn("Failed to retrieve server information for server '{}' ({})", server.getLabel(), server.getId());
            server.setAvailable(false);
        }
    }

    /**
     * Update Collection information for a TAXII 1.1 server
     *
     * @param server the server to update collection information for
     */
    private void updateCollectionInformation(Taxii11Server server) {
        log.info("Requesting Collection information for server '{}' ({})", server.getLabel(), server.getId());
        if (server.getCollections() == null) {
            server.setCollections(new HashSet<>());
        }

        Set<String> collectionUrls = getCollectionManagementUrls(server);

        boolean receivedCollectionInformation = false;
        Iterator<String> urlIterator = collectionUrls.iterator();
        while (urlIterator.hasNext() && !receivedCollectionInformation) {
            String attemptUrl = urlIterator.next();
            log.info("Attempting to get collection information from '{}'", attemptUrl);
            try {
                // Attempt REST request to get TaxiiCollection information
                CollectionInformationResponse collectionInfoResponse =
                    getTaxiiService().getTaxii11RestTemplate().submitCollectionInformationResponse(
                        server,
                        new CollectionInformationRequest().withMessageId(MessageHelper.generateMessageId()),
                        URI.create(attemptUrl));

                receivedCollectionInformation = parseCollectionsFromResponse(server, collectionInfoResponse);

            } catch (RestClientResponseException e) {
                log.warn("Unable to update collection information for {} ({}). Received {} {} from {}.",
                    server.getLabel(), server.getId(), e.getRawStatusCode(), e.getStatusText(), attemptUrl);
            } catch (RestClientException e) {
                log.warn("Unable to update collection information for {} ({}). Encountered a client-side error: {}",
                    server.getLabel(), server.getId(), e.getMessage());
            }
        }

        server.setHasReceivedCollectionInformation(receivedCollectionInformation);
        if (receivedCollectionInformation) {
            server.setLastReceivedCollectionInformation(Instant.now());
        }
    }

    private Set<String> getCollectionManagementUrls(Taxii11Server server) {
        return server.getServiceInstances().stream()
            .filter(serviceInstanceType ->
                serviceInstanceType.getMessageBindings().contains(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                    && serviceInstanceType.getServiceType().equals(ServiceTypeEnum.COLLECTION_MANAGEMENT))  // Filter based on type
            .map(ServiceInstanceType::getAddress)                                                       // Get address from each
            .collect(Collectors.toSet());

    }

    /**
     * Parse set of collections from a {@link CollectionInformationResponse}
     *
     * <ol>
     *     <li>If we receive a collection information response, check that there are collections for processing.</li>
     *     <li>
     *         If there are, check each one to see if we already have a local copy.
     *         <ol>
     *             <li>If we do, update the local copy</li>
     *             <li>else, create a new collection</li>
     *         </ol>
     *     </li>
     *     <li>Save the collections.</li>
     * </ol>
     * @param server the server we will add the resulting collections to
     * @param response the response containing collection information
     */
    private boolean parseCollectionsFromResponse(Taxii11Server server, CollectionInformationResponse response) {
        if (response != null && response.getCollections() != null && !response.getCollections().isEmpty()) {
            Set<Taxii11Collection> collections = response.getCollections().stream()
                .map(
                    collectionRecordType -> {
                        log.debug("Adding or updating collection '{}'", collectionRecordType.getCollectionName());
                        return getCollectionService().findOneByDisplayName(collectionRecordType.getCollectionName())
                            .orElse(new Taxii11Collection(collectionRecordType, server.getId()));
                    }).map(Taxii11Collection.class::cast)
                .map(getCollectionService()::save)
                .collect(Collectors.toSet());
            server.getCollections().addAll(collections);
            return true;
        }
        return false;
    }

    /**
     * Updates all information for a server
     * <p>
     * This method will first call {@link com.bcmc.xor.flare.client.taxii.taxii11.Taxii11RestTemplate#submitDiscovery(TaxiiServer)}
     * to obtain a {@link DiscoveryResponse} object, which will then be used to update the server and collection information.
     *
     * @param server the server to be updated
     * @return the updated server
     */
    private Taxii11Server refreshServer(Taxii11Server server) {
        DiscoveryResponse response;
        try {
            response = getTaxiiService().getTaxii11RestTemplate().submitDiscovery(server);
        } catch (RequestException | RestClientException e) {
            if (e.getMessage().contains("Unauthorized")) {
                log.info("Received a 401 - Unauthorized in response to a discovery request. This indicates bad Basic Auth credentials.");
                removeServerCredential(server.getLabel());
                throw new AuthenticationFailureException();
            }
            server.setAvailable(false);
            server.setHasReceivedServerInformation(false);
            server.setHasReceivedSubscriptionInformation(false);
            server.setHasReceivedCollectionInformation(false);
            server.setUrl(null);

            serverRepository.save(server);
            clearServerCaches(server);
            eventService.createEvent(EventType.SERVER_UNAVAILABLE, String.format("Failed to update server information for '%s'. %s", server.getLabel(), e.getMessage()), server.getLabel());
            throw new ServerDiscoveryException();
        }
        return refreshServer(server, response);
    }

    /**
     * Updates all information for a server
     *
     * @param server            the server to update
     * @return the updated server
     */
    private Taxii11Server refreshServer(Taxii11Server server, DiscoveryResponse discoveryResponse) {
        if (discoveryResponse == null) {
            return server;
        }

        log.info("Updating information for server '{}'", server.getLabel());
        updateServerInformation(server, discoveryResponse);
        if (server.hasReceivedServerInformation()) {
            updateCollectionInformation(server);
        }
        server = (Taxii11Server) finalizeAndSave(server);
        eventService.createEvent(EventType.SERVER_UPDATED, String.format("Updated server information for '%s'", server.getLabel()), server.getLabel());
        return server;
    }
    // -------------------


    // TAXII 2.1 ---------

    /**
     * Update the server information for a TAXII 2.1 server
     *
     * @param server            the server to update
     * @param discoveryResponse the discovery response object that will be used to update the server's information
     */
    private void updateServerInformation(Taxii21Server server, Discovery discoveryResponse) {
        log.info("Requesting Discovery information for server '{}' ({})", server.getLabel(), server.getId());
        log.debug("TAXII 2.1 Discovery Response: {}", JsonHandler.getInstance().toJson(discoveryResponse));

        // Based on the information, update the passed Server object
        server.updateFromDiscovery(discoveryResponse);

        // Check to see if we have received server information ('title' is the only required field!)
        boolean receivedServerInformation = !server.getTitle().isEmpty();
        server.setHasReceivedServerInformation(receivedServerInformation);
        if (receivedServerInformation) {
            log.info("Received server information for server '{}' ({})", server.getLabel(), server.getId());
            server.setLastReceivedServerInformation(Instant.now());
        } else {
            log.error("Failed to retrieve server information for server '{}' ({})", server.getLabel(), server.getId());
        }
    }

    /**
     * Update API Root information for a TAXII 2.1 server
     *
     * @param server the server to update API Root information for
     */
    private void updateApiRootInformation(Taxii21Server server) {
        log.info("Requesting API Root information for server '{}' ({})", server.getLabel(), server.getId());

        // If not exists
        if (server.getApiRootObjects() == null) {
            server.setApiRootObjects(new HashSet<>());
        }

        if (server.getApiRoots() == null || server.getApiRoots().isEmpty()) {
            log.warn("No API Roots found for '{}' ({}). Unable to update API Root information.",
                server.getLabel(), server.getId());
            server.setHasReceivedApiRootInformation(false);
            return;
        }

        // For each ApiRoot (a URL) in the supplied server (retrieved from the Discovery request)
        boolean updateFailure = false;
        for (String apiRootString : server.getApiRoots()) {
            try {
                URI apiRootUrl = server.getApiRootUrl(apiRootString);
                // Request information for that specific ApiRoot via REST
                log.info("Attempting to get API root information from '{}'", apiRootUrl.toString());
                xor.bcmc.taxii2.resources.ApiRoot apiRootResponse = getTaxiiService().getTaxii21RestTemplate().getApiRoot(server, apiRootUrl);
                log.debug("API Root Resource: {}", JsonHandler.getInstance().getGson().toJson(apiRootResponse));

                // Check whether or not we have a local version of API Root. If we do, update it, else, create a new one.
                Optional<ApiRoot> existingApiRoot = server.getApiRootObjects().stream()
                    .filter(api -> api.getUrl().equals(apiRootUrl)).findAny();
                ApiRoot apiRoot;
                if (existingApiRoot.isPresent()) {
                    apiRoot = existingApiRoot.get();
                } else {
                    apiRoot = new ApiRoot(server.getId());
                    apiRoot.setUrl(apiRootUrl);
                    try {
                        apiRoot.setEndpoint(ApiRoot.getEndpointFromURL(apiRootString));
                    } catch (URISyntaxException e) {
                        log.error("Won't add endpoint to ApiRoot object. URI Syntax Exception in API Root URL: {}. {}",
                            apiRootUrl.toString(), e.getMessage());
                    }
                    server.getApiRootObjects().add(apiRoot);
                }

                apiRoot.setObject(apiRootResponse);
                // Save the ApiRoot
                log.info("Saving ApiRoot '{}'", apiRoot.getEndpoint());
                getApiRootService().save(apiRoot);
            } catch (RestClientException e) {
                log.warn("Unable to update API Root information for '{}' of server '{}' ({})",
                    apiRootString, server.getLabel(), server.getId());
                updateFailure = true;
            }
        }

        if (!updateFailure) {
            server.setHasReceivedApiRootInformation(true);
            server.setLastReceivedApiRootInformation(Instant.now());
        }

    }

    /**
     * Update Collection information for a TAXII 1.1 server
     *
     * @param server the server to update collection information for
     */
    private void updateCollectionInformation(Taxii21Server server) {
        log.info("Requesting Collection information for server '{}' ({})", server.getLabel(), server.getId());
        if (server.getCollections() == null) {
            server.setCollections(new HashSet<>());
        }

        boolean updateFailure = false;
        for (ApiRoot apiRoot : server.getApiRootObjects()) {
            URI collectionUrl = server.getCollectionInformationUrl(apiRoot.getEndpoint());
            try {
                Collections collectionsResponse = getTaxiiService().getTaxii21RestTemplate().getCollections(server, collectionUrl);
                log.debug("Collection information: {}", JsonHandler.getInstance().toJson(collectionsResponse));

                if (collectionsResponse != null) {
                    if (collectionsResponse.getCollections() == null || collectionsResponse.getCollections().isEmpty()) {
                        getApiRootService().save(apiRoot);
                    } else {
                        HashSet<Taxii21Collection> collections = new HashSet<>();
                        // For each 'TaxiiCollection' object in the response, create a new GUI Domain TaxiiCollection object
                        // and update its fields; save them in the Collections repository
                        for (xor.bcmc.taxii2.resources.Collection collectionObject : collectionsResponse.getCollections()) {
                            Optional<Taxii21Collection> existingCollection =
                                apiRoot.getCollections().stream().map(Taxii21Collection.class::cast)
                                    .filter(collection -> collection.getCollectionObject().getId().equals(collectionObject.getId())).findAny();
                            Taxii21Collection collection;
                            if (existingCollection.isPresent()) {
                                collection = existingCollection.get();
                                log.debug("Existing collection will be updated: {}", collection.getId());
                                collection.setCollectionObject(collectionObject);
                            } else {
                                collection = new Taxii21Collection(collectionObject, server.getId(), apiRoot.getEndpoint());
                                collection.setApiUrl(collectionUrl.resolve(collectionObject.getId()));
                                collections.add(collection);
                            }
                            getCollectionService().save(collection);
                            // Add to the collection list which will be added to the ApiRoot object
                        }
                        // If at least one ApiRoot had collection, update 'atLeastOneUpdated' boolean to 'true'
                        if (!collections.isEmpty()) {
                            apiRoot.getCollections().addAll(collections);
                            server.getCollections().addAll(collections);
                            getApiRootService().save(apiRoot);
                        }
                    }
                }
            } catch (RestClientResponseException e) {
                log.warn("Unable to update collection information for {} ({}) - {} ({}). Received {} {} from {}.",
                    server.getLabel(), server.getId(), apiRoot.getEndpoint(), apiRoot.getId(),
                    e.getRawStatusCode(), e.getStatusText(), collectionUrl);
                updateFailure = true;
            } catch (RestClientException e) {
                log.warn("Unable to update collection information for {} ({}) - {} ({}). Encountered a client-side error: {}",
                    server.getLabel(), server.getId(), apiRoot.getEndpoint(), apiRoot.getId(),
                    e.getMessage());
                updateFailure = true;
            }
        }

        if (!updateFailure) {
            log.info("Received collection information for '{}' ({})", server.getLabel(), server.getId());
            server.setHasReceivedCollectionInformation(true);
            server.setLastReceivedCollectionInformation(Instant.now());
        } else {
            log.warn("Unable to retrieve ALL collection information for '{}' ({})", server.getLabel(), server.getId());
        }
    }

    /**
     * Updates all information for a server
     * <p>
     * This method will first call {@link Taxii21RestTemplate#discovery(TaxiiServer)}
     * to obtain a {@link Discovery} object, which will then be used to update the server and collection information.
     *
     * @param server the server to be updated
     * @return the updated server
     */
    private Taxii21Server refreshServer(Taxii21Server server) {

        Discovery discovery;
        try {
            discovery = getTaxiiService().getTaxii21RestTemplate().discovery(server);
        } catch (RequestException | RestClientException e) {
            if (e.getMessage().contains("Unauthorized")) {
                log.info("Received a 401 - Unauthorized in response to a discovery request. This indicates bad Basic Auth credentials.");
                removeServerCredential(server.getLabel());
                throw new AuthenticationFailureException();
            }
            server.setAvailable(false);
            server.setHasReceivedServerInformation(false);
            server.setHasReceivedCollectionInformation(false);

            serverRepository.save(server);
            clearServerCaches(server);
            eventService.createEvent(EventType.SERVER_UNAVAILABLE, String.format("Failed to update server information for '%s'. %s", server.getLabel(), e.getMessage()), server.getLabel());
            throw new ServerDiscoveryException();
        }

        Taxii21Server taxii21Server = refreshServer(server, discovery);
        return taxii21Server;
    }

    /**
     * Updates all information for a server
     *
     * @param server the server to update
     * @param discovery - discovery endpoint information for the server
     * @return the updated server
     */
    private Taxii21Server refreshServer(Taxii21Server server, Discovery discovery) {
        log.info("Updating information for server '{}'", server.getLabel());

        updateServerInformation(server, discovery);
        if (server.hasReceivedServerInformation()) {
            updateApiRootInformation(server);
            if (server.hasReceivedApiRootInformation()) {
                updateCollectionInformation(server);
            }
        }
        finalizeAndSave(server);
        eventService.createEvent(EventType.SERVER_UPDATED, String.format("Updated server information for '%s'", server.getLabel()), server.getLabel());
        return server;
    }
    // -------------------


    // CREATE ------------

    /**
     * Discovers the version of a provided TAXII server
     *
     * @param serverDTO the ServerDTO containing the URL that will be targeted for Discovery requests
     * @return either a {@link DiscoveryResponse} or {@link Discovery} object
     * @throws StatusMessageResponseException, RequestException if any errors are encountered when submitting the
     *                                         Discovery requests
     */
    private Object attemptVersionDiscovery(ServerDTO serverDTO) {
//        checkNewServerCredentials(serverDTO);

        Object discoveryObject = null;

        log.info("Trying 2.0 discovery against '{}'...", serverDTO.getUrl());
        try {
            discoveryObject = taxiiService.getTaxii21RestTemplate().discovery(serverDTO);
        } catch (RequestException e) {
            log.info("Encountered request exception: {}", e.getMessage());
            if (StringUtils.containsIgnoreCase(e.getMessage(),"UNAUTHORIZED")) {
                removeServerCredential(serverDTO.getLabel());
                throw new ServerCredentialsUnauthorizedException();
            }
        } catch (Exception e) {
            log.error("Couldn't parse response: {}", e.getMessage());
        }

        if (discoveryObject == null) {
            log.info("Trying 1.1 discovery against '{}'...", serverDTO.getUrl());
            try {
                discoveryObject = taxiiService.getTaxii11RestTemplate().submitDiscovery(serverDTO);
            } catch (RequestException e) {
                log.error("Encountered request exception: {}", e.getMessage());
                if (StringUtils.containsIgnoreCase(e.getMessage(), "UNAUTHORIZED")) {
                    removeServerCredential(serverDTO.getLabel());
                    throw new ServerCredentialsUnauthorizedException();
                } else {
                    throw new ServerCreationException();
                }
            } catch (Exception e) {
                log.error("Couldn't parse response: {}", e.getMessage());
            }
        }

        if (discoveryObject == null) {
            log.error("Discovery failed for {} and URL {}", serverDTO.getLabel(), serverDTO.getUrl());
            throw new ServerDiscoveryException();
        }

        return discoveryObject;
    }

    /**
     * Creates a new TaxiiServer
     *
     * @param serverDTO the ServerDTO containing the URL that will be targeted for Discovery requests
     * @return the resulting TaxiiServer
     * @throws StatusMessageResponseException, RequestException if any errors are encountered when submitting the
     *                                         Discovery requests
     */
    public TaxiiServer createServer(ServerDTO serverDTO) {
        checkNewServerCredentials(serverDTO);
        Object response = attemptVersionDiscovery(serverDTO);
        TaxiiServer server = null;
        if (response instanceof Discovery) {
            log.info("Got TAXII 2.1 Discovery response!");
            server = new Taxii21Server(serverDTO);
            server = serverRepository.save(server);
            server = refreshServer((Taxii21Server) server, (Discovery) response);
        } else if (response instanceof DiscoveryResponse) {
            log.info("Got TAXII 1.1 Discovery response!");
            server = new Taxii11Server(serverDTO);
            server = serverRepository.save(server);
            server = refreshServer((Taxii11Server) server, (DiscoveryResponse) response);
        }

        if (server == null) {
            String errorMessage = String.format("Error configuring server '%s', or server unavailable", serverDTO.getLabel());
            eventService.createEvent(EventType.SERVER_UNAVAILABLE, errorMessage, serverDTO.getLabel());
            log.error(errorMessage);
            throw new ServerCreationException();
        } else {
            eventService.createEvent(EventType.SERVER_ADDED, String.format("Created the '%s' server", serverDTO.getLabel()), serverDTO.getLabel());
            clearServerCaches(server);
            log.debug("Created Information for server {} ({})", server.getLabel(), server.getId());
        }

        return server;
    }

    /**
     * Saves a TaxiiServer
     * <p>
     * Will set the lastUpdated field to now, and set the isAvailable field
     *
     * @param server the server to save
     * @return the saved server
     */
    private TaxiiServer finalizeAndSave(TaxiiServer server) {
        log.info("Finalizing server {} ('{}')", server.getLabel(), server.getId());
        log.info("Received server information: {}", server.hasReceivedServerInformation());
        log.info("Received collection information: {}", server.hasReceivedCollectionInformation());
        if (server.hasReceivedServerInformation()) {
            server.setLastUpdated(Instant.now());
        }
        boolean available = server.hasReceivedServerInformation() && server.hasReceivedCollectionInformation();
        server.setAvailable(available);
        clearServerCaches(server);
        return serverRepository.save(server);
    }
    // -------------------


    // READ --------------

    public boolean exists(String label) {
        return findOneByLabel(label).isPresent();
    }

    public ServersDTO getAllServers() {
        return new ServersDTO(serverRepository.findAll());
    }

    public Optional<TaxiiServer> findOneByLabel(String label) {
        return serverRepository.findOneByLabelIgnoreCase(label);
    }

    public Optional<TaxiiServer> findOneById(String id) {
        return serverRepository.findOneById(id);
    }

    /**
     * Checks HTTP Basic Authentication credentials for new servers
     * <p>
     * Will determine if credentials already exist for the current logged in User and the given ServerDTO object.
     * If they do not already exist, a call to ServerCredentialsUtils will be executed to add the credentials provided
     * by the ServerDTO object.
     *
     * @param serverDTO the ServerDTO that will contain basic auth information
     */
    private void checkNewServerCredentials(ServerDTO serverDTO) {
        log.debug("Checking new server credentials for '{}'", serverDTO.getLabel());
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(IllegalStateException::new);
		if (serverDTO.getRequiresBasicAuth() && StringUtils.isNotBlank(serverDTO.getUsername())
				&& StringUtils.isNotBlank(serverDTO.getPassword())) {
	        // Note: Verify that blank password is not allowed.

			// Check new server credentials
            User user = userService.getUserWithAuthoritiesByLogin(login).orElseThrow(NotFoundException::new);
            Map<String, String> serverCredentialsForUser = ServerCredentialsUtils.getInstance().getServerCredentialsMap().get(user.getLogin());
            if (serverCredentialsForUser == null || !serverCredentialsForUser.containsKey(serverDTO.getLabel())) {
                addServerCredential(user, serverDTO.getLabel(), serverDTO.getUsername(), serverDTO.getPassword());
                serverCredentialsForUser = new HashMap<>();
            }
            if (serverCredentialsForUser.containsKey(serverDTO.getLabel())) {
                removeServerCredential(serverDTO.getLabel());
                addServerCredential(user, serverDTO.getLabel(), serverDTO.getUsername(), serverDTO.getPassword());
            }
            attemptVersionDiscovery(serverDTO);
        } else if (serverDTO.getRequiresBasicAuth() && 
        		(StringUtils.isBlank(serverDTO.getUsername()) || StringUtils.isBlank(serverDTO.getPassword()))) {
            // Note: Verify that blank password is a cause to enter here.
            log.debug("Checking new server credentials for '{}'.  The serverDTO requiresBasicAuth and has blank username or blank password.", serverDTO.getLabel());
            throw new IllegalStateException("checkNewServerCredentials was called with missing information in serverDTO");
        }
    }

    private void addServerCredential(User user, String label, String username, String password) {
        log.debug("Adding server credential for user '{}' and server '{}'", user.getLogin(), label);
        String encryptedCredentials = ServerCredentialsUtils.encryptBasicAuthCredentials(username, password, user.getPassword());
        user.getServerCredentials().put(label, encryptedCredentials);
        log.debug("Adding encrypted credentials to map for user '{}': {}", user.getLogin(), encryptedCredentials);
        ServerCredentialsUtils.getInstance().addCredentialsForUserToMemory(user.getServerCredentials(), username, user.getPassword());
        userService.updateUser(new UserDTO(user));
    }

    public void addServerCredential(String label, String username, String password) {
        Optional<User> optionalUser = userService.getUserWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin().orElseThrow(UserNotFoundException::new));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            addServerCredential(user, label, username, password);
        }
    }

    public void removeServerCredential(String label) {
        Optional<User> optionalUser = userService.getUserWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin().orElseThrow(UserNotFoundException::new));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!user.getServerCredentials().isEmpty()) {
                log.debug("Deleting server credential for user '{}' and server '{}'", user.getLogin(), label);
                Map<String, Map<String, String>> serverCredentialMap = ServerCredentialsUtils.getInstance().getServerCredentialsMap();
                if (!serverCredentialMap.isEmpty() && serverCredentialMap.get(user.getLogin()) != null) {
                    serverCredentialMap.get(user.getLogin()).remove(label);
                }
                user.getServerCredentials().remove(label);
                userService.updateUser(new UserDTO(user));
            }
        }
    }
    // -------------------


    // UPDATE ------------

    /**
     * Convenience method to call a specific version of {@link #refreshServer(Taxii11Server)} or
     * {@link #refreshServer(Taxii21Server)}
     *
     * @param server a TaxiiServer object that should have a specific version
     * @return the updated server
     * @throws IllegalStateException if the provided server does not have a version
     */
    private TaxiiServer refreshServer(TaxiiServer server) {
        switch (server.getVersion()) {
            case TAXII21:
                return refreshServer((Taxii21Server) server);
            case TAXII11:
                return refreshServer((Taxii11Server) server);
            default:
                throw new IllegalStateException("Unable to refresh a server that does not have a 'version");
        }
    }

    /**
     * Convenience method to call a specific version of {@link #refreshServer(Taxii11Server)} or
     * {@link #refreshServer(Taxii21Server)}
     *
     * @param serverLabel a TaxiiServer label to check
     * @return the updated server
     * @throws IllegalStateException if the provided server does not have a version
     */
    public TaxiiServer refreshServer(String serverLabel) {
        Optional<? extends TaxiiServer> server = findOneByLabel(serverLabel);
        if (server.isPresent()) {
            log.debug("Found server '{}' ({}). Refreshing...", server.get().getLabel(), server.get().getId());
            return refreshServer(server.get());
        } else {
            throw new NotFoundException(String.format("'%s' does not exist", serverLabel));
        }
    }

    /**
     * Updates a server based on a ServerDTO
     *
     * Will only update the label, url, and serverDescription fields.
     * Will call {@link #refreshServer(Taxii11Server)} or {@link #refreshServer(Taxii21Server)} depending on the version.
     *
     * If the provided ServerDTO (must have an 'id') does not already exist, will call {@link #createServer(ServerDTO)}
     *
     * @param serverDTO the ServerDTO containing an 'id', 'url', 'serverDescription' and 'label' for updating
     * @return the created or updated server
     * @throws IllegalStateException if the provided server does not have a version
     */
    public TaxiiServer updateServer(@Nonnull ServerDTO serverDTO) {
        Optional<TaxiiServer> optionalServer = serverRepository.findOneById(serverDTO.getId());
        Optional<TaxiiServer> optionalServerByLabel = serverRepository.findOneByLabelIgnoreCase(serverDTO.getLabel());
        // Server exists and server id was passed
        if (optionalServer.isPresent()) {
            // Check for trying to change label to an existing label
            if (optionalServerByLabel.isPresent()
                    && StringUtils.equalsIgnoreCase(serverDTO.getLabel(), optionalServerByLabel.get().getLabel())
                    && !serverDTO.getId().equals(optionalServerByLabel.get().getId())) {
                throw new ServerLabelAlreadyExistsException();
            }
            TaxiiServer taxiiServer = optionalServer.get();
            taxiiServer.setUrl(URI.create(serverDTO.getUrl()));
            taxiiServer.setServerDescription(serverDTO.getServerDescription());
            if (serverDTO.getRequiresBasicAuth() && StringUtils.isNotBlank(serverDTO.getUsername()) && StringUtils.isNotBlank(serverDTO.getPassword())) {
                checkNewServerCredentials(serverDTO);
                taxiiServer.setRequiresBasicAuth(true);
            } else if (!serverDTO.getRequiresBasicAuth()) {
                taxiiServer.setRequiresBasicAuth(false);
                this.removeServerCredential(serverDTO.getLabel());
            }
            // Handle label change with credentials
            if (!StringUtils.equalsIgnoreCase(serverDTO.getLabel(), taxiiServer.getLabel())
                    && taxiiServer.getRequiresBasicAuth()
                    && StringUtils.isBlank(serverDTO.getUsername())
                    && StringUtils.isBlank(serverDTO.getPassword())) {
                changeServerLabelForCredentials(serverDTO, taxiiServer);
            }
            taxiiServer.setLabel(serverDTO.getLabel());

            switch (taxiiServer.getVersion()) {
                case TAXII21:
                    refreshServer((Taxii21Server) taxiiServer);
                    break;
                case TAXII11:
                    refreshServer((Taxii11Server) taxiiServer);
                    break;
                case UNKNOWN:
                    deleteServer(serverDTO.getLabel());
                    taxiiServer = createServer(serverDTO);
                    break;
                default:
                    throw new IllegalStateException("Cannot update server; no version present");
            }
            return taxiiServer;
        } else if (optionalServerByLabel.isPresent()) { // Server by id was not present but server exists by label
            log.error("Server already exists and a Server ID was not provided for update");
            throw new ServerLabelAlreadyExistsException();
        } else { // Server wasn't found by id or label
            return createServer(serverDTO);
        }
    }

    private void changeServerLabelForCredentials(ServerDTO serverDTO, TaxiiServer taxiiServer) {
        Optional<User> optionalUser = userService.getUserWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin().orElseThrow(UserNotFoundException::new));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Map<String, String> serverCredentialsMap = user.getServerCredentials();
            Map<String, String> newServerCredentialMap = new HashMap<>();
            String oldCredentials = serverCredentialsMap.get(taxiiServer.getLabel());
            // Add new credentials
            serverCredentialsMap.put(serverDTO.getLabel(), oldCredentials);
            // Remove old credentials
            serverCredentialsMap.remove(taxiiServer.getLabel());
            user.setServerCredentials(serverCredentialsMap);
            userService.updateUser(user);
        }
    }
    // -------------------


    // DELETE ------------
    /**
     * Deletes a server and any associated ApiRoot or TaxiiCollection objects
     *
     * @param serverLabel the server's label to delete
     */
    public void deleteServer(String serverLabel) {
	recurringFetchService.deleteAllRecurringFetchesByServerLabel(serverLabel);  
	asyncFetchRequestService.deleteAllAsyncFetchesByServerLabel(serverLabel);
	serverRepository.findOneByLabelIgnoreCase(serverLabel).ifPresent(server -> {
		if (server.getRequiresBasicAuth()) {
		    removeServerCredential(serverLabel);
		}
		switch (server.getVersion()) {
		case TAXII21:
		    log.info("Deleting API Roots for '{}'", serverLabel);
		    if (((Taxii21Server) server).getApiRootObjects() != null
			&& !((Taxii21Server) server).getApiRootObjects().isEmpty()) {
			apiRootService.deleteAll(((Taxii21Server) server).getApiRootObjects());
		    }
		default:
		    log.info("Deleting Collections for '{}'", serverLabel);
		    if (log.isDebugEnabled()) {
			StringBuffer idBuffer = new StringBuffer();
			idBuffer.append("[ ");
			for (TaxiiCollection tc : server.getCollections()) {
			    String colId = "" + tc.getDisplayName() + " (" + tc.getId() + ")";
			    idBuffer.append(colId);
			}
			idBuffer.append(" ]");
			log.debug("Deleting Server's collections {}", idBuffer.toString());
		    }
		    if (server.getCollections() != null && !server.getCollections().isEmpty()) {
			server.getCollections().forEach( taxiiCollection -> { 
				log.debug("Deleting Server processing content for taxiiCollection '{}'", taxiiCollection.getDisplayName());
				TaxiiAssociation association = TaxiiAssociation.from(serverLabel, taxiiCollection.getId(), this, collectionService);
				log.trace("TaxiiAssociation '{}'", association.toString());
				
				contentRepository.deleteByAssociation(association);
				log.debug("Completed contentRepository.deleteByAssociation(association);");
			    });
			
			collectionService.deleteAll(server.getCollections());
		    }
		    log.info("Deleting Server '{}'", serverLabel);
		    serverRepository.delete(server);
		    this.clearServerCaches(server);
		    log.info("Delete server credentials for Server '{}'", serverLabel);
		    this.removeServerCredential(serverLabel);
		    eventService.createEvent(EventType.SERVER_DELETED, String.format("Deleted the '%s' server", serverLabel),
					     serverLabel);
		}
	    });
    }

    /**
     * Clears repository caches for the given server
     *
     * @param server the TaxiiServer to clear caches for
     */
    private void clearServerCaches(TaxiiServer server) {
        // TODO We need to investigate why evict() does not work here.
        Objects.requireNonNull(cacheManager.getCache(ServerRepository.SERVERS_BY_ID_CACHE)).clear();
        Objects.requireNonNull(cacheManager.getCache(ServerRepository.SERVERS_BY_LABEL_CACHE)).clear();
    }

    /**
     * Servers that do not exist should be cleared from the credentials map
     * <p>
     * This is scheduled everyday at 0100
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void removeNonExistentServerCredentials() {
        List<User> users = userService.getUsers();
        for (User user : users) {
            log.debug("Removing non existent server credentials for user '{}'", user.getLogin());
            Set<String> keys = new HashSet<>(user.getServerCredentials().keySet());
            keys.stream()
                .filter(key -> !serverRepository.existsByLabelIgnoreCase(key))
                .forEach(key -> {
                    log.debug("Removing '{}'", key);
                    user.getServerCredentials().remove(key);
                });
            userService.updateUser(user);
            userService.clearUserCaches(user);
        }
    }
    // -------------------


    // Dependencies ------
    public ServerRepository getServerRepository() {
        return serverRepository;
    }

    public void setServerRepository(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    private TaxiiService getTaxiiService() {
        return taxiiService;
    }

    public void setTaxiiService(TaxiiService taxiiService) {
        this.taxiiService = taxiiService;
    }

    private ApiRootService getApiRootService() {
        return apiRootService;
    }

    public void setApiRootService(ApiRootService apiRootService) {
        this.apiRootService = apiRootService;
    }

    private CollectionService getCollectionService() {
        return collectionService;
    }

    public void setCollectionService(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
}
