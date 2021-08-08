package gov.dhs.cisa.ctm.flare.client.api.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import com.github.cloudyrock.spring.v5.MongockSpring5.MongockInitializingBeanRunner;

import gov.dhs.cisa.ctm.flare.client.util.JSR310DateConverters;

@Configuration
@EnableMongoRepositories("gov.dhs.cisa.ctm.flare.client.api.repository")
@Import(value = MongoAutoConfiguration.class)
@EnableMongoAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class DatabaseConfiguration {

	@Autowired
	MongoDatabaseFactory mongoDatabaseFactory;

	@Autowired
	MongoMappingContext mappingContext;

	@Bean
	public ValidatingMongoEventListener validatingMongoEventListener() {
		return new ValidatingMongoEventListener(validator());
	}

	@Bean
	public MappingMongoConverter mappingMongoConverter() throws Exception {
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
		MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
		converter.setCustomConversions(customConversions());
		converter.setMapKeyDotReplacement(",");
		return converter;
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public MongockInitializingBeanRunner mongockInitializingBeanRunner(ApplicationContext springContext,
			MongoTemplate mongoTemplate, @Value("${mongock.lockAcquiredForMinutes:5}") long lockAcquiredForMinutes,
			@Value("${mongock.maxWaitingForLockMinutes:3}") long maxWaitingForLockMinutes,
			@Value("${mongock.maxTries:3}") int maxTries) {
		SpringDataMongo3Driver driver = SpringDataMongo3Driver.withLockSetting(mongoTemplate, lockAcquiredForMinutes,
				maxWaitingForLockMinutes, maxTries);
		return MongockSpring5.builder().setDriver(driver)
				.addChangeLogsScanPackage("gov.dhs.cisa.ctm.flare.client.api.config.dbmigrations")
				.setSpringContext(springContext).buildInitializingBeanRunner();
	}

	@Bean
	public MongoCustomConversions customConversions() {
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.add(JSR310DateConverters.DateToZonedDateTimeConverter.INSTANCE);
		converters.add(JSR310DateConverters.ZonedDateTimeToDateConverter.INSTANCE);
		return new MongoCustomConversions(converters);
	}

}