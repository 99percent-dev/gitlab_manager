package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabNotFoundException extends GitLabException {
  public GitLabNotFoundException(String message, GitLabApiException e) {
    super(message, e);
  }
}
