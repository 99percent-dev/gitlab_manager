package dev.ixixpercent.gitlab_mananager.base;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
public class ProjectIntegrationTest extends BaseIntegrationTest {


  @Autowired
  private MockMvc mvc;

  private static String CREATE_PROJECT_BODY = """
    {
      "projectName": "test_project",
      "namespaceId":"%d",
      "description": "Description for test project"
    }
    """;

  @Test
  void createProject() throws Exception {

    mvc
      .perform(post("/project/create").content(String.format(CREATE_PROJECT_BODY, 1)).contentType(APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id").isNumber());
  }

  @Test
  void addMember() throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Test
  void removeMember() throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
