package dev.ixixpercent.gitlab_manager.service.exception;

import org.gitlab4j.api.GitLabApiException;

public class GitLabServerIsATeapot extends GitLabException {
  public GitLabServerIsATeapot(String message, GitLabApiException e) {
    super(message, e);
  }
}
