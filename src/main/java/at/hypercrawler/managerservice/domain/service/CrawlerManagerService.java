package at.hypercrawler.managerservice.domain.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import at.hypercrawler.managerservice.event.AddressSuppliedMessage;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.hypercrawler.managerservice.domain.CrawlerManagerRepository;
import at.hypercrawler.managerservice.domain.exception.CrawlerAlreadyExistsException;
import at.hypercrawler.managerservice.domain.exception.CrawlerNotFoundException;
import at.hypercrawler.managerservice.domain.model.Crawler;
import at.hypercrawler.managerservice.web.dto.CrawlerConfig;
import at.hypercrawler.managerservice.web.dto.CrawlerStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return crawlerManagerRepository.existsById(crawler.getId()).flatMap(exists -> {
            if (Boolean.TRUE.equals(exists)) {
                return Mono.error(new CrawlerAlreadyExistsException(crawler.getId()));
            }
            return crawlerManagerRepository.save(crawler);
        });
    }

    public Mono<Crawler> startCrawler(UUID uuid) {
        return updateCrawlerStatus(uuid, CrawlerStatus.STARTED);
    }

    public Mono<Crawler> stopCrawler(UUID uuid) {
        return updateCrawlerStatus(uuid, CrawlerStatus.STOPPED);
    }

    public Mono<Void> deleteCrawler(UUID uuid) {
        return crawlerManagerRepository.deleteById(uuid);
    }

    public Mono<Crawler> updateCrawler(UUID uuid, String name, CrawlerConfig config) {
        UnaryOperator<Crawler> updateCrawler =
                c -> new Crawler(c.getId(), name, config, c.getStatus(), c.getCreatedAt(), c.getUpdatedAt(),
                        c.getVersion());
        return crawlerManagerRepository.findById(uuid).map(updateCrawler)
                .flatMap(crawlerManagerRepository::save)
                .switchIfEmpty(Mono.error(new CrawlerNotFoundException(uuid)));
    }

    private Mono<Crawler> updateCrawlerStatus(UUID uuid, CrawlerStatus status) {
        UnaryOperator<Crawler> applyStatus =
                c -> new Crawler(c.getId(), c.getName(), c.getConfig(), status, c.getCreatedAt(), c.getUpdatedAt(),
                        c.getVersion());

        return crawlerManagerRepository.findById(uuid).map(applyStatus).flatMap(crawlerManagerRepository::save)
                .switchIfEmpty(Mono.error(new CrawlerNotFoundException(uuid))).doOnNext(crawler -> {
                    if (status == CrawlerStatus.STARTED) {
                        publishAddressSupplyEvent(crawler);
                    }
                });
    }

    private void publishAddressSupplyEvent(Crawler crawler) {
        UUID id = crawler.getId();

        identifyPublishAdresses(crawler).forEach(address -> {
            var addressSupplyMessage = new AddressSuppliedMessage(id, address);
            log.info("Sending data with address {} of crawler with id: {}", address, id);

            var result = streamBridge.send("supplyAddress-out-0", addressSupplyMessage);
            log.info("Result of sending address {} for crawler with id: {} is {}", address, id, result);
        });
    }

    private List<URL> identifyPublishAdresses(Crawler crawler) {
        List<URL> publishAdresses = new ArrayList<>();

        for (String address : crawler.getConfig().getStartUrls()) {
            try {
                publishAdresses.add(new URL(address));
            } catch (MalformedURLException e) {
                log.warn("Error while parsing address: {} with error: {}", address, e.getMessage());
            }
        }

        return publishAdresses;
    }
}
