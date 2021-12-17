package com.bcmc.xor.flare.client.api.config;

import org.mapstruct.ap.shaded.freemarker.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "flare", ignoreUnknownFields = true)
@Component("trace")
public class TraceConfiguration {

    private boolean trace;

    public TraceConfiguration() {}

    public synchronized boolean getTrace()  { return trace;}

    public synchronized void setTrace(boolean trace) { this.trace = trace;}

}
