package dev.ixixpercent.gitlab_manager.service;


import dev.ixixpercent.gitlab_manager.service.exception.GitLabExceptionService;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.gitlab4j.api.models.AccessLevel.DEVELOPER;
import static org.gitlab4j.api.models.AccessLevel.MAINTAINER;
import static org.gitlab4j.api.models.Visibility.PRIVATE;


//TODO maybe add validation

@Slf4j
@Service
public class GitLabProjectService {


  @Value("${gitlab.admin}")
  private long admin;

  private final GitLabApi gitLabApi;
  private final GitLabExceptionService exceptionService;

  public GitLabProjectService(GitLabApi gitLabApi, GitLabExceptionService exceptionService) {
    this.gitLabApi = gitLabApi;
    this.exceptionService = exceptionService;
  }

  public Project createProject(String projectName, long namespaceId, String projectDescription) {
    try {
      Project project = gitLabApi
        .getProjectApi()
        .createProject(new Project()
                         .withNamespaceId(namespaceId)
                         .withName(projectName)
                         .withDescription(projectDescription)
                         .withVisibility(PRIVATE));
      gitLabApi.getProjectApi().addMember(project.getId(), admin, MAINTAINER);
      log.info("Created project {}", project);
      return project;
    } catch (GitLabApiException e) {
      throw exceptionService.generateException(Map.of("Project name",
                                                      projectName,
                                                      "Namespace Id",
                                                      namespaceId,
                                                      "Description",
                                                      projectDescription), e);
    }
  }


  public void addUser(long projectId, long userId) {
    try {
      gitLabApi.getProjectApi().addMember(projectId, userId, DEVELOPER);

    } catch (GitLabApiException e) {
      throw exceptionService.generateException(Map.of("Project id ", projectId, "User Id", userId), e);
    }
  }

  public void removeUser(long projectId, long userId) {
    try {
      gitLabApi.getProjectApi().removeMember(projectId, userId);
    } catch (GitLabApiException e) {
      throw exceptionService.generateException(Map.of("Project id ", projectId, "User Id", userId), e);
    }
  }
}
