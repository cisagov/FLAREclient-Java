package gov.dhs.cisa.ctm.flare.client.api;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import gov.dhs.cisa.ctm.flare.client.api.config.DefaultProfileUtil;

/**
 * This is a helper Java class that provides an alternative to creating a web.xml.
 * This will be invoked only when the application is deployed to a Servlet container like Tomcat, JBoss etc.
 */
@SuppressWarnings("unused")
class ApplicationWebXml extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        /*
          set a default to use when no profile is configured.
         */
//        DefaultProfileUtil.addDefaultProfile(application.application());
        return application.sources(FlareclientApp.class);
    }
}
