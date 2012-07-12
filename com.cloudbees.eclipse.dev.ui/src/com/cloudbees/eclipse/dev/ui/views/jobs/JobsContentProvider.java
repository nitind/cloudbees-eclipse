package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.JobViewGeneric;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.View;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

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
  public Object[] getChildren(final Object parent) {
    if (parent instanceof IViewSite) {
      if (root == null) {
        return new JenkinsJobsResponse.JobViewGeneric[0];
      } else {
        return root.toArray(new JenkinsJobsResponse.JobViewGeneric[root.size()]);
      }
    }
    if ((parent instanceof JenkinsJobsResponse.Job && ((JenkinsJobsResponse.Job) parent).color == null)
        || (parent instanceof JenkinsJobsResponse.View)) {
      // This is a folder job or a view so we can use the url to fetch the children

      final List<JobViewGeneric>[] toRet = new ArrayList[1];

      Job job = new Job("Fetching jobs for the folder or view") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          try {
            String url = ((JenkinsJobsResponse.JobViewGeneric) parent).getUrl();
            JenkinsJobsResponse jobs = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(url)
                .getJobs(url, monitor);

            List<JenkinsJobsResponse.JobViewGeneric> reslist = new ArrayList<JenkinsJobsResponse.JobViewGeneric>();
            
            if (jobs.views!=null)
            for (View view : jobs.views) {
              if (view.url != null && (jobs.primaryView == null || !view.url.equals(jobs.primaryView.url))) {
                reslist.add(view);
              }
            }
            
            if (jobs.jobs != null) {
              reslist.addAll(Arrays.asList(jobs.jobs));
            }

            toRet[0] = reslist;

          } catch (CloudBeesException e) {
            e.printStackTrace();
            CloudBeesDevUiPlugin.logError(e);
            return Status.OK_STATUS; // Is It Ok?
          }
          return Status.OK_STATUS;

        }
      };
      job.schedule();
      try {
        job.join();
        if (toRet[0] != null) {
          return toRet[0].toArray(new JobViewGeneric[0]);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
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
    // for folders and views assume there are children to suggest lazy loading later.
    if ((element instanceof JenkinsJobsResponse.Job && ((JenkinsJobsResponse.Job) element).color == null)
        || (element instanceof JenkinsJobsResponse.View)) {
      return true;
    }
    Object[] e = getChildren(element);
    return e != null && e.length > 0;
  }

}
