package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabNotConflictException extends RuntimeException {
  public GitLabNotConflictException(String message, GitLabApiException e) {
    super(message, e);
  }
}
