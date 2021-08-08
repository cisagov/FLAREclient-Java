package gov.dhs.cisa.ctm.flare.client.api.service.scheduled.async;

import gov.dhs.cisa.ctm.flare.client.TestData;
import gov.dhs.cisa.ctm.flare.client.api.FlareclientApp;
import gov.dhs.cisa.ctm.flare.client.api.security.ServerCredentialsUtils;
import gov.dhs.cisa.ctm.flare.client.api.service.ServerService;
import gov.dhs.cisa.ctm.flare.client.api.service.UserService;
import gov.dhs.cisa.ctm.flare.client.api.service.scheduled.async.AsyncServerInfoUpdateService;
import gov.dhs.cisa.ctm.flare.client.error.AuthenticationFailureException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class AsyncServerInfoUpdateTest {

    private static final Logger log = LoggerFactory.getLogger(AsyncServerInfoUpdateTest.class);

    private AsyncServerInfoUpdateService asyncServerInfoUpdateService;

    @MockBean
    private ServerCredentialsUtils serverCredentialsUtils;

    @MockBean
    private ServerService serverService;

    @MockBean
    private UserService userService;

    @BeforeClass
    public static void init() throws Exception {

    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        asyncServerInfoUpdateService = new AsyncServerInfoUpdateService(serverService, userService);
        ServerCredentialsUtils.setInstance(serverCredentialsUtils);
    }

    @Test
    public void asyncServerInformationUpdates() {
        Map<String, Map<String, String>> credentialMap = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        credentials.put(TestData.taxii11Server.getLabel(), ServerCredentialsUtils.encryptBasicAuthCredentials(TestData.user.getLogin(), TestData.user.getPassword(), TestData.user.getPassword()));
        credentialMap.put(TestData.user.getLogin(), credentials);
        when(serverCredentialsUtils.getServerCredentialsMap()).thenReturn(credentialMap);
        when(serverService.exists(TestData.taxii11Server.getLabel())).thenReturn(true);
        when(serverService.refreshServer(TestData.taxii11Server.getLabel())).thenReturn(TestData.taxii11Server);

        asyncServerInfoUpdateService.asyncServerInformationUpdates();

        verify(serverService).refreshServer(TestData.taxii11Server.getLabel());
    }

    @Test
    public void asyncServerInformationUpdateAuthFailure() {
        Map<String, Map<String, String>> credentialMap = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        credentials.put(TestData.taxii11Server.getLabel(), ServerCredentialsUtils.encryptBasicAuthCredentials(TestData.user.getLogin(), "user", "user"));
        credentialMap.put(TestData.user.getLogin(), credentials);
        when(serverCredentialsUtils.getServerCredentialsMap()).thenReturn(credentialMap);
        when(serverService.exists(TestData.taxii11Server.getLabel())).thenReturn(true);
        when(serverService.refreshServer(TestData.taxii11Server.getLabel())).thenThrow(new AuthenticationFailureException());
        when(userService.getUserWithAuthoritiesByLogin(TestData.user.getLogin())).thenReturn(Optional.of(TestData.user));

        asyncServerInfoUpdateService.asyncServerInformationUpdates();

        verify(userService).updateUser(TestData.user);
        verify(serverCredentialsUtils).clearCredentialsForUser(TestData.user.getLogin());
    }
}
