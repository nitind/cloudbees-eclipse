package com.cloudbees.eclipse.ui.views.jobs;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;

public class JobsContentProvider implements IStructuredContentProvider {
  private List<JenkinsJobsResponse.Job> root;

  public JobsContentProvider() {
  }

  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    if (newInput instanceof List && (((List) newInput).isEmpty() || ((List) newInput).get(0) instanceof Job)) {
      root = (List<Job>) newInput;
    } else {
      root = null; // reset
    }
  }

  public void dispose() {
  }

  public Object[] getElements(Object parent) {
    if (parent instanceof IViewSite) {
      if (root == null) {
        return new JenkinsJobsResponse.Job[0];
      } else {
        return root.toArray(new JenkinsJobsResponse.Job[root.size()]);
      }
    }
    return null;
  }
}
