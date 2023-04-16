package at.hypercrawler.supplierservice.web;

import at.hypercrawler.supplierservice.domain.model.CrawlerConfig;
import at.hypercrawler.supplierservice.domain.model.SupportedFileType;
import at.hypercrawler.supplierservice.web.dto.CrawlerRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class CrawlerRequestValiationTest {

    private static Validator validator;

    Supplier<List<String>> startUrls = () -> Arrays.asList("https://www.google.com", "https://www.bing.com");
    Supplier<List<SupportedFileType>> fileTypesToMatch = () -> Arrays.asList(SupportedFileType.HTML, SupportedFileType.PDF);
    Supplier<List<String>> pathsToMatch = () -> Arrays.asList("/path/to/file.html", "/test2/**");
    Supplier<List<String>> selectorsToMatch = () -> Arrays.asList("selector1", "selector2");
    Supplier<CrawlerConfig> crawlerConfig = () -> new CrawlerConfig(startUrls.get(), fileTypesToMatch.get(), pathsToMatch.get(), selectorsToMatch.get());
    Supplier<CrawlerRequest> crawlerRequest = () -> new CrawlerRequest("Test Crawler", crawlerConfig.get());

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsCorrectThenValidationSucceeds() {
        Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest.get());
        assertThat(violations).isEmpty();
    }

    @Test
    void whenNameIsNullThenValidationFails() {
        var crawlerRequest = new CrawlerRequest(null, null);
        Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenNameIsEmptyThenValidationFails() {
        var crawlerRequest = new CrawlerRequest("", crawlerConfig.get());
        Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenConfigIsNullThenValidationFails() {
        var crawlerRequest = new CrawlerRequest("Test Crawler", null);
        Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenStartUrlsIsNullThenValidationFails() {
        var crawlerConfig = new CrawlerConfig(null, fileTypesToMatch.get(), pathsToMatch.get(), selectorsToMatch.get());
        var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
        Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenEntryOfStartUrlsIsNullThenValidationFails() {
        var startUrls = Arrays.asList("https://www.google.com", null);
        var crawlerConfig = new CrawlerConfig(startUrls, fileTypesToMatch.get(), pathsToMatch.get(), selectorsToMatch.get());
        var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
        Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenEntryOfStartUrlsIsEmptyThenValidationFails() {
        var startUrls = Arrays.asList("https://www.google.com", "");
        var crawlerConfig = new CrawlerConfig(startUrls, fileTypesToMatch.get(), pathsToMatch.get(), selectorsToMatch.get());
        var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
        Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenEntryOfFileTypesToMatchIsNullThenValidationFails() {
        var fileTypesToMatch = Arrays.asList(SupportedFileType.HTML, null);
        var crawlerConfig = new CrawlerConfig(startUrls.get(), fileTypesToMatch, pathsToMatch.get(), selectorsToMatch.get());
        var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
        Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
        assertThat(violations).isNotEmpty();
    }

}