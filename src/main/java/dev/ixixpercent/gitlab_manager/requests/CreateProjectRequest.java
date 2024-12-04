package dev.ixixpercent.gitlab_manager.requests;

public record CreateProjectRequest(String projectName, String namespaceId, String description) {}