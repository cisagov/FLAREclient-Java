package com.bcmc.xor.flare.client.selenium;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class CukesConfig {

    private Logger logger = LoggerFactory.getLogger(CukesConfig.class);

    public Properties getPropValues() {

        logger.info("loading cukes config properties ...");

        Properties prop = new Properties();
        try {
            FileInputStream ip = new FileInputStream(System.getProperty("cukes.config.file"));
            prop.load(ip);
        }
        catch (IOException e) {
            logger.error("Unable to get properties config: " + e.getMessage());
        }
        return prop;
    }
}
