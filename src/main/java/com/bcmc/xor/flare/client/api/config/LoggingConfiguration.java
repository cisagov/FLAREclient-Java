package com.bcmc.xor.flare.client.api.config;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.Iterator;

@SuppressWarnings("FieldCanBeLocal")
@Configuration
public class LoggingConfiguration {

    private static final String LOGSTASH_APPENDER_NAME = "LOGSTASH";

    private static final String ASYNC_LOGSTASH_APPENDER_NAME = "ASYNC_LOGSTASH";

    private static final Logger log = LoggerFactory.getLogger(LoggingConfiguration.class);

    @SuppressWarnings("CanBeFinal")
    private LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    private final String appName;

    private final String serverPort;

    public LoggingConfiguration(@Value("${spring.application.name}") String appName, @Value("${server.port}") String serverPort,
                                @Value("${spring.metrics.logs.enabled}") Boolean metricsLogsEnabled) {
        this.appName = appName;
        this.serverPort = serverPort;
        if (metricsLogsEnabled != null && metricsLogsEnabled) {
            setMetricsMarkerLogbackFilter(context);
        }
    }

    // Configure a log filter to remove "metrics" logs from all appenders except the "LOGSTASH" appender
    private void setMetricsMarkerLogbackFilter(LoggerContext context) {
        log.info("Filtering metrics logs from all appenders except the {} appender", LOGSTASH_APPENDER_NAME);
        OnMarkerEvaluator onMarkerMetricsEvaluator = new OnMarkerEvaluator();
        onMarkerMetricsEvaluator.setContext(context);
        onMarkerMetricsEvaluator.addMarker("metrics");
        onMarkerMetricsEvaluator.start();
        EvaluatorFilter<ILoggingEvent> metricsFilter = new EvaluatorFilter<>();
        metricsFilter.setContext(context);
        metricsFilter.setEvaluator(onMarkerMetricsEvaluator);
        metricsFilter.setOnMatch(FilterReply.DENY);
        metricsFilter.start();

        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext();) {
                Appender<ILoggingEvent> appender = it.next();
                if (!appender.getName().equals(ASYNC_LOGSTASH_APPENDER_NAME)) {
                    log.debug("Filter metrics logs from the {} appender", appender.getName());
                    appender.setContext(context);
                    appender.addFilter(metricsFilter);
                    appender.start();
                }
            }
        }
    }

    /* *
     * Logback configuration is achieved by configuration file and API.
     * When configuration file change is detected, the configuration is reset.
     * This listener ensures that the programmatic configuration is also re-applied after reset.
     */
//    class LogbackLoggerContextListener extends ContextAwareBase implements LoggerContextListener {
//
//        @Override
//        public boolean isResetResistant() {
//            return true;
//        }
//
//        @Override
//        public void onStart(LoggerContext context) {
//            addLogstashAppender(context);
//        }
//
//        @Override
//        public void onReset(LoggerContext context) {
//            addLogstashAppender(context);
//        }
//
//        @Override
//        public void onStop(LoggerContext context) {
//            // Nothing to do.
//        }
//
//        @Override
//        public void onLevelChange(ch.qos.logback.classic.Logger logger, Level level) {
//            // Nothing to do.
//        }
//    }

}
