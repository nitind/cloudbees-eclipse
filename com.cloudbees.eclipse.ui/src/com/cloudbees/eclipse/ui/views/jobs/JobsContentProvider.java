package com.cloudbees.eclipse.ui.views.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse.Job;


public class JobsContentProvider implements IStructuredContentProvider {
  private List<NectarJobsResponse.Job> root;
  private IViewSite site;

  public JobsContentProvider(IViewSite iViewSite) {
    site = iViewSite;
  }

  public void inputChanged(Viewer v, Object oldInput, Object newInput) {

  }

  public void dispose() {
  }

  public Object[] getElements(Object parent) {
    if (parent.equals(site)) {
      if (root == null) {
        try {
          initialize();
        } catch (Exception e) {
          e.printStackTrace();
          return new NectarJobsResponse.Job[0];//service not available
        }
      }
      return root.toArray(new NectarJobsResponse.Job[0]);
    }
    return null;
  }


  private void initialize() throws CloudBeesException {
    root = new ArrayList<NectarJobsResponse.Job>();
  }

  public void setJobs(List<Job> jobs) {
    this.root = jobs;
  }

}