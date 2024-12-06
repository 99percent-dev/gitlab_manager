package dev.ixixpercent.gitlab_manager.controller;

import dev.ixixpercent.gitlab_manager.service.exception.GitLabBadRequestException;
import dev.ixixpercent.gitlab_manager.service.exception.GitLabUnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static dev.ixixpercent.gitlab_manager.controller.ExceptionController.GitLabError.BAD_REQUEST;
import static dev.ixixpercent.gitlab_manager.controller.ExceptionController.GitLabError.UNAUTHORIZED;
import static dev.ixixpercent.gitlab_manager.controller.ExceptionController.GitLabError.UNKNOWN_ERROR;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RequiredArgsConstructor
@ControllerAdvice
public class ExceptionController {


  @ExceptionHandler(Throwable.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleAll(Exception e) {
    log.error("Unknown error", e);
    return ResponseEntity.internalServerError().body(buildErrorResponse(UNKNOWN_ERROR));
  }

  @ExceptionHandler(GitLabBadRequestException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> badRequest(GitLabBadRequestException e) {
    log.warn("Bad request to GitLab API ", e);
    return ResponseEntity.badRequest().body(buildErrorResponse(BAD_REQUEST));
  }


  @ExceptionHandler(GitLabUnauthorizedException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> unauthorized(GitLabUnauthorizedException e) {
    log.warn("Authorization to GitLab API failed, check token ", e);
    return status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse(UNAUTHORIZED));
  }


  private ErrorResponse buildErrorResponse(GitLabError error) {
    return new ErrorResponse(error.getClientMessage(), error.getCode());
  }

  public enum GitLabError {
    BAD_REQUEST("1", "Bad request to GitLab"),
    UNAUTHORIZED("2", "Authorization to GitLab failed, check token"),
    UNKNOWN_ERROR("900", "Unknown error");
    //TODO add remaining errors

    private String code;
    private String clientMessage;

    GitLabError(String code, String clientMessage) {
      this.code = code;
      this.clientMessage = clientMessage;
    }

    public String getClientMessage() {
      return clientMessage;
    }

    public String getCode() {
      return code;
    }
  }
}
