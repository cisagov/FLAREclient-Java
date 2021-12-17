package com.bcmc.xor.flare.client.api.repository;

import com.bcmc.xor.flare.client.api.domain.async.AsyncFetch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unused")
@Component
class DatabaseSync {

    @Autowired
    private
    AsyncFetchRequestRepository asyncFetchRequestRepository;

    @EventListener(ContextRefreshedEvent.class)
    void contextRefreshedEvent() {
        List<AsyncFetch> fetches = asyncFetchRequestRepository.findAllByStatus(AsyncFetch.Status.FETCHING);
        fetches.forEach(fetch -> fetch.setStatus(AsyncFetch.Status.PENDING));
        asyncFetchRequestRepository.saveAll(fetches);
    }
}
