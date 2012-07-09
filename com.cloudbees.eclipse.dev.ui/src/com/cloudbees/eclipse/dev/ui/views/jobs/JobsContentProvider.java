package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.JobViewGeneric;

public class JobsContentProvider implements ITreeContentProvider {
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
    return getChildren(parent);
  }

  @Override
  public Object[] getChildren(Object parent) {
    if (parent instanceof IViewSite) {
      if (root == null) {
        return new JenkinsJobsResponse.JobViewGeneric[0];
      } else {
        return root.toArray(new JenkinsJobsResponse.JobViewGeneric[root.size()]);
      }
    }
    return null;
  }

  @Override
  public Object getParent(Object element) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasChildren(Object element) {
    Object[] e = getChildren(element);
    return e!=null && e.length>0;
  }
  
}
