package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabForbbidenException extends GitLabException {
  public GitLabForbbidenException(String message, GitLabApiException e) {
    super(message, e);
  }
}
