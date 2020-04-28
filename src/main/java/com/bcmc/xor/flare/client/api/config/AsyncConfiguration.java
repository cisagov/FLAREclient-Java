package com.bcmc.xor.flare.client.api.config;

import io.github.jhipster.async.ExceptionHandlingAsyncTaskExecutor;
import io.github.jhipster.config.JHipsterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    @Autowired
    private JHipsterProperties jHipsterProperties;

    public AsyncConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(jHipsterProperties.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(jHipsterProperties.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(jHipsterProperties.getAsync().getQueueCapacity());
        executor.setThreadNamePrefix("async-exec-");
        log.debug("Creating Async Task Executor. Core pool size: {}, Max pool size: {}, Queue Capacity: {}",
            jHipsterProperties.getAsync().getCorePoolSize(),
            jHipsterProperties.getAsync().getMaxPoolSize(),
            jHipsterProperties.getAsync().getQueueCapacity());
        executor.initialize();
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
