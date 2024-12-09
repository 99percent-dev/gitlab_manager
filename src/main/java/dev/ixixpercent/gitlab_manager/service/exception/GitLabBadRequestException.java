package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabBadRequestException extends GitLabException {
  public GitLabBadRequestException(String message, GitLabApiException e) {
    super(message, e);
  }
}
