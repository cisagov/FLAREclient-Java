package com.bcmc.xor.flare.client.api;

import com.bcmc.xor.flare.client.api.config.ApplicationProperties;
import com.bcmc.xor.flare.client.api.config.DefaultProfileUtil;
import com.bcmc.xor.flare.client.taxii.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@ComponentScan("com.bcmc.xor.flare.client")
@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class FlareclientApp {

    private static final Logger log = LoggerFactory.getLogger(FlareclientApp.class);

    private final Environment env;

    @Autowired
    private ApplicationProperties applicationProperties;

    public FlareclientApp(Environment env) {
        this.env = env;
    }

    /**
     * Initializes flareclient.
     * <p>
     * Spring profiles can be configured with a program argument --spring.profiles.active=your-active-profile
     */
    @PostConstruct
    public void initApplication() {
        Validation.init(applicationProperties);
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(FlareclientApp.class);
        Map<String, Object> properties = new HashMap<>();//DefaultProfileUtil.addDefaultProfile(app);
        app.setDefaultProperties(properties);
        Environment env = app.run(args).getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\t{}://localhost:{}\n\t" +
                "External: \t{}://{}:{}\n\t" +
                "Profile(s): \t{}  \n\t" +
                "Application Name: \t{}  \n\t" +
                "Application Version: \t{}  \n" +
                "----------------------------------------------------------",
            env.getProperty("spring.application.name"),
            protocol,
            env.getProperty("server.port"),
            protocol,
            hostAddress,
            env.getProperty("server.port"),
            env.getActiveProfiles(),
            env.getProperty("flare.application-name"),
            env.getProperty("flare.application-version"));
    }
}
