package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabUnauthorizedException extends GitLabException {
  public GitLabUnauthorizedException(String message, GitLabApiException e) {
    super(message, e);
  }
}
