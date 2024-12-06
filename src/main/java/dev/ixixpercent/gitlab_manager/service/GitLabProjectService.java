package dev.ixixpercent.gitlab_manager.service;


import dev.ixixpercent.gitlab_manager.service.exception.GitLabExceptionService;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.gitlab4j.api.models.AccessLevel.DEVELOPER;
import static org.gitlab4j.api.models.Visibility.PRIVATE;

@Service
public class GitLabProjectService {


  private final GitLabApi gitLabApi;
  private final GitLabExceptionService exceptionService;

  public GitLabProjectService(GitLabApi gitLabApi, GitLabExceptionService exceptionService) {
    this.gitLabApi = gitLabApi;
    this.exceptionService = exceptionService;
  }

  public Project createProject(String projectName, long namespaceId, String projectDescription) {

    Project project = new Project()
      .withNamespaceId(namespaceId)
      .withName(projectName)
      .withDescription(projectDescription)
      .withVisibility(PRIVATE);

    try {
      return gitLabApi.getProjectApi().createProject(project);
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
      //TODO maybe add validation
      // User userObj = gitLabApi.getUserApi().getUser(userId);

      gitLabApi.getProjectApi().addMember(projectId, userId, DEVELOPER);

    } catch (GitLabApiException e) {
      throw exceptionService.generateException(Map.of("Project id ", projectId, "User Id", userId), e);
    }
  }

  public void removeUser(long projectId, long userId) {
    try {
      //TODO maybe maybe add validation
      // User userObj = gitLabApi.getUserApi().getUser(userId);

      gitLabApi.getProjectApi().removeMember(projectId, userId);

    } catch (GitLabApiException e) {
      throw exceptionService.generateException(Map.of("Project id ", projectId, "User Id", userId), e);
    }
  }


  // Optional: Close the GitLabApi instance when done
  public void close() {
    gitLabApi.close();
  }

}
