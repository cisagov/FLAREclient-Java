package com.bcmc.xor.flare.client.api.config;

import com.bcmc.xor.flare.client.api.repository.CollectionRepository;
import com.bcmc.xor.flare.client.api.repository.EventRepository;
import com.bcmc.xor.flare.client.api.repository.StatusRepository;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Not putting time into fixing this class because a Cache seems unnecessary for a client
 */
//@SuppressWarnings("unused")
//@Configuration
//@EnableCaching
public class CacheConfiguration {

//    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;
//
//    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
//        JHipsterProperties.Cache.Ehcache ehcache =
//            jHipsterProperties.getCache().getEhcache();
//
//        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
//            CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
//                ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
//                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
//                .build());
//    }
//
//    @Bean
//    public JCacheManagerCustomizer cacheManagerCustomizer() {
//        return cm -> {
//            cm.createCache(com.bcmc.xor.flare.client.api.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
//            cm.createCache(com.bcmc.xor.flare.client.api.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
//            cm.createCache(com.bcmc.xor.flare.client.api.repository.ServerRepository.SERVERS_BY_LABEL_CACHE, jcacheConfiguration);
//            cm.createCache(com.bcmc.xor.flare.client.api.repository.ServerRepository.SERVERS_BY_ID_CACHE, jcacheConfiguration);
//            cm.createCache(EventRepository.EVENTS_BY_ID_CACHE, jcacheConfiguration);
//            cm.createCache(StatusRepository.STATUS_BY_ID_CACHE, jcacheConfiguration);
//            cm.createCache(CollectionRepository.COLLECTIONS_BY_ID_CACHE, jcacheConfiguration);
//            // jhipster-needle-ehcache-add-entry
//        };
//    }
}
