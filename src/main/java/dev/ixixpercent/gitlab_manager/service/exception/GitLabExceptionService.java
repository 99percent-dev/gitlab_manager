package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class GitLabExceptionService {

  public <V, K> RuntimeException generateException(Map<K, V> clarifyingInfo, GitLabApiException e) {
    int httpStatus = e.getHttpStatus();
    String message = Optional.ofNullable(clarifyingInfo).orElse(Collections.emptyMap()).toString();
    switch (httpStatus) {
      case 400:
        return new GitLabBadRequestException(message, e);
      case 401:
        return new GitLabUnauthorizedException(message, e);
      case 403:
        return new GitLabForbbidenException(message, e);
      case 404:
        return new GitLabNotFoundException(message, e);
      case 409:
        return new GitLabConflictException(message, e);
      case 408:
        return new GitLabRequestTimeoutException(message, e);
      case 418:
        return new GitLabServerIsATeapot(message, e);
      case 500:
        return new GitLabServerErrorException(message, e);
      default:
        return new GitLabException(message, e);
    }
  }
}
