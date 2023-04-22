package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.api.repository.CollectionRepository;
import com.bcmc.xor.flare.client.api.repository.ServerRepository;
import com.bcmc.xor.flare.client.api.repository.StatusRepository;
import com.bcmc.xor.flare.client.api.service.dto.StatusDTO;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Class for StatusService
 *
 * @see StatusService
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StatusServiceTest {

    //Repositories
    @Autowired
    StatusRepository statusRepository;

    //Services
    @Autowired
    StatusService statusService;

    //Misc
    @Autowired
    CacheManager cacheManager;

    @Autowired
    TaxiiService taxiiService;

    @Autowired
    ServerRepository serverRepository;

    @Autowired
    CollectionRepository collectionRepository;

    @Autowired
    UserService userService;

    @MockBean
    SecurityContext securityContext;

    @Before
    public void init() {
        statusRepository.deleteAll();
        MockitoAnnotations.initMocks(this);

        statusService.setCacheManager(cacheManager);
        statusService.setStatusRepository(statusRepository);

        serverRepository.save(TestData.taxii21Server);
        collectionRepository.save(TestData.taxii21Collection);
        TestData.setLoggedInUser(securityContext, userService);
    }

    /**
     * Create a Status Object and assert it has been saved to the repository
     */
    @Test
    public void testSave() {
        statusService.save(TestData.taxii21Status);
        assertTrue(statusRepository.findOneById(TestData.taxii21Status.getId()).isPresent());
    }

    /**
     * Retrieve a Status Object by ID and assert it was the correct status message
     */
    @Test
    public void testGetStatusById() {
        statusRepository.save(TestData.taxii21Status);
        assertTrue(statusService.getStatusById(TestData.taxii21Status.getId()).isPresent());
    }

    /**
     * Retrieve all Status Objects assert all statuses are retrieved
     */
    @Test
    public void testGetAllStatus() {
        Status status = statusRepository.save(TestData.taxii21Status);
        Page<StatusDTO> page = statusService.getAllStatus(PageRequest.of(0, 10));
        assertEquals(status.getStatus(), page.getContent().get(0).getStatus());
    }
}
