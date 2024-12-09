package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class GitLabExceptionServiceTest {

  private GitLabExceptionService exceptionService;
  private GitLabApiException gitLabApiException;
  private Map<String, Object> clarifyingInfo;

  @BeforeEach
  void setUp() {
    exceptionService = new GitLabExceptionService();
    gitLabApiException = mock(GitLabApiException.class);
    clarifyingInfo = new HashMap<>();
    clarifyingInfo.put("key", "value");
  }

  @TestFactory
  @DisplayName("generateException Dynamic Tests")
  Stream<DynamicTest> generateExceptionTests() {
    return Stream.of(dynamicTest("Should return GitLabBadRequestException for HTTP 400",
                                 () -> test(400, GitLabBadRequestException.class)),
                     dynamicTest("Should return GitLabUnauthorizedException for HTTP 401",
                                 () -> test(401, GitLabUnauthorizedException.class)),
                     dynamicTest("Should return GitLabForbiddenException for HTTP 403",
                                 () -> test(403, GitLabForbbidenException.class)),
                     dynamicTest("Should return GitLabNotFoundException for HTTP 404",
                                 () -> test(404, GitLabNotFoundException.class)),
                     dynamicTest("Should return GitLabNotConflictException for HTTP 409",
                                 () -> test(409, GitLabNotConflictException.class)),
                     dynamicTest("Should return GitLabRequestTimeoutException for HTTP 408",
                                 () -> test(408, GitLabRequestTimeoutException.class)),
                     dynamicTest("Should return GitLabServerIsATeapot for HTTP 418",
                                 () -> test(418, GitLabServerIsATeapot.class)),
                     dynamicTest("Should return GitLabServerErrorException for HTTP 500",
                                 () -> test(500, GitLabServerErrorException.class)),
                     dynamicTest("Should return GitLabException for unknown HTTP status",
                                 () -> test(999, GitLabException.class)),
                     dynamicTest("Should handle empty clarifyingInfo map",
                                 () -> test(400, GitLabBadRequestException.class)));
  }

  private void test(Integer httpStatus, Class<? extends Object> expectedExceptionClass) {
    when(gitLabApiException.getHttpStatus()).thenReturn(httpStatus);
    RuntimeException exception = exceptionService.generateException(clarifyingInfo, gitLabApiException);
    assertInstanceOf(expectedExceptionClass,
                     exception,
                     "Expected exception of type " + expectedExceptionClass.getSimpleName());
    assertEquals(clarifyingInfo.toString(),
                 exception.getMessage(),
                 "Exception message should match clarifyingInfo.toString()");
    assertEquals(gitLabApiException, exception.getCause(), "Exception cause should be the original GitLabApiException");
  }
}
