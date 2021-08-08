package gov.dhs.cisa.ctm.flare.client.api.web.rest;

import gov.dhs.cisa.ctm.flare.client.TestData;
import gov.dhs.cisa.ctm.flare.client.api.FlareclientApp;
import gov.dhs.cisa.ctm.flare.client.api.domain.content.AbstractContentWrapper;
import gov.dhs.cisa.ctm.flare.client.api.domain.content.Stix1ContentWrapper;
import gov.dhs.cisa.ctm.flare.client.api.domain.content.Stix2ContentWrapper;
import gov.dhs.cisa.ctm.flare.client.api.domain.content.ValidationResult;
import gov.dhs.cisa.ctm.flare.client.api.repository.ContentRepository;
import gov.dhs.cisa.ctm.flare.client.api.service.CollectionService;
import gov.dhs.cisa.ctm.flare.client.api.service.ServerService;
import gov.dhs.cisa.ctm.flare.client.api.web.rest.ContentResource;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class ContentResourceTest {

    @Autowired
    private ContentResource contentResource;

    @MockBean
    private ServerService serverService;

    @MockBean
    private CollectionService collectionService;

    @MockBean
    private ContentRepository contentRepository;

    private static PageRequest page11;
    private static PageRequest page20;
    private static Stix1ContentWrapper content11;
    private static List<AbstractContentWrapper> content20;
    private static Page<AbstractContentWrapper> emptyPage;
    private static Page<AbstractContentWrapper> returnPage11;
    private static Page<AbstractContentWrapper> returnPage20;

    @BeforeClass
    public static void setUp() throws Exception {
        page11 = PageRequest.of(0, 1);
        page20 = PageRequest.of(0, 4);
        content11 = new Stix1ContentWrapper(TestData.rawStix111, TestData.taxii11Association);
        emptyPage = new PageImpl<>(Collections.emptyList());

        returnPage11 = new PageImpl<>(Collections.singletonList(content11), page11, 1);

        content20 = new ArrayList<>();
        TestData.jsonStix21.getAsJsonObject().get("objects").getAsJsonArray().forEach(object -> {
            Stix2ContentWrapper contentWrapper = new Stix2ContentWrapper(object.toString(), TestData.taxii21Association);
            contentWrapper.setValidationResult(new ValidationResult(ValidationResult.Status.PENDING, null));
            contentWrapper.setLastRetrieved(Instant.now());
            content20.add(contentWrapper);
        });

        returnPage20 = new PageImpl<>(content20, page20, 1);
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(serverService.findOneByLabel(TestData.taxii11Server.getLabel())).thenReturn(Optional.of(TestData.taxii11Server));
        when(collectionService.findOneById(TestData.taxii11Collection.getId())).thenReturn(Optional.of(TestData.taxii11Collection));
        when(serverService.findOneByLabel(TestData.taxii21Server.getLabel())).thenReturn(Optional.of(TestData.taxii21Server));
        when(collectionService.findOneById(TestData.taxii21Collection.getId())).thenReturn(Optional.of(TestData.taxii21Collection));
    }

    @Test
    public void getContentWrapper_NoContent_Test() {
        when(contentRepository.findAllByAssociation(any(), eq(TestData.taxii11Association))).thenReturn(emptyPage);
        ResponseEntity<List<AbstractContentWrapper>> response =
            contentResource.getContentWrapper(
                TestData.taxii11Server.getLabel(),
                TestData.taxii11Collection.getId(),
                page11);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        TestCase.assertEquals(0, response.getBody().size());
    }

    @Test
    public void testGetContentWrapper11() {
        when(contentRepository.findAllByAssociation(page11, TestData.taxii11Association)).thenReturn(returnPage11);
        ResponseEntity<List<AbstractContentWrapper>> response =
            contentResource.getContentWrapper(
                TestData.taxii11Server.getLabel(),
                TestData.taxii11Collection.getId(),
                page11);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(TestData.taxii11Server, response.getBody().get(0).getAssociation().getServer());
        assertEquals(TestData.taxii11Collection, response.getBody().get(0).getAssociation().getCollection());
    }

    @Test
    public void testGetContentWrapper20() {
        when(contentRepository.findAllByAssociation(page20, TestData.taxii21Association)).thenReturn(returnPage20);
        ResponseEntity<List<AbstractContentWrapper>> response =
            contentResource.getContentWrapper(
                TestData.taxii21Server.getLabel(),
                TestData.taxii21Collection.getId(),
                page20);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(TestData.taxii21Server, response.getBody().get(0).getAssociation().getServer());
        assertEquals(TestData.taxii21Collection, response.getBody().get(0).getAssociation().getCollection());
    }

    @Test
    public void testGetContentWrapperById11() {
        when(contentRepository.findByIdAndAssociation(content11.getId(), TestData.taxii11Association)).thenReturn(Optional.of(content11));
        ResponseEntity<AbstractContentWrapper> response =
            contentResource.getContentWrapperById(
                TestData.taxii11Server.getLabel(),
                TestData.taxii11Collection.getId(),
                content11.getId());

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(content11, response.getBody());
    }

    @Test
    public void testGetContentWrapperById20() {
        when(contentRepository.findByIdAndAssociation(content20.get(0).getId(), TestData.taxii21Association)).thenReturn(Optional.of(content20.get(0)));
        ResponseEntity<AbstractContentWrapper> response =
            contentResource.getContentWrapperById(
                TestData.taxii21Server.getLabel(),
                TestData.taxii21Collection.getId(),
                content20.get(0).getId());

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(content20.get(0), response.getBody());
    }

}
