package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabRequestTimeoutException extends RuntimeException {
  public GitLabRequestTimeoutException(String message, GitLabApiException e) {
    super(message, e);
  }
}
