package dev.ixixpercent.gitlab_manager.requests;

public record AddUserToProjectRequest(String projectId, String userId) {}