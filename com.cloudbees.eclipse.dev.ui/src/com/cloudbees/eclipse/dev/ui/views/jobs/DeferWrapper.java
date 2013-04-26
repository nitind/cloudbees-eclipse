/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.JobViewGeneric;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.View;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class DeferWrapper implements IDeferredWorkbenchAdapter {

  protected static final Object[] EMPTY = new Object[0];
  private JobHolder parent;

  public DeferWrapper(JobHolder element) {
    this.parent = element;
  }

  @Override
  public Object[] getChildren(Object parent) {
    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor(Object object) {
    return null;
  }

  @Override
  public String getLabel(Object o) {    
    return "na: "+o;
  }

  @Override
  public Object getParent(Object o) {
    return null;
  }

  @Override
  public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
    if (monitor.isCanceled()) {
      return;
    }
    try {
      Object[] children = getChildren(object, monitor);
      if (monitor.isCanceled()) {
        return;
      }
      if (children.length > 0) {
        collector.add(children, monitor);
      }
      collector.done();
    } catch (CloudBeesException e){
      CloudBeesUIPlugin.getDefault().getLogger().error(e);      
    }
  }

  private Object[] getChildren(Object parent, IProgressMonitor monitor) throws CloudBeesException {
    
    JobViewGeneric p = null;
    if (parent instanceof JobHolder) {
      p = ((JobHolder) parent).job;
    }
    if (p!=null && (p instanceof JobViewGeneric) && ((JobViewGeneric) p).isFolderOrView()) {
      // This is a folder job or a view so we can use the url to fetch the children

      String url = ((JenkinsJobsResponse.JobViewGeneric) p).getUrl();
      JenkinsJobsResponse jobs = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(url).getJobs(url, monitor);

      List<JenkinsJobsResponse.JobViewGeneric> reslist = new ArrayList<JenkinsJobsResponse.JobViewGeneric>();
      
      List<JobHolder> retlist = new ArrayList<JobHolder>();

      if (jobs.views != null)
        for (View view : jobs.views) {
          if (view.url != null && (jobs.primaryView == null || !view.url.equals(jobs.primaryView.url))) {
            reslist.add(view);
          }
        }

      if (jobs.jobs != null) {
        reslist.addAll(Arrays.asList(jobs.jobs));
      }

      Iterator<JobViewGeneric> it = reslist.iterator();
      while (it.hasNext()) {
        JenkinsJobsResponse.JobViewGeneric j = (JenkinsJobsResponse.JobViewGeneric) it.next();
        retlist.add(new JobHolder(j, ((JobHolder)parent)));
      }
      
      return retlist.toArray(new JobHolder[0]);
    }
    return EMPTY;

  }

  @Override
  public boolean isContainer() {
    return parent.job.isFolderOrView();
  }

  @Override
  public ISchedulingRule getRule(Object object) {
    return null;
  }

}
