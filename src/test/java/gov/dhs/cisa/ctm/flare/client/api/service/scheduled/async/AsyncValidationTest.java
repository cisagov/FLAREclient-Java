package gov.dhs.cisa.ctm.flare.client.api.service.scheduled.async;

import gov.dhs.cisa.ctm.flare.client.TestData;
import gov.dhs.cisa.ctm.flare.client.api.FlareclientApp;
import gov.dhs.cisa.ctm.flare.client.api.domain.content.Stix1ContentWrapper;
import gov.dhs.cisa.ctm.flare.client.api.domain.content.ValidationResult;
import gov.dhs.cisa.ctm.flare.client.api.repository.CollectionRepository;
import gov.dhs.cisa.ctm.flare.client.api.repository.ContentRepository;
import gov.dhs.cisa.ctm.flare.client.api.repository.ServerRepository;
import gov.dhs.cisa.ctm.flare.client.api.service.EventService;
import gov.dhs.cisa.ctm.flare.client.api.service.scheduled.async.AsyncValidationService;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class AsyncValidationTest {

    private static final Logger log = LoggerFactory.getLogger(AsyncValidationTest.class);

    private AsyncValidationService asyncValidationService;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @MockBean
    private EventService eventService;

    private static Stix1ContentWrapper validContent;
    private static Stix1ContentWrapper invalidContent;


    @BeforeClass
    public static void init() {
        validContent = new Stix1ContentWrapper(TestData.rawStix111, TestData.taxii11Association);
        invalidContent = new Stix1ContentWrapper(TestData.rawStix111Invalid, TestData.taxii11Association);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        asyncValidationService = new AsyncValidationService(contentRepository, eventService);

        // Async Validation will look at the Association from the Database.
        // The association is made up of DBRefs, so we must save the Server and Collection from the Association first
        serverRepository.save(TestData.taxii11Association.getServer());
        collectionRepository.save(TestData.taxii11Association.getCollection());
    }

    @After
    public void tearDown() throws Exception {
        contentRepository.deleteAll();
    }

    @Test
    public void validatePending() {
        contentRepository.save(validContent);
        asyncValidationService.validatePending();
        assertTrue(contentRepository.findById(validContent.getId()).isPresent());
        assertNotNull(contentRepository.findById(validContent.getId()).get().getValidationResult());
        assertEquals(ValidationResult.Status.VALID, contentRepository.findById(validContent.getId()).get().getValidationResult().getStatus());
    }

    @Test
    public void validatePendingInvalid() {
        contentRepository.save(invalidContent);
        asyncValidationService.validatePending();
        assertTrue(contentRepository.findById(invalidContent.getId()).isPresent());
        assertNotNull(contentRepository.findById(invalidContent.getId()).get().getValidationResult());
        assertEquals(ValidationResult.Status.INVALID, contentRepository.findById(invalidContent.getId()).get().getValidationResult().getStatus());
    }
}
