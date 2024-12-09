package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabRequestTimeoutException extends GitLabException {
  public GitLabRequestTimeoutException(String message, GitLabApiException e) {
    super(message, e);
  }
}
