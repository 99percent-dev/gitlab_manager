package dev.ixixpercent.gitlab_manager.controller;

import lombok.Getter;

@Getter
public class ErrorResponse {

  private String message;
  private String error;

  public ErrorResponse(String message, String error) {
    this.message = message;
    this.error = error;
  }
}
