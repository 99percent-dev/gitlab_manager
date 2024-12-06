package dev.ixixpercent.gitlab_manager.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@Getter
@ToString
public class User {
  private final Role role;
  private final String id;

  public User(String id, Role role) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("id cannot be null or empty");
    }
    this.role = role;
    this.id = id;
  }
}
