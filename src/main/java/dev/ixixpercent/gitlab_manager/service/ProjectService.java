package dev.ixixpercent.gitlab_manager.service;

import dev.ixixpercent.gitlab_manager.model.AddUserToProject;
import dev.ixixpercent.gitlab_manager.model.CreateProject;
import dev.ixixpercent.gitlab_manager.model.RemoveUserFromProject;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

  private final GitLabProjectService gitLabProjectService;

  public ProjectService(GitLabProjectService gitLabProjectService) {
    this.gitLabProjectService = gitLabProjectService;
  }

  public void addUserToProject(AddUserToProject user) {
    gitLabProjectService.addUser(user.getProjectId(), user.getUserId());
  }

  public long createProject(CreateProject project) {
    return gitLabProjectService
      .createProject(project.getProjectName(), project.getNamespaceId(), project.getDescription())
      .getId();
  }

  public void removeUserFromProject(RemoveUserFromProject user) {
    gitLabProjectService.removeUser(user.getProjectId(), user.getUserId());
  }
}
