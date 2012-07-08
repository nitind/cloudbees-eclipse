package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.JobViewGeneric;

public class JobsContentProvider implements IStructuredContentProvider {
  private List<JenkinsJobsResponse.JobViewGeneric> root;

  public JobsContentProvider() {
  }

  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    if (newInput instanceof List && (((List) newInput).isEmpty() || ((List) newInput).get(0) instanceof JobViewGeneric)) {
      root = (List<JobViewGeneric>) newInput;
    } else {
      root = null; // reset
    }
  }

  public void dispose() {
  }

  public Object[] getElements(Object parent) {
    if (parent instanceof IViewSite) {
      if (root == null) {
        return new JenkinsJobsResponse.JobViewGeneric[0];
      } else {
        return root.toArray(new JenkinsJobsResponse.JobViewGeneric[root.size()]);
      }
    }
    return null;
  }
}
