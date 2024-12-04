package dev.ixixpercent.gitlab_manager.service;

import dev.ixixpercent.gitlab_manager.model.AddUserToProject;
import dev.ixixpercent.gitlab_manager.model.CreateProject;
import dev.ixixpercent.gitlab_manager.model.Project;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
  public ResponseEntity<Void> addUserToProject(AddUserToProject user) {
    return null;
  }

  public ResponseEntity<Project> createProject(CreateProject project) {
    return null;
  }

  public ResponseEntity<Void> removeUserFromProject() {
    return null;
  }
}
