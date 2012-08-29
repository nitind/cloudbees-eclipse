package com.cloudbees.eclipse.core;

import java.util.List;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;

public class CBRemoteChangeAdapter implements CBRemoteChangeListener {

  public void activeJobViewChanged(JenkinsJobsResponse newView) {
  }

  public void jenkinsChanged(List<JenkinsInstanceResponse> instances) {
  }

  public void activeJobHistoryChanged(JenkinsJobAndBuildsResponse newView) {
  }

  public void forgeChanged(List<ForgeInstance> instances) {
  }

  public void activeAccountChanged(String email, String newAccountName) {
    
  }

  public void jenkinsStatusUpdate(String viewUrl, boolean online) {
    
  }
  
}
