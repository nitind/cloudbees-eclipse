package com.cloudbees.eclipse.ui.views.build;

import org.eclipse.ui.internal.part.NullEditorInput;

import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse.Job;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse.Job.Build;

public class BuildEditorInput extends NullEditorInput {

  private Job job;

  public BuildEditorInput(Job job) {
    super();
    System.out.println("Creating job details editor for url " + job.url);
    this.job = job;
  }

  @Override
  public boolean exists() {
    return true;
  }

  public Build getLastBuild() {
    return job.lastBuild;
  }

}
