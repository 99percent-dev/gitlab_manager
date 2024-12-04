package dev.ixixpercent.gitlab_manager.requests;

public record RemoveUserFromProjectRequest(String projectId, String userId) {}