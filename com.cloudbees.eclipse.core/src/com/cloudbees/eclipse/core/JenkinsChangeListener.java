package com.cloudbees.eclipse.core;

import java.util.List;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;

public interface JenkinsChangeListener {

  void activeJobViewChanged(JenkinsJobsResponse newView);

  void jenkinsChanged(List<JenkinsInstanceResponse> instances);

  void activeJobHistoryChanged(JenkinsJobAndBuildsResponse newView);
}
