package dev.ixixpercent.gitlab_manager.configuration;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitLabConfiguration {

  @Value("${gitlab.url}")
  private String gitLabUrl;

  @Value("${gitlab.personalAccessToken}")
  private String personalAccessToken;


  @Bean
  public GitLabApi gitLabApi() {
    // TODO check thread safety and wrap in thread safe class if needed
    return new GitLabApi(gitLabUrl, personalAccessToken);
  }
}
