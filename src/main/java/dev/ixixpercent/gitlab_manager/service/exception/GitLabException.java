package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabException extends RuntimeException {
  public GitLabException(String message, GitLabApiException e) {
    super(message, e);
  }
}
