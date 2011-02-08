package com.cloudbees.eclipse.ui.views.build;

import org.eclipse.ui.internal.part.NullEditorInput;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job.Build;

public class BuildEditorInput extends NullEditorInput {

  private Job job;
  private String buildUrl;

  public BuildEditorInput(Job job) {
    super();
    //System.out.println("Creating job details editor for url " + job.url);
    this.job = job;
    if (getLastBuild() != null && getLastBuild().url != null) {
      buildUrl = getLastBuild().url;
    } else {
      buildUrl = job.url;
    }

  }

  @Override
  public boolean exists() {
    return true;
  }

  public Build getLastBuild() {
    return job.lastBuild;
  }

  public Job getJob() {
    return job;
  }

  public String getBuildUrl() {
    return this.buildUrl;
  }

  public void setBuildUrl(String newBuildUrl) {
    this.buildUrl = newBuildUrl;
  }

}
