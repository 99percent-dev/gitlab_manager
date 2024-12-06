package dev.ixixpercent.gitlab_manager.controller;

import dev.ixixpercent.gitlab_manager.api.ProjectApi;
import dev.ixixpercent.gitlab_manager.model.AddUserToProject;
import dev.ixixpercent.gitlab_manager.model.CreateProject;
import dev.ixixpercent.gitlab_manager.model.Project;
import dev.ixixpercent.gitlab_manager.model.RemoveUserFromProject;
import dev.ixixpercent.gitlab_manager.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class ProjectController implements ProjectApi {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }


  @Override
  public ResponseEntity<Void> addUserToProject(AddUserToProject user) {
    projectService.addUserToProject(user);
    return ok().build();
  }

  @Override
  public ResponseEntity<Project> createProject(CreateProject project) {
    return ok(new Project(projectService.createProject(project)));
  }

  @Override
  public ResponseEntity<Void> projectUserRemoveDelete(RemoveUserFromProject user) {
    projectService.removeUserFromProject(user);
    return ok().build();
  }
}
