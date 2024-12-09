package dev.ixixpercent.gitlab_mananager.base;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ixixpercent.gitlab_manager.GitLabManagerApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMinutes;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;


@Slf4j
@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = {BaseIntegrationTest.Initializer.class})
@SpringBootTest(classes = GitLabManagerApplication.class, webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
public class BaseIntegrationTest {

  private static final GenericContainer<?> GITLAB_CONTAINER;
  private static final String GITLAB_CONTAINER_IMAGE_VERSION = "gitlab/gitlab-ee:17.6.1-ee.0";
  private static final int GITLAB_PORT = 80;
  private static final ObjectMapper objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static TestUser testUser;


  /**
   * Root password for test gitlab when running integration tests. Must be at least 8 characters long.
   */
  public static final String ROOT_PASSWORD = UUID.randomUUID().toString();

  public static final String TEST_USER_PASSWORD = UUID.randomUUID().toString();

  static {
    log.info("Starting gitlab testcontainer");
    GITLAB_CONTAINER = new GenericContainer<>(DockerImageName.parse(GITLAB_CONTAINER_IMAGE_VERSION))
      .withEnv("GITLAB_ROOT_PASSWORD", ROOT_PASSWORD)
      .withExposedPorts(GITLAB_PORT)
      .withLogConsumer(new Slf4jLogConsumer(log))
      .waitingFor(Wait.forHttp("/").forPort(80).forStatusCode(200).withStartupTimeout(ofMinutes(10)));

    GITLAB_CONTAINER.start();
    log.info("Gitlab testcontainer started");
  }

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues
        .of("gitlab.url=http://" + GITLAB_CONTAINER.getHost() + ":" + GITLAB_CONTAINER.getMappedPort(GITLAB_PORT))
        .applyTo(configurableApplicationContext.getEnvironment());
    }

  }

  @BeforeAll
  public static void setup() throws Exception {
    log.info("Setting up gitlab test data");
    String baseUrl = "http://" + GITLAB_CONTAINER.getHost() + ":" + GITLAB_CONTAINER.getMappedPort(GITLAB_PORT);
    String token = authenticateAsRoot(baseUrl, ROOT_PASSWORD);

    log.info("Using token: {}", token);
    testUser = createGitLabUser(baseUrl, token, "test-user", "test@example.com", TEST_USER_PASSWORD, "test_user_name");

    //    // Create a test project
//    createGitLabProject(baseUrl, token, "test-project");
  }

  public static TestUser getTestUser() {
    return testUser;
  }

  /**
   * Attempts to authenticate as the root user via the OAuth token endpoint using username/password.
   * This requires that the OAuth password grant flow is enabled and the root password is known.
   *
   * @param baseUrl      The base URL of the GitLab instance (e.g., "http://localhost:8080").
   * @param rootPassword The root user's password.
   * @return An OAuth access token if successful.
   * @throws IOException          If an I/O error occurs when sending or receiving.
   * @throws InterruptedException If the operation is interrupted.
   * @throws RuntimeException     If authentication fails.
   */
  public static String authenticateAsRoot(String baseUrl, String rootPassword) throws Exception {
    log.info("Authenticating to test environment {} with password {}", baseUrl, rootPassword);

    HttpClient client = HttpClient.newHttpClient();

    String formData = "grant_type=password&username=root&password=" + URLEncoder.encode(rootPassword, UTF_8);

    HttpRequest request = HttpRequest
      .newBuilder()
      .uri(URI.create(baseUrl + "/oauth/token"))
      .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
      .POST(HttpRequest.BodyPublishers.ofString(formData))
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    log.info("Authentication response {}", response.body());

    return objectMapper.readValue(response.body(), TokenResponse.class).access_token;
  }


  /**
   * Creates a GitLab user using the GitLab API.
   *
   * @param baseUrl  The base URL of the GitLab instance (e.g. http://localhost:8080).
   * @param token    A private or personal access token with admin privileges.
   * @param username The username for the new user.
   * @param email    The email address for the new user.
   * @param password The password for the new user.
   * @param name     The display name for the new user.
   * @throws java.io.IOException  If an I/O error occurs when sending or receiving.
   * @throws InterruptedException If the operation is interrupted.
   */
  public static TestUser createGitLabUser(String baseUrl,
                                          String token,
                                          String username,
                                          String email,
                                          String password,
                                          String name) throws IOException, InterruptedException {
    log.info("Creating test user");

    HttpClient client = HttpClient.newHttpClient();

    // Prepare the form data for user creation
    String formData = "email="
                      + encode(email, UTF_8)
                      + "&username="
                      + encode(username, UTF_8)
                      + "&name="
                      + encode(name, UTF_8)
                      + "&password="
                      + encode(password, UTF_8);

    HttpRequest request = HttpRequest
      .newBuilder()
      .uri(URI.create(baseUrl + "/api/v4/users"))
      .header("Authorization", "Bearer " + token)
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(formData))
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() >= 300) {
      throw new RuntimeException("Failed to create user. Status: "
                                 + response.statusCode()
                                 + ", Body: "
                                 + response.body());
    } else {
      log.info("User created successfully: {}", response.body());
      return objectMapper.readValue(response.body(), TestUser.class);
    }
  }


  /**
   * Creates a GitLab project using the GitLab API.
   *
   * @param baseUrl     The base URL of the GitLab instance.
   * @param token       A private or personal access token with privileges to create projects.
   * @param projectName The name of the project to create.
   * @throws IOException          If an I/O error occurs when sending or receiving.
   * @throws InterruptedException If the operation is interrupted.
   */
  public static void createGitLabProject(String baseUrl, String token, String projectName) throws IOException,
                                                                                                  InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    // Prepare the form data for project creation
    String formData = "name=" + encode(projectName, UTF_8);

    HttpRequest request = HttpRequest
      .newBuilder()
      .uri(URI.create(baseUrl + "/api/v4/projects"))
      .header("Authorization", "Bearer " + token)
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(formData))
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() >= 300) {
      throw new RuntimeException("Failed to create project. Status: "
                                 + response.statusCode()
                                 + ", Body: "
                                 + response.body());
    } else {
      log.info("Project created successfully: {}", response.body());
    }
  }


  public record TokenResponse(String access_token, String token_type, int expires_in, String refresh_token,
                              String scope, long created_at) {}

  public record TestUser(long id, long namespaceId, String username, String name) {}

}
