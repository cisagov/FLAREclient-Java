package com.bcmc.xor.flare.client.api.config;

import com.bcmc.xor.flare.client.util.JSR310DateConverters;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import com.github.cloudyrock.spring.v5.MongockSpring5.MongockInitializingBeanRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMongoRepositories("com.bcmc.xor.flare.client.api.repository")
//@Profile("!" + JHipsterConstants.SPRING_PROFILE_CLOUD)
@Import(value = MongoAutoConfiguration.class)
@EnableMongoAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class DatabaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
        return new ValidatingMongoEventListener(validator());
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MongockInitializingBeanRunner mongockInitializingBeanRunner(
            ApplicationContext springContext,
            MongoTemplate mongoTemplate,
            @Value("${mongock.lockAcquiredForMinutes:5}") long lockAcquiredForMinutes,
            @Value("${mongock.maxWaitingForLockMinutes:3}") long maxWaitingForLockMinutes,
            @Value("${mongock.maxTries:3}") int maxTries
    ) {
        SpringDataMongo3Driver driver = SpringDataMongo3Driver.withLockSetting(
                mongoTemplate,
                lockAcquiredForMinutes,
                maxWaitingForLockMinutes,
                maxTries
        );
        return MongockSpring5
                .builder()
                .setDriver(driver)
                .addChangeLogsScanPackage("io.github.jhipster.sample.config.dbmigrations")
                .setSpringContext(springContext)
                .buildInitializingBeanRunner();
    }

    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(JSR310DateConverters.DateToZonedDateTimeConverter.INSTANCE);
        converters.add(JSR310DateConverters.ZonedDateTimeToDateConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }

}