package dev.ixixpercent.gitlab_manager.domain;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Project {

  private User maintainer;
  private Set<User> members = new ConcurrentSkipListSet<>();

  public Project(User maintainer) {
    this.maintainer = maintainer;
  }

  public void addUser(User user) {
    members.add(user);
  }

  public void removeUser(User user) {
    members.remove(user);
  }
}
