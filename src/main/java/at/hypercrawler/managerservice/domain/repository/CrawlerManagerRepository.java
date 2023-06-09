package at.hypercrawler.managerservice.domain.repository;

import at.hypercrawler.managerservice.domain.model.Crawler;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CrawlerManagerRepository
        extends ReactiveMongoRepository<Crawler, UUID> {
}