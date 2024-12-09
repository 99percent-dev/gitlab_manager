package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabConflictException extends GitLabException {
  public GitLabConflictException(String message, GitLabApiException e) {
    super(message, e);
  }
}
