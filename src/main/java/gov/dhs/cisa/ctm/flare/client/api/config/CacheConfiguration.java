package gov.dhs.cisa.ctm.flare.client.api.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gov.dhs.cisa.ctm.flare.client.api.repository.CollectionRepository;
import gov.dhs.cisa.ctm.flare.client.api.repository.EventRepository;
import gov.dhs.cisa.ctm.flare.client.api.repository.StatusRepository;

import java.time.Duration;

@SuppressWarnings("unused")
@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration() {

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                ResourcePoolsBuilder.heap(100))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(3600))) //1 hour
                .build());
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            cm.createCache(gov.dhs.cisa.ctm.flare.client.api.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
            cm.createCache(gov.dhs.cisa.ctm.flare.client.api.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
            cm.createCache(gov.dhs.cisa.ctm.flare.client.api.repository.ServerRepository.SERVERS_BY_LABEL_CACHE, jcacheConfiguration);
            cm.createCache(gov.dhs.cisa.ctm.flare.client.api.repository.ServerRepository.SERVERS_BY_ID_CACHE, jcacheConfiguration);
            cm.createCache(EventRepository.EVENTS_BY_ID_CACHE, jcacheConfiguration);
            cm.createCache(StatusRepository.STATUS_BY_ID_CACHE, jcacheConfiguration);
            cm.createCache(CollectionRepository.COLLECTIONS_BY_ID_CACHE, jcacheConfiguration);
        };
    }
}
