package gov.dhs.cisa.ctm.flare.client.api.web.rest;

import gov.dhs.cisa.ctm.flare.client.TestData;
import gov.dhs.cisa.ctm.flare.client.api.domain.server.Taxii21Server;
import gov.dhs.cisa.ctm.flare.client.api.service.CollectionService;
import gov.dhs.cisa.ctm.flare.client.api.service.ServerService;
import gov.dhs.cisa.ctm.flare.client.api.service.TaxiiService;
import gov.dhs.cisa.ctm.flare.client.api.web.rest.TaxiiManifestResource;
import gov.dhs.cisa.ctm.flare.client.error.ErrorConstants;
import gov.dhs.cisa.ctm.flare.client.error.ManifestNotSupportedException;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(TaxiiAssociation.class)
@PowerMockIgnore("javax.management.*")
public class TaxiiManifestResourceTest {

    private TaxiiManifestResource taxiiManifestResource;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private ServerService serverService;

    @MockBean
    private CollectionService collectionService;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private TaxiiService taxiiService;

    private static Taxii21Server taxii21Server;
    private static String collectionId;

    @BeforeClass
    public static void setUp() {
        taxii21Server = TestData.taxii21Server;
        collectionId = TestData.taxii21Collection.getId();
    }

    @Before
    public void init() {
        taxiiManifestResource = new TaxiiManifestResource(taxiiService,serverService,collectionService);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void fetchManifestResource_NoFiltering() {

        when(serverService.getServerRepository().findOneByLabelIgnoreCase(any(String.class))).thenReturn(Optional.of(taxii21Server));
        PowerMockito.mockStatic(TaxiiAssociation.class);
        PowerMockito.when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii21Association);
        when(taxiiService.getTaxii21RestTemplate().getManifest(any(Taxii21Server.class),any(URI.class))).thenReturn(TestData.manifest);

        ResponseEntity<Map<String, String>> response = taxiiManifestResource.fetchManifestResource(taxii21Server.getLabel(),collectionId, null );
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey(collectionId));
        assertEquals(response.getBody().get(collectionId), TestData.manifest);
    }

    @Test
    public void fetchManifestResource_Filtering() {
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put(TestData.filterStringKey, TestData.filterStringValue);
        when(serverService.getServerRepository().findOneByLabelIgnoreCase(any(String.class))).thenReturn(Optional.of(taxii21Server));
        PowerMockito.mockStatic(TaxiiAssociation.class);
        PowerMockito.when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii21Association);
        when(taxiiService.getTaxii21RestTemplate().getManifest(any(Taxii21Server.class),any(URI.class))).thenReturn(TestData.manifest);

        ResponseEntity<Map<String, String>> response = taxiiManifestResource.fetchManifestResource(taxii21Server.getLabel(),collectionId, filterMap);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey(collectionId));
        assertFalse(response.getBody().isEmpty());
    }

    @Test(expected = ManifestNotSupportedException.class)
    public void fetchManifestResourceUnsupported() {
        when(serverService.getServerRepository().findOneByLabelIgnoreCase(any(String.class))).thenReturn(Optional.of(TestData.taxii11Server));

        ResponseEntity<Map<String, String>> response = taxiiManifestResource.fetchManifestResource(taxii21Server.getLabel(),collectionId, null);
        assertNotNull(response);
        assertEquals(response.getBody(), ErrorConstants.MANIFEST_NOT_SUPPORTED);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
