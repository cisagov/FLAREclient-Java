package gov.dhs.cisa.ctm.flare.client.api.web.rest.errors;

import gov.dhs.cisa.ctm.flare.client.api.FlareclientApp;
import gov.dhs.cisa.ctm.flare.client.error.FlareExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.spring.web.advice.MediaTypes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the FlareExceptionTranslator controller advice.
 *
 * @see FlareExceptionTranslator
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class FlareExceptionTranslatorIntTest {

    @Autowired
    private ExceptionTranslatorTestController controller;

    private FlareExceptionTranslator flareExceptionTranslator = new FlareExceptionTranslator();

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(flareExceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .build();
    }

    @Test
    public void testConcurrencyFailure() throws Exception {
        mockMvc.perform(get("/test/concurrency-failure"))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaTypes.PROBLEM));
//            .andExpect(jsonPath("$.message").value(ErrorConstants.ERR_CONCURRENCY_FAILURE));
    }

    @Test
    public void testMethodArgumentNotValid() throws Exception {
         mockMvc.perform(post("/test/method-argument").content("{}").contentType(MediaType.APPLICATION_JSON))
             .andExpect(status().isBadRequest())
             .andExpect(content().contentType(MediaTypes.PROBLEM))
             .andExpect(jsonPath("$.message").value("Method argument not valid"));
//             .andExpect(jsonPath("$.fieldErrors.[0].objectName").value("testDTO"))
//             .andExpect(jsonPath("$.fieldErrors.[0].field").value("test"))
//             .andExpect(jsonPath("$.fieldErrors.[0].message").value("NotNull"));
    }

    @Test
    public void testParameterizedError() throws Exception {
        mockMvc.perform(get("/test/parameterized-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
            .andExpect(jsonPath("$.message").value("Parameterized Exception"));
//            .andExpect(jsonPath("$.params.param0").value("param0_value"))
//            .andExpect(jsonPath("$.params.param1").value("param1_value"));
    }

    @Test
    public void testParameterizedError2() throws Exception {
        mockMvc.perform(get("/test/parameterized-error2"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
            .andExpect(jsonPath("$.message").value("Parameterized Exception"));
//            .andExpect(jsonPath("$.params.foo").value("foo_value"))
//            .andExpect(jsonPath("$.params.bar").value("bar_value"));
    }

    @Test
    public void testMissingServletRequestPartException() throws Exception {
        mockMvc.perform(get("/test/missing-servlet-request-part"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
            .andExpect(jsonPath("$.message").value("Bad Request: Required request part 'missing Servlet request part' is not present"));
    }

    @Test
    public void testMissingServletRequestParameterException() throws Exception {
        mockMvc.perform(get("/test/missing-servlet-request-parameter"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
            .andExpect(jsonPath("$.message")
                .value("Bad Request: Required parameter type parameter 'missing Servlet request parameter' is not present"));
    }

    @Test
    public void testAccessDenied() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
            .andExpect(jsonPath("$.message").value("Forbidden: test access denied!"))
            ;
    }

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
            .andExpect(jsonPath("$.message").value("Unauthorized: test authentication failed!"));
//            .andExpect(jsonPath("$.path").value("/test/unauthorized"))
//            .andExpect(jsonPath("$.detail").value("test authentication failed!"));
    }

    @Test
    public void testMethodNotSupported() throws Exception {
        mockMvc.perform(post("/test/access-denied"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
            .andExpect(jsonPath("$.message")
                .value("Method Not Allowed: Request method 'POST' not supported"));
    }

    @Test
    public void testExceptionWithResponseStatus() throws Exception {
        mockMvc.perform(get("/test/response-status"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
            .andExpect(jsonPath("$.message").value("test response status"));
    }

    @Test
    public void testInternalServerError() throws Exception {
        mockMvc.perform(get("/test/internal-server-error"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaTypes.PROBLEM))
//            .andExpect(jsonPath("$.message").value("error.http.500"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"));
    }

}
