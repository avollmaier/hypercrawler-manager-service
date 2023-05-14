package at.hypercrawler.managerservice.domain.service;

import at.hypercrawler.managerservice.domain.CrawlerManagerRepository;
import at.hypercrawler.managerservice.domain.exception.CrawlerAlreadyExistsException;
import at.hypercrawler.managerservice.domain.exception.CrawlerNotFoundException;
import at.hypercrawler.managerservice.domain.model.Crawler;
import at.hypercrawler.managerservice.domain.model.CrawlerConfig;
import at.hypercrawler.managerservice.domain.model.CrawlerStatus;
import at.hypercrawler.managerservice.event.AddressSuppliedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Transactional
public class CrawlerManagerService {
    private final CrawlerManagerRepository crawlerManagerRepository;
    private final StreamBridge streamBridge;

    public CrawlerManagerService(CrawlerManagerRepository crawlerManagerRepository, StreamBridge streamBridge) {
        this.crawlerManagerRepository = crawlerManagerRepository;
        this.streamBridge = streamBridge;
    }

    public Flux<Crawler> findAll() {
        return crawlerManagerRepository.findAll();
    }

    public Mono<Crawler> findById(UUID uuid) {
        return crawlerManagerRepository.findById(uuid).switchIfEmpty(Mono.error(new CrawlerNotFoundException(uuid)));
    }

    public Mono<Crawler> createCrawler(Crawler crawler) {
        return crawlerManagerRepository.existsById(crawler.id()).flatMap(exists -> {
            if (Boolean.TRUE.equals(exists)) {
                return Mono.error(new CrawlerAlreadyExistsException(crawler.id()));
            }
            return crawlerManagerRepository.save(crawler);
        });
    }

    public Mono<Crawler> startCrawler(UUID uuid) {
        return updateCrawlerStatus(uuid, CrawlerStatus.RUNNING);
    }

    public Mono<Crawler> stopCrawler(UUID uuid) {
        return updateCrawlerStatus(uuid, CrawlerStatus.STOPPED);
    }

    public Mono<Void> deleteCrawler(UUID uuid) {
        return crawlerManagerRepository.deleteById(uuid);
    }

    public Mono<Crawler> updateCrawler(UUID uuid, String name, CrawlerConfig config) {
        Function<Crawler, Crawler> updateCrawler = c -> new Crawler(c.id(), name, c.status(), config, c.createdAt(), c.updatedAt(), c.version());
        return crawlerManagerRepository.findById(uuid)
                .map(updateCrawler)
                .flatMap(crawlerManagerRepository::save)
                .switchIfEmpty(Mono.error(new CrawlerNotFoundException(uuid)));
    }

    private Mono<Crawler> updateCrawlerStatus(UUID uuid, CrawlerStatus status) {
        Function<Crawler, Crawler> applyStatus = c -> new Crawler(c.id(), c.name(), status, c.config(), c.createdAt(), c.updatedAt(), c.version());
        return crawlerManagerRepository.findById(uuid)
                .map(applyStatus)
                .flatMap(crawlerManagerRepository::save)
                .switchIfEmpty(Mono.error(new CrawlerNotFoundException(uuid)))
                .doOnNext(crawler -> {
                    if (status == CrawlerStatus.RUNNING) {
                        publishAddressSupplyEvent(crawler);
                    }
                });
    }

    private void publishAddressSupplyEvent(Crawler crawler) {
        UUID id = crawler.id();

        for (String address : crawler.config().startUrls()) {
            try {
                var addressSupplyMessage = new AddressSuppliedMessage(id, new URL(address));
                log.info("Sending data with address {} of crawler with id: {}", address, id);

                var result = streamBridge.send("supplyAddress-out-0", addressSupplyMessage);
                log.info("Result of sending address {} for crawler with id: {} is {}", address, id, result);

            } catch (Exception e) {
                log.warn("Address {} is not a valid URL", address);
            }
        }
    }
}