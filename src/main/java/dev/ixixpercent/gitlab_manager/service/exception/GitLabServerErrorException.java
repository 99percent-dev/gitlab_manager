package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabServerErrorException extends GitLabException {
  public GitLabServerErrorException(String message, GitLabApiException e) {
    super(message, e);
  }
}
