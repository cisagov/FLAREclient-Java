package com.bcmc.xor.flare.client;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.async.AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.async.Taxii11AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.audit.Event;
import com.bcmc.xor.flare.client.api.domain.auth.Authority;
import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii11Collection;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii20Collection;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii20GetParameters;
import com.bcmc.xor.flare.client.api.domain.server.ApiRoot;
import com.bcmc.xor.flare.client.api.domain.server.Taxii11Server;
import com.bcmc.xor.flare.client.api.domain.server.Taxii20Server;
import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.api.security.AuthoritiesConstants;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.api.service.dto.UserDTO;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20Association;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.mitre.taxii.messages.xml11.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import xor.bcmc.taxii2.JsonHandler;
import xor.bcmc.taxii2.resources.Collection;
import xor.bcmc.taxii2.resources.Discovery;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.mockito.Mockito.when;

public class TestData {


    public static User user;
    public static Taxii11Server taxii11Server;
    public static Taxii20Server taxii20Server;
    public static Taxii11Collection taxii11Collection;
    public static Taxii20Collection taxii20Collection;
    public static ApiRoot apiRoot;
    public static Taxii11Association taxii11Association;
    public static Taxii20Association taxii20Association;
    public static AsyncFetch taxii11TestRequest;
    public static CollectionRecordType collectionRecordType;
    public static ServiceContactInfoType pollingServices;
    public static InboxServiceBindingsType inboxServiceBindingsType;
    public static ServiceInstanceType pollServiceInstance;
    public static ServiceInstanceType inboxServiceInstance;
    public static ServiceInstanceType collectionManagementServiceInstance;
    public static ServiceInstanceType discoveryServiceInstance;
    public static PollResponse pollResponse;
    public static String rawStix111;
    public static String rawStix111Invalid;
    public static String rawStix20;
    public static JsonElement jsonStix20;
    public static ContentBlock contentBlock;
    public static Taxii11PollParameters pollParameters;
    public static Taxii20GetParameters getParameters;
    public static StatusMessage failureStatusMessage;
    public static StatusMessage successStatusMessage;
    public static ResponseEntity<String> taxii20GetResponse;
    public static Event event;
    public static DiscoveryResponse discoveryResponse;
    public static Discovery taxii20Discovery;
    public static xor.bcmc.taxii2.resources.ApiRoot taxii20ApiRoot;
    public static Collection taxii20CollectionObject;
    public static CollectionInformationResponse collectionInformationResponse;
    public static Status taxii20Status;

    static {
        user = new User();
        Authority authority = new Authority();
        authority.setName("user");
        Set<Authority> authorities = Sets.newHashSet(authority);
        Map<String, String> serverCredentialsMap = new HashMap<>();
        serverCredentialsMap.put("username","user");
        serverCredentialsMap.put("password", "password");
        user.setId(UUID.randomUUID().toString());
        user.setAuthorities(authorities);
        user.setEmail("user@mail.com");
        user.setLogin("user");
        user.setActivated(true);
        user.setPassword("password");
        user.setFirstName("User");
        user.setLastName("User");
        user.setServerCredentials(serverCredentialsMap);

        // Establish test data: Taxii11Server
        taxii11Server = new Taxii11Server();
        taxii11Server.setId(UUID.randomUUID().toString());
        taxii11Server.setVersion(Constants.TaxiiVersion.TAXII11);
        taxii11Server.setLabel("Taxii11");
        taxii11Server.setUrl(URI.create("http://localhost:5000/discovery/"));

        // Establish test data: Taxii11Collection
        pollingServices =
                new ServiceContactInfoType(Constants.HEADER_TAXII11_PROTOCOL,
                        "http://localhost:5000/poll/",
                        Collections.singletonList(Constants.HEADER_TAXII11_SERVICES));

        inboxServiceBindingsType = new InboxServiceBindingsType()
                .withAddress("test")
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());

        pollServiceInstance = new ServiceInstanceType()
                .withServiceType(ServiceTypeEnum.POLL)
                .withAddress("test")
                .withAvailable(true)
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());

        inboxServiceInstance = new ServiceInstanceType()
                .withServiceType(ServiceTypeEnum.INBOX)
                .withAddress("test")
                .withAvailable(true)
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());

        collectionManagementServiceInstance = new ServiceInstanceType()
                .withServiceType(ServiceTypeEnum.COLLECTION_MANAGEMENT)
                .withAddress("test")
                .withAvailable(true)
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());

        discoveryServiceInstance = new ServiceInstanceType()
                .withServiceType(ServiceTypeEnum.DISCOVERY)
                .withAddress("test")
                .withAvailable(true)
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());


        collectionRecordType = new CollectionRecordType()
                .withCollectionType(CollectionTypeEnum.DATA_FEED)
                .withDescription("Collection record under test")
                .withCollectionName("Test11Collection")
                .withAvailable(true)
                .withPollingServices(pollingServices)
                .withReceivingInboxServices(inboxServiceBindingsType);

        taxii11Collection = new Taxii11Collection(collectionRecordType, taxii11Server.getId());
        taxii11Collection.setId(UUID.randomUUID().toString());
        taxii11Collection.setTaxiiVersion(Constants.TaxiiVersion.TAXII11);
        taxii11Collection.setId(UUID.randomUUID().toString());
        taxii11Collection.setDisplayName("Test11Collection");



        // Establish test data: Taxii20Server
        taxii20Server = new Taxii20Server();
        taxii20Server.setVersion(Constants.TaxiiVersion.TAXII21);
        taxii20Server.setLabel("Test TAXII 2.0 Server");
        taxii20Server.setUrl(URI.create("http://localhost:5000/taxii/"));




        // Establish test data: Taxii20 ApiRoot
        xor.bcmc.taxii2.resources.ApiRoot apiRootObject = new xor.bcmc.taxii2.resources.ApiRoot()
                .withTitle("Test Title")
                .withDescription("Test Description")
                .withMaxContentLength(10000000)
                .withVersions(Collections.singletonList("taxii-2.0"))
                .withId("TestApi");

        apiRoot = new ApiRoot(taxii20Server.getId());
        String apiRootId = UUID.randomUUID().toString();
        apiRoot.setId(apiRootId);
        apiRoot.setObject(apiRootObject);
        apiRoot.setCollections(Sets.newHashSet(taxii20Collection));
        apiRoot.setUrl(URI.create("http://localhost:5000/TestApi/"));
        apiRoot.setEndpoint("TestApi");

        // Establish test data: Taxii20Collection

        Collection collectionObject = new Collection(UUID.randomUUID().toString(), "Taxii20Collection", true, true);
        taxii20Collection = new Taxii20Collection(collectionObject, taxii20Server.getId(), apiRoot.getEndpoint());
        taxii20Collection.setTaxiiVersion(Constants.TaxiiVersion.TAXII21);
        taxii20Collection.setId(UUID.randomUUID().toString());
        taxii20Collection.setDisplayName("Taxii20Collection");

        taxii11Server.setServiceInstances(Sets.newHashSet(pollServiceInstance, inboxServiceInstance, discoveryServiceInstance, collectionManagementServiceInstance));
        taxii11Server.setCollections(Sets.newHashSet(taxii11Collection));
        taxii20Server.setCollections(Sets.newHashSet(taxii20Collection));

        taxii11Association = new Taxii11Association(taxii11Server, taxii11Collection, user);
        taxii20Association = new Taxii20Association(taxii20Server, taxii20Collection, user);

        rawStix111 = "<stix:STIX_Package\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:stix=\"http://stix.mitre.org/stix-1\"\n" +
                "    xmlns:indicator=\"http://stix.mitre.org/Indicator-2\"\n" +
                "    xmlns:cybox=\"http://cybox.mitre.org/cybox-2\"\n" +
                "    xmlns:AddressObject=\"http://cybox.mitre.org/objects#AddressObject-2\"\n" +
                "    xmlns:stixVocabs=\"http://stix.mitre.org/default_vocabularies-1\"\n" +
                "    xmlns:example=\"http://example.com/\"\n" +
                "    xsi:schemaLocation=\"http://stix.mitre.org/stix-1 ../stix_core.xsd\n" +
                "    http://stix.mitre.org/Indicator-2 ../indicator.xsd\n" +
                "\n" +
                "    http://stix.mitre.org/default_vocabularies-1 ../stix_default_vocabularies.xsd\n" +
                "    http://cybox.mitre.org/objects#AddressObject-2 ../cybox/objects/Address_Object.xsd\"\n" +
                "    id=\"example:STIXPackage-33fe3b22-0201-47cf-85d0-97c02164528d\"\n" +
                "    timestamp=\"2014-05-08T09:00:00.000000Z\"\n" +
                "    version=\"1.1.1\"\n" +
                ">\n" +
                "    <stix:STIX_Header>\n" +
                "        <stix:Title>Example watchlist that contains IP information.</stix:Title>\n" +
                "        <stix:Package_Intent xsi:type=\"stixVocabs:PackageIntentVocab-1.0\">Indicators - Watchlist</stix:Package_Intent>\n" +
                "    </stix:STIX_Header>\n" +
                "    <stix:Indicators>\n" +
                "        <stix:Indicator xsi:type=\"indicator:IndicatorType\" id=\"example:Indicator-33fe3b22-0201-47cf-85d0-97c02164528d\" timestamp=\"2014-05-08T09:00:00.000000Z\">\n" +
                "            <indicator:Type xsi:type=\"stixVocabs:IndicatorTypeVocab-1.1\">IP Watchlist</indicator:Type>\n" +
                "            <indicator:Description>Sample IP Address Indicator for this watchlist. This contains one indicator with a set of three IP addresses in the watchlist.</indicator:Description>\n" +
                "            <indicator:Observable  id=\"example:Observable-1c798262-a4cd-434d-a958-884d6980c459\">\n" +
                "                <cybox:Object id=\"example:Object-1980ce43-8e03-490b-863a-ea404d12242e\">\n" +
                "                    <cybox:Properties xsi:type=\"AddressObject:AddressObjectType\" category=\"ipv4-addr\">\n" +
                "                        <AddressObject:Address_Value condition=\"Equals\" apply_condition=\"ANY\">10.0.0.0##comma##10.0.0.1##comma##10.0.0.2</AddressObject:Address_Value>\n" +
                "                    </cybox:Properties>\n" +
                "                </cybox:Object>\n" +
                "            </indicator:Observable>\n" +
                "        </stix:Indicator>\n" +
                "    </stix:Indicators>\n" +
                "</stix:STIX_Package>\n";

        rawStix111Invalid = "<stix:STIX_Package\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:stix=\"http://stix.mitre.org/stix-1\"\n" +
                "    xmlns:indicator=\"http://stix.mitre.org/Indicator-2\"\n" +
                "    xmlns:cybox=\"http://cybox.mitre.org/cybox-2\"\n" +
                "    xmlns:AddressObject=\"http://cybox.mitre.org/objects#AddressObject-2\"\n" +
                "    xmlns:stixVocabs=\"http://stix.mitre.org/default_vocabularies-1\"\n" +
                "    xmlns:example=\"http://example.com/\"\n" +
                "    xsi:schemaLocation=\"http://stix.mitre.org/stix-1 ../stix_core.xsd\n" +
                "    http://stix.mitre.org/Indicator-2 ../indicator.xsd\n" +
                "\n" +
                "    http://stix.mitre.org/default_vocabularies-1 ../stix_default_vocabularies.xsd\n" +
                "    http://cybox.mitre.org/objects#AddressObject-2 ../cybox/objects/Address_Object.xsd\"\n" +
                "    id=\"example:STIXPackage-33fe3b22-0201-47cf-85d0-97c02164528d\"\n" +
                "    timestamp=\"2014-05-08T09:00:00.000000Z\"\n" +
                "    version=\"1.1.1\"\n" +
                ">\n" +
                "    <stix:STIX_Header>\n" +
                "        <stix:Title>Example watchlist that contains IP information.</stix:Title>\n" +
                "        <stix:Package_Intent xsi:type=\"stixVocabs:PackageIntentVocab-1.0\">Indicators - Watchlist</stix:Package_Intent>\n" +
                "    </stix:STIX_Header>\n" +
                "    <stix:Indicators>\n" +
                "        <stix:Indicator xsi:type=\"indicator:IndicatorType\" id=\"example:Indicator-33fe3b22-0201-47cf-85d0-97c02164528d\" timestamp=\"2014-05-08T09:00:00.000000Z\">\n" +
                "            <indicator:Observable xsi:type=\"stixVocabs:IndicatorTypeVocab-1.1\">IP Watchlist</indicator:Observable>\n" +
                "            <indicator:Description>Sample IP Address Indicator for this watchlist. This contains one indicator with a set of three IP addresses in the watchlist.</indicator:Description>\n" +
                "            <indicator:Observable  id=\"example:Observable-1c798262-a4cd-434d-a958-884d6980c459\">\n" +
                "                <cybox:Object id=\"example:Object-1980ce43-8e03-490b-863a-ea404d12242e\">\n" +
                "                    <cybox:Properties xsi:type=\"AddressObject:AddressObjectType\" category=\"ipv4-addr\">\n" +
                "                        <AddressObject:Address_Value condition=\"Equals\" apply_condition=\"ANY\">10.0.0.0##comma##10.0.0.1##comma##10.0.0.2</AddressObject:Address_Value>\n" +
                "                    </cybox:Properties>\n" +
                "                </cybox:Object>\n" +
                "            </indicator:Observable>\n" +
                "        </stix:Indicator>\n" +
                "    </stix:Indicators>\n" +
                "</stix:STIX_Package>\n";

        // Establish test data: Taxii11 Fetch Params
        Instant begin = Instant.now().minusMillis(60000*3);
        Instant end = Instant.now();

        pollParameters = new Taxii11PollParameters();
        pollParameters.setAssociation(taxii11Association);
        pollParameters.setFetchUrl(URI.create("http://localhost:5000/poll/"));
        pollParameters.setId(UUID.randomUUID().toString());
        pollParameters.setServerLabel(taxii11Server.getLabel());
        pollParameters.setRepeat(false);
        pollParameters.setWindow(8);
        pollParameters.setStartDate(ZonedDateTime.ofInstant(begin, ZoneId.of("UTC")));
        pollParameters.setEndDate(ZonedDateTime.ofInstant(end, ZoneId.of("UTC")));
        pollParameters.setContentBindings(new ArrayList<>(Constants.ContentBindings.contentBindingMap.values()));

        // Establish test data: Taxii11 Async Request
        taxii11TestRequest = new Taxii11AsyncFetch(pollParameters, 1000000);

        XMLGregorianCalendarImpl xmlGregorianCalendar = (XMLGregorianCalendarImpl) XMLGregorianCalendarImpl.createDateTime(2018, 1, 1, 1, 1, 1);
        contentBlock = new ContentBlock()
                .withContentBinding(new ContentInstanceType(null, Constants.stix1ContentBindingsStringMap.get("1.1.1")))
                .withTimestampLabel(xmlGregorianCalendar)
                .withContent(new AnyMixedContentType().withContent(rawStix111));

        pollResponse = new PollResponse()
                .withContentBlocks(contentBlock)
                .withCollectionName(taxii11Collection.getName())
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString())
                .withMore(false)
                .withInclusiveEndTimestamp(xmlGregorianCalendar);

        failureStatusMessage = new StatusMessage()
                .withStatusType(StatusTypeEnum.FAILURE.toString())
                .withMessage("Failure!")
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString());

        successStatusMessage = new StatusMessage()
                .withStatusType(StatusTypeEnum.SUCCESS.toString())
                .withMessage("Success!")
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString());

        getParameters = new Taxii20GetParameters();
        getParameters.setAssociation(taxii20Association);
        getParameters.setApiRootRef(apiRootId);
        getParameters.setServerLabel(taxii20Server.getLabel());
        getParameters.setAddedAfter(ZonedDateTime.now().minusDays(1));
        getParameters.setFetchUrl(URI.create("http://test.com"));
        getParameters.setRepeat(false);
        getParameters.setWindow(8000);
        //getParameters.setTypes(Constants.STIX20_TYPES.toArray(new String[]{}));

        rawStix20 = "{\n" +
                "  \"type\": \"bundle\",\n" +
                "  \"id\": \"bundle--c6a895f2-849c-4d1b-aba4-4b45c2800374\",\n" +
                "  \"spec_version\": \"2.0\",\n" +
                "  \"objects\": [\n" +
                "    {\n" +
                "      \"type\": \"identity\",\n" +
                "      \"id\": \"identity--39012926-a052-44c4-ae48-caaf4a10ee61\",\n" +
                "      \"created\": \"2017-02-24T15:50:10.564Z\",\n" +
                "      \"modified\": \"2017-08-24T15:50:10.564Z\",\n" +
                "      \"name\": \"Alpha Threat Analysis Org.\",\n" +
                "      \"identity_class\": \"organization\",\n" +
                "      \"labels\": [\n" +
                "        \"Cyber Security\"\n" +
                "      ],\n" +
                "      \"sectors\": [\n" +
                "        \"technology\"\n" +
                "      ],\n" +
                "      \"contact_information\": \"info@alpha.org\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"identity\",\n" +
                "      \"id\": \"identity--5206ba14-478f-4b0b-9a48-395f690c20a1\",\n" +
                "      \"created\": \"2017-02-26T17:55:10.442Z\",\n" +
                "      \"modified\": \"2017-02-26T17:55:10.442Z\",\n" +
                "      \"name\": \"Beta Cyber Intelligence Company\",\n" +
                "      \"identity_class\": \"organization\",\n" +
                "      \"labels\": [\n" +
                "        \"Cyber Security\"\n" +
                "      ],\n" +
                "      \"sectors\": [\n" +
                "        \"technology\"\n" +
                "      ],\n" +
                "      \"contact_information\": \"info@beta.com\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"indicator\",\n" +
                "      \"id\": \"indicator--9299f726-ce06-492e-8472-2b52ccb53192\",\n" +
                "      \"created_by_ref\": \"identity--39012926-a052-44c4-ae48-caaf4a10ee6e\",\n" +
                "      \"created\": \"2017-02-27T13:57:10.515Z\",\n" +
                "      \"modified\": \"2017-02-27T13:57:10.515Z\",\n" +
                "      \"name\": \"Malicious URL\",\n" +
                "      \"description\": \"This URL is potentially associated with malicious activity and is listed on several blacklist sites.\",\n" +
                "      \"pattern\": \"[url:value = 'http://paypa1.banking.com']\",\n" +
                "      \"valid_from\": \"2015-06-29T09:10:15.915Z\",\n" +
                "      \"labels\": [\n" +
                "        \"malicious-activity\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"sighting\",\n" +
                "      \"id\": \"sighting--8356e820-8080-4692-aa91-ecbe94006832\",\n" +
                "      \"created_by_ref\": \"identity--5206ba14-478f-4b0b-9a48-395f690c20a2\",\n" +
                "      \"created\": \"2017-02-28T19:37:11.213Z\",\n" +
                "      \"modified\": \"2017-02-28T19:37:11.213Z\",\n" +
                "      \"first_seen\": \"2017-02-27T21:37:11.213Z\",\n" +
                "      \"last_seen\": \"2017-02-27T21:37:11.213Z\",\n" +
                "      \"count\": 1,\n" +
                "      \"sighting_of_ref\": \"indicator--9299f726-ce06-492e-8472-2b52ccb53191\",\n" +
                "      \"where_sighted_refs\": [\n" +
                "        \"identity--5206ba14-478f-4b0b-9a48-395f690c20a2\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        taxii20GetResponse = ResponseEntity.ok(rawStix20);

        jsonStix20 = JsonHandler.getInstance().getGson().fromJson(rawStix20, JsonElement.class);


        event = new Event();
        event.setId(UUID.randomUUID().toString());
        event.setDetails("test");
        event.setServer(taxii11Server.getLabel());
        event.setTaxiiCollection(taxii11Collection.getDisplayName());

        discoveryResponse = new DiscoveryResponse()
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString())
                .withServiceInstances(discoveryServiceInstance, pollServiceInstance, collectionManagementServiceInstance, inboxServiceInstance);

        collectionInformationResponse = new CollectionInformationResponse()
                .withCollections(collectionRecordType)
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString());

        taxii20Discovery = new Discovery()
                .withId(UUID.randomUUID().toString())
                .withTitle("Test")
                .withContact("Test")
                .withDefaultApiRoot("test")
                .withApiRoots(Arrays.asList("test"))
                .withDescription("Test");
        taxii20Server.updateFromDiscovery(taxii20Discovery);

        taxii20ApiRoot = new xor.bcmc.taxii2.resources.ApiRoot()
                .withId(UUID.randomUUID().toString())
                .withTitle("Test")
                .withDescription("Test")
                .withMaxContentLength(1000000000)
                .withVersions(Arrays.asList(Constants.HEADER_TAXII20_JSON));
        HashSet<ApiRoot> apiRootObjects = new HashSet<>();
        apiRootObjects.add(apiRoot);
        taxii20Server.setApiRootObjects(apiRootObjects);
        taxii20Collection.setApiRootRef(apiRoot.getEndpoint());

        taxii20CollectionObject = new Collection()
                .withId(UUID.randomUUID().toString())
                .withTitle("Test")
                .withDescription("Test")
                .withMediaTypes(Arrays.asList(Constants.HEADER_STIX20_JSON))
                .withCanRead(true)
                .withCanWrite(true);

        taxii20Status = new Status();
        taxii20Status.setAssociation(taxii20Association);
        taxii20Status.setFailureCount(0);
        taxii20Status.setPendingCount(0);
        taxii20Status.setSuccessCount(5);
        taxii20Status.setTotalCount(5);
        taxii20Status.setStatus("COMPLETE");
        taxii20Status.setRequestTimestamp(ZonedDateTime.now().minusDays(1));

        taxii20Association.setApiRoot(apiRoot);

    }

    public static void setLoggedInUser(SecurityContext securityContext, UserService userService) {
        userService.createUser(new UserDTO(TestData.user));
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(TestData.user.getLogin(), TestData.user.getPassword(), Collections.singletonList(grantedAuthority));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, TestData.user.getPassword());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

}