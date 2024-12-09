package dev.ixixpercent.gitlab_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ixixpercent.gitlab_manager.model.AddUserToProject;
import dev.ixixpercent.gitlab_manager.model.CreateProject;
import dev.ixixpercent.gitlab_manager.model.RemoveUserFromProject;
import dev.ixixpercent.gitlab_manager.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProjectController.class)
class ProjectControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProjectService projectService;

  @Autowired
  private ObjectMapper objectMapper;

  private AddUserToProject addUserToProject;
  private CreateProject createProject;
  private RemoveUserFromProject removeUserFromProject;

  @BeforeEach
  void setUp() {
    // Initialize test data
    addUserToProject = new AddUserToProject();
    addUserToProject.setProjectId(1L);
    addUserToProject.setUserId(100L);

    createProject = new CreateProject();
    createProject.setProjectName("Integration Test Project");
    createProject.setNamespaceId(10L);
    createProject.setDescription("Description for Integration Test Project");

    removeUserFromProject = new RemoveUserFromProject();
    removeUserFromProject.setProjectId(1L);
    removeUserFromProject.setUserId(100L);
  }

  @Test
  @DisplayName("POST /addUserToProject should add user and return 200 OK")
  void addUserToProjectEndpoint() throws Exception {

    mockMvc
      .perform(post("/project/user/add")
                 .contentType(APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(addUserToProject)))
      .andExpect(status().isOk())
      .andExpect(content().string("")); // Expect empty body for Void response

    verify(projectService, times(1)).addUserToProject(addUserToProject);
  }

  @Test
  @DisplayName("POST /createProject should create project and return Project with ID")
  void createProjectEndpoint() throws Exception {
    // Arrange
    long mockProjectId = 300L;
    when(projectService.createProject(any(CreateProject.class))).thenReturn(mockProjectId);


    mockMvc
      .perform(post("/project/create")
                 .contentType(APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(createProject)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(mockProjectId));


    verify(projectService, times(1)).createProject(createProject);
  }

  @Test
  @DisplayName("DELETE /projectUserRemoveDelete should remove user and return 200 OK")
  void removeUserFromProjectEndpoint() throws Exception {

    mockMvc
      .perform(delete("/project/user/remove")
                 .contentType(APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(removeUserFromProject)))
      .andExpect(status().isOk())
      .andExpect(content().string("")); // Expect empty body for Void response


    verify(projectService, times(1)).removeUserFromProject(removeUserFromProject);
  }

  @Test
  @DisplayName("POST /createProject should handle service exceptions and return 500 Internal Server Error")
  void createProjectEndpointException() throws Exception {
    // Arrange
    when(projectService.createProject(any(CreateProject.class))).thenThrow(new RuntimeException("Service error"));

    mockMvc
      .perform(post("/project/create")
                 .contentType(APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(createProject)))
      .andExpect(status().isInternalServerError());

    verify(projectService, times(1)).createProject(createProject);
  }
}
