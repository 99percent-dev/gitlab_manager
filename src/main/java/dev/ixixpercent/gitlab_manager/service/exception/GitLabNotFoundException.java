package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabNotFoundException extends RuntimeException {
  public GitLabNotFoundException(String message, GitLabApiException e) {
    super(message, e);
  }
}
