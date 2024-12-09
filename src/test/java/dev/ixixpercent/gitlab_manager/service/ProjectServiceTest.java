package dev.ixixpercent.gitlab_manager.service;

import dev.ixixpercent.gitlab_manager.model.AddUserToProject;
import dev.ixixpercent.gitlab_manager.model.CreateProject;
import dev.ixixpercent.gitlab_manager.model.RemoveUserFromProject;
import dev.ixixpercent.gitlab_manager.service.exception.GitLabException;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private GitLabProjectService gitLabProjectService;

  @InjectMocks
  private ProjectService projectService;

  private AddUserToProject addUserToProject;
  private CreateProject createProject;
  private RemoveUserFromProject removeUserFromProject;

  @BeforeEach
  void setUp() {

    addUserToProject = new AddUserToProject();
    addUserToProject.setProjectId(1L);
    addUserToProject.setUserId(100L);

    createProject = new CreateProject();
    createProject.setProjectName("Test Project");
    createProject.setNamespaceId(10L);
    createProject.setDescription("Description for Test Project");

    removeUserFromProject = new RemoveUserFromProject();
    removeUserFromProject.setProjectId(1L);
    removeUserFromProject.setUserId(100L);
  }

  @Test
  @DisplayName("addUserToProject should call GitLabProjectService.addUser with correct parameters")
  void addUserToProject() {
    projectService.addUserToProject(addUserToProject);
    verify(gitLabProjectService, times(1)).addUser(addUserToProject.getProjectId(), addUserToProject.getUserId());
  }

  @Test
  @DisplayName("createProject should call GitLabProjectService.createProject and return project ID")
  void createProject() throws Exception {

    Project mockProject = new Project();
    mockProject.setId(200L);

    when(gitLabProjectService.createProject(createProject.getProjectName(),
                                            createProject.getNamespaceId(),
                                            createProject.getDescription())).thenReturn(mockProject);


    long projectId = projectService.createProject(createProject);

    verify(gitLabProjectService, times(1)).createProject(createProject.getProjectName(),
                                                         createProject.getNamespaceId(),
                                                         createProject.getDescription());

    assertEquals(200L, projectId, "Returned project ID should match the mocked project ID");
  }

  @Test
  @DisplayName("removeUserFromProject should call GitLabProjectService.removeUser with correct parameters")
  void removeUserFromProject() {

    projectService.removeUserFromProject(removeUserFromProject);

    verify(gitLabProjectService, times(1)).removeUser(removeUserFromProject.getProjectId(),
                                                      removeUserFromProject.getUserId());
  }

  @Test
  @DisplayName("addUserToProject should propagate GitLabException thrown by GitLabProjectService")
  void addUserToProjectThrowsException() throws Exception {

    doThrow(new GitLabException("Error adding user", new GitLabApiException("test exception")))
      .when(gitLabProjectService)
      .addUser(addUserToProject.getProjectId(), addUserToProject.getUserId());


    GitLabException exception = assertThrows(GitLabException.class, () -> {
      projectService.addUserToProject(addUserToProject);
    });

    assertEquals("Error adding user", exception.getMessage());
    verify(gitLabProjectService, times(1)).addUser(addUserToProject.getProjectId(), addUserToProject.getUserId());
  }

  @Test
  @DisplayName("createProject should propagate GitLabException thrown by GitLabProjectService")
  void createProjectThrowsException() throws Exception {

    when(gitLabProjectService.createProject(createProject.getProjectName(),
                                            createProject.getNamespaceId(),
                                            createProject.getDescription())).thenThrow(new GitLabException(
      "Error creating project",
      new GitLabApiException("test exception")));


    GitLabException exception = assertThrows(GitLabException.class, () -> {
      projectService.createProject(createProject);
    });

    assertEquals("Error creating project", exception.getMessage());
    verify(gitLabProjectService, times(1)).createProject(createProject.getProjectName(),
                                                         createProject.getNamespaceId(),
                                                         createProject.getDescription());
  }

  @Test
  @DisplayName("removeUserFromProject should propagate GitLabException thrown by GitLabProjectService")
  void removeUserFromProjectThrowsException() throws Exception {

    doThrow(new GitLabException("Error removing user", new GitLabApiException("test exception")))
      .when(gitLabProjectService)
      .removeUser(removeUserFromProject.getProjectId(), removeUserFromProject.getUserId());


    GitLabException exception = assertThrows(GitLabException.class, () -> {
      projectService.removeUserFromProject(removeUserFromProject);
    });

    assertEquals("Error removing user", exception.getMessage());
    verify(gitLabProjectService, times(1)).removeUser(removeUserFromProject.getProjectId(),
                                                      removeUserFromProject.getUserId());
  }
}
