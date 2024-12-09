package dev.ixixpercent.gitlab_mananager.base;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.UUID;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMinutes;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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


  /**
   * Root password for test gitlab when running integration tests. Must be at least 8 characters long.
   */
  public static final String ROOT_PASSWORD = UUID.randomUUID().toString();
  public static final String TEST_USER_PASSWORD = UUID.randomUUID().toString();
  public static final String MAINTAINER_USER_PASSWORD = UUID.randomUUID().toString();


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
      try {
        String baseUrl = "http://" + GITLAB_CONTAINER.getHost() + ":" + GITLAB_CONTAINER.getMappedPort(GITLAB_PORT);
        String rootToken = authenticate(baseUrl, "root", ROOT_PASSWORD);

        String[] scopes = {"api"};
        String rootPat = createPersonalAccessToken(baseUrl,
                                                   1,
                                                   rootToken,
                                                   "root_pat",
                                                   LocalDate.now().plusMonths(6).toString(),
                                                   scopes);

        TestUserWithPat maintainerUser = createGitLabUser(baseUrl,
                                                          rootToken,
                                                          "maintainer",
                                                          "maintainert@example.com",
                                                          MAINTAINER_USER_PASSWORD,
                                                          "maintainer namer");


        TestPropertyValues
          .of("gitlab.url=http://" + GITLAB_CONTAINER.getHost() + ":" + GITLAB_CONTAINER.getMappedPort(GITLAB_PORT),
              "gitlab.personalAccessToken=" + rootPat,
              "gitlab.admin=" + maintainerUser.testUser().id)
          .applyTo(configurableApplicationContext.getEnvironment());

      } catch (Exception e) {
        throw new RuntimeException("Could not initialized Gitlab testcontainer environment", e);
      }
    }
  }


  @BeforeAll
  public static void setup() throws Exception {
    log.info("Setting up gitlab test data");
//    String baseUrl = "http://" + GITLAB_CONTAINER.getHost() + ":" + GITLAB_CONTAINER.getMappedPort(GITLAB_PORT);
//    String token = authenticate(baseUrl, "root", ROOT_PASSWORD);
//    log.info("Using token: {}", token);
//    testUser = createGitLabUser(baseUrl, token, "test-user", "test@example.com", TEST_USER_PASSWORD, "test_user_name");
//    // Create a test project
//    createGitLabProject(baseUrl, token, "test-project");
  }

  /**
   * Attempts to authenticate via the OAuth token endpoint using username/password.
   * This requires that the OAuth password grant flow is enabled and the root password is known.
   *
   * @param baseUrl  The base URL of the GitLab instance (e.g., "http://localhost:8080").
   * @param password The user's password.
   * @return An OAuth access token if successful.
   * @throws IOException          If an I/O error occurs when sending or receiving.
   * @throws InterruptedException If the operation is interrupted.
   * @throws RuntimeException     If authentication fails.
   */
  public static String authenticate(String baseUrl, String username, String password) throws Exception {
    log.info("Authenticating to test environment {} with user {} and password {}", baseUrl, username, password);

    HttpClient client = HttpClient.newHttpClient();

    String formData =
      "grant_type=password&username=" + encode(username, UTF_8) + "&password=" + encode(password, UTF_8);

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
  public static TestUserWithPat createGitLabUser(String baseUrl,
                                                 String token,
                                                 String username,
                                                 String email,
                                                 String password,
                                                 String name) throws Exception {
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
      .header(AUTHORIZATION, "Bearer " + token)
      .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
      .POST(HttpRequest.BodyPublishers.ofString(formData))
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    String body = response.body();
    if (response.statusCode() >= 300) {
      throw new RuntimeException("Failed to create user. Status: " + response.statusCode() + ", Body: " + body);
    } else {
      log.info("User created successfully: {}", body);
      TestUser testUser = objectMapper.readValue(body, TestUser.class);
      String[] scopes = {};
      String pat = createPersonalAccessToken(baseUrl,
                                             testUser.id,
                                             authenticate(baseUrl, testUser.username, TEST_USER_PASSWORD),
                                             "test_pat",
                                             LocalDate.now().plusMonths(6).toString(),
                                             scopes);
      return new TestUserWithPat(testUser, pat);
    }
  }


  /**
   * Creates a Personal Access Token for a specified user using form data.
   *
   * @param baseUrl    The base URL of the GitLab instance (e.g., "http://localhost:8080").
   * @param userId     The ID of the user.
   * @param oauthToken The OAuth token with admin privileges.
   * @param name       The name of the PAT.
   * @param expiresAt  The expiration date in YYYY-MM-DD format.
   * @param scopes     The scopes for the PAT.
   * @return The created PAT as a String.
   * @throws Exception If an error occurs during the HTTP request or response processing.
   */
  public static String createPersonalAccessToken(String baseUrl,
                                                 long userId,
                                                 String oauthToken,
                                                 String name,
                                                 String expiresAt,
                                                 String[] scopes) throws Exception {

    String formData = buildFormData(name, expiresAt, scopes);

    log.info("Form Data: {}", formData);

//    POST /users/:user_id/personal_access_tokens
    // Build the HTTP request
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest
      .newBuilder()
      .uri(URI.create(baseUrl + "/api/v4/users/" + userId + "/personal_access_tokens"))
      .header(AUTHORIZATION, "Bearer " + oauthToken)
      .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
      .POST(HttpRequest.BodyPublishers.ofString(formData))
      .build();

    // Send the request
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    log.info("Personal authentication token request: {}", request);

    // Log the response body
    log.info("Personal Access Token creation response: {}", response.body());

    // Handle the response
    if (response.statusCode() == 201) {
      // Parse the response to extract the token
      ObjectNode responseBody = (ObjectNode) objectMapper.readTree(response.body());
      if (responseBody.has("token")) {
        return responseBody.get("token").asText();
      } else {
        throw new RuntimeException("Token not found in the response.");
      }
    } else {
      throw new RuntimeException("Failed to create PAT. Status Code: "
                                 + response.statusCode()
                                 + ", Response Body: "
                                 + response.body());
    }
  }

  /**
   * Builds a URL-encoded form data string from the provided parameters.
   *
   * @param name      The name of the PAT.
   * @param expiresAt The expiration date in YYYY-MM-DD format.
   * @param scopes    The scopes for the PAT.
   * @return A URL-encoded form data string.
   */
  private static String buildFormData(String name, String expiresAt, String[] scopes) throws Exception {

    StringBuilder formBuilder = new StringBuilder();
    formBuilder.append("name=").append(encode(name, UTF_8));
    formBuilder.append("&expires_at=").append(encode(expiresAt, UTF_8));
    for (String scope : scopes) {
      formBuilder.append("&scopes[]=").append(encode(scope, UTF_8));
    }

    return formBuilder.toString();
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
      .header(AUTHORIZATION, "Bearer " + token)
      .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
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


  public record TestUserWithPat(TestUser testUser, String pat) {}

  public record TestUser(long id, long namespaceId, String username, String name) {}

}
