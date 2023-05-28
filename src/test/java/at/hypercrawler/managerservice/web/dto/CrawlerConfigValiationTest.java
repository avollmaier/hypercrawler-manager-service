package at.hypercrawler.managerservice.web.dto;

import at.hypercrawler.managerservice.CrawlerTestDummyProvider;
import at.hypercrawler.managerservice.domain.model.ConnectionHeader;
import at.hypercrawler.managerservice.domain.model.CrawlerConfig;
import at.hypercrawler.managerservice.domain.model.CrawlerFilterOptions;
import at.hypercrawler.managerservice.domain.model.CrawlerRequestOptions;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class CrawlerConfigValiationTest {

  private static Validator validator;

  Supplier<String> validCron = () -> "0 0 12 * * ?";

  Supplier<CrawlerConfig.CrawlerConfigBuilder> crawlerConfigBuilder =
    () -> CrawlerConfig.builder().actions(Collections.singletonList(CrawlerTestDummyProvider.crawlerAction.get()))
      .indexPrefix("crawler_").requestOptions(CrawlerTestDummyProvider.crawlerRequestOptions.get()).startUrls(CrawlerTestDummyProvider.startUrls.get())
      .schedule(validCron.get()).robotOptions(CrawlerTestDummyProvider.robotOptions.get())
      .filterOptions(CrawlerTestDummyProvider.crawlerFilterOptions.get());

  @BeforeAll
  public static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void whenIndexPrefixIsNullThenValidationFails() {
    var crawlerConfig = crawlerConfigBuilder.get().indexPrefix(null).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Index prefix could not be empty");
  }

  @Test
  void whenScheduleIsNullThenValidationSucceeds() {
    var crawlerConfig = crawlerConfigBuilder.get().schedule(null).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isEmpty();
  }

  @Test
  void whenScheduleDoesntMatchPatternThenValidationFails() {
    var crawlerConfig = crawlerConfigBuilder.get().schedule("invalid cron").build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Schedule is not valid");
  }

  @Test
  void whenScheduleMatchesPatternThenValidationSucceeds() {
    var crawlerConfig = crawlerConfigBuilder.get().schedule(validCron.get()).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isEmpty();
  }

  @Test
  void whenStartUrlsIsNullThenValidationFails() {
    var crawlerConfig = crawlerConfigBuilder.get().startUrls(null).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Start-Urls could not be empty");
  }

  @Test
  void whenAddedStartUrlIsNullThenValidationFails() {
    var startUrls = Arrays.asList("https://www.google.com", null);
    var crawlerConfig = crawlerConfigBuilder.get().startUrls(startUrls).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Start-Url could not be empty");
  }

  @Test
  void whenAddedStartUrlIsEmptyThenValidationFails() {
    var startUrls = Arrays.asList("https://www.google.com", "");
    var crawlerConfig = crawlerConfigBuilder.get().startUrls(startUrls).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Start-Url could not be empty");
  }

  @Test
  void whenSiteExclusionPatternsIsNullThenValidationSucceeds() {
    var crawlerConfig = crawlerConfigBuilder.get().filterOptions(CrawlerFilterOptions.builder().siteExclusionPatterns(null).build()).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isEmpty();
  }

  @Test
  void whenAddedSiteExclusionPatternIsNullThenValidationFails() {
    var siteExclusionPatterns = Arrays.asList("https://www.google.com", null);
    var crawlerConfig = crawlerConfigBuilder.get().filterOptions(CrawlerFilterOptions.builder().siteExclusionPatterns(siteExclusionPatterns).build()).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Exclusion-Pattern could not be empty");
  }

  @Test
  void whenAddedSiteExclusionPatternIsEmptyThenValidationFails() {
    var siteExclusionPatterns = Arrays.asList("https://www.google.com", "");
    var crawlerConfig = crawlerConfigBuilder.get().filterOptions(CrawlerFilterOptions.builder().siteExclusionPatterns(siteExclusionPatterns).build()).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Exclusion-Pattern could not be empty");
  }

  @Test
  void whenQueryParameterExclusionPatternsIsNullThenValidationSucceeds() {
    var crawlerConfig = crawlerConfigBuilder.get().filterOptions(CrawlerFilterOptions.builder().queryParameterExclusionPatterns(null).build()).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isEmpty();
  }

  @Test
  void whenAddedQueryParameterExclusionPatternIsNullThenValidationFails() {
    var queryParameterExclusionPatterns = Arrays.asList("utm_*", null);
    var crawlerConfig = crawlerConfigBuilder.get().filterOptions(CrawlerFilterOptions.builder().queryParameterExclusionPatterns(queryParameterExclusionPatterns).build()).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Ignored-Query-Parameter could not be empty");
  }

  @Test
  void whenAddedQueryParameterExclusionPatternIsEmptyThenValidationFails() {
    var queryParameterExclusionPatterns = Arrays.asList("utm_*", "");
    var crawlerConfig = crawlerConfigBuilder.get().filterOptions(CrawlerFilterOptions.builder().queryParameterExclusionPatterns(queryParameterExclusionPatterns).build()).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Ignored-Query-Parameter could not be empty");
  }

  @Test
  void whenRequestOptionsIsNullThenValidationSucceeds() {
    var crawlerConfig = crawlerConfigBuilder.get().requestOptions(null).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isEmpty();
  }

  @Test
  void whenRequestOptionRequestTimeoutIsLowerThanZeroThenValidationFails() {
    var requestOptions = CrawlerRequestOptions.builder().requestTimeout(-1).retries(1).build();
    var crawlerConfig = crawlerConfigBuilder.get().requestOptions(requestOptions).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Request timeout must be greater than 0");
  }

  @Test
  void whenRequestOptionRetriesIsLowerThanZeroThenValidationFails() {
    var requestOptions = CrawlerRequestOptions.builder().retries(-1).requestTimeout(12).build();
    var crawlerConfig = crawlerConfigBuilder.get().requestOptions(requestOptions).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Retries must be greater than 0");
  }

  @Test
  void whenRequestOptionHeadersIsNullThenValidationSucceeds() {
    var requestOptions = CrawlerRequestOptions.builder().headers(null).requestTimeout(12).retries(12).build();
    var crawlerConfig = crawlerConfigBuilder.get().requestOptions(requestOptions).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isEmpty();
  }

  @Test
  void whenAddedRequestOptionHeaderIsNullThenValidationFails() {
    var headers = Arrays.asList(new ConnectionHeader("name", "type"), null);
    var requestOptions =
      CrawlerRequestOptions.builder().requestTimeout(12).retries(12).headers(headers).build();
    var crawlerConfig = crawlerConfigBuilder.get().requestOptions(requestOptions).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Request header could not be null");
  }

  @Test
  void whenAddedRequestOptionHeaderNameIsNullThenValidationFails() {
    var headers = Arrays.asList(new ConnectionHeader(null, "type"));
    var requestOptions =
      CrawlerRequestOptions.builder().requestTimeout(12).retries(12).headers(headers).build();
    var crawlerConfig = crawlerConfigBuilder.get().requestOptions(requestOptions).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);

    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Request header name could not be null");
  }

  @Test
  void whenAddedRequestOptionHeaderTypeIsNullThenValidationFails() {
    var headers = Arrays.asList(new ConnectionHeader("name", null));
    var requestOptions =
      CrawlerRequestOptions.builder().requestTimeout(12).retries(12).headers(headers).build();
    var crawlerConfig = crawlerConfigBuilder.get().requestOptions(requestOptions).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);

    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Request header value could not be null");
  }

  @Test
  void whenRobotOptionsIsNullThenValidationSucceeds() {
    var crawlerConfig = crawlerConfigBuilder.get().robotOptions(null).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isEmpty();
  }

  @Test
  void whenActionIsNullThenValidationFails() {
    var crawlerConfig = crawlerConfigBuilder.get().actions(null).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Actions could not be empty");
  }

  @Test
  void whenAddedActionIsNullThenValidationFails() {
    var actions = Arrays.asList(CrawlerTestDummyProvider.crawlerAction.get(), null);
    var crawlerConfig = crawlerConfigBuilder.get().actions(actions).build();
    var crawlerRequest = new CrawlerRequest("Test Crawler", crawlerConfig);
    Set<ConstraintViolation<CrawlerRequest>> violations = validator.validate(crawlerRequest);
    assertThat(violations).isNotEmpty().hasSize(1).extracting(ConstraintViolation::getMessage)
      .containsExactly("Actions could not be empty");
  }

}
