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

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;

/**
 * Model element for the JenkinsJobsResponse.JobViewGeneric Main difference is that this holder maintains unique ID for
 * proper expanding and sorting routines in tree views.
 */
public class JobHolder {

  public final JenkinsJobsResponse.JobViewGeneric job;

  private JobHolder parentJobHolder;

  public JobHolder(JenkinsJobsResponse.JobViewGeneric job, JobHolder parent) {
    this.job = job;
    this.parentJobHolder = parent;
  }

  @Override
  public boolean equals(Object obj) {

    if (obj instanceof JobHolder) {
      JobHolder h2 = (JobHolder) obj;

      if (parentJobHolder == null && h2.parentJobHolder == null) {
        return job.equals(h2.job);
      }
      if (parentJobHolder == null || h2.parentJobHolder == null) {
        return false; // at least on of these was with a non-null parent
      }

      // both have parents.

      if (!job.equals(h2.job)) {
        return false;
      }

      // jobs are same, compare the parent jobs.
      return isParentsEqual(parentJobHolder, h2.parentJobHolder);

    }

    return false;
  }


  private boolean isParentsEqual(JobHolder p1, JobHolder p2) {
    if (p1==null && p2==null) {
      // got that far without finding a single inconsistency. must be the same paths.
      return true;
    }
    
    if (p1!=null && p2!=null) {
      if (!p1.job.equals(p2.job)) {
        return false;
      }
      return isParentsEqual(p1.parentJobHolder, p2.parentJobHolder);
    }
    
    // one parent is null and one is not, can't be equal
    return false;
  }

  @Override
  public String toString() {
    return job.getName();
  }
  
  @Override
  public int hashCode() {

    if (parentJobHolder == null) {
      return job.hashCode();
    }

    return 17*(parentJobHolder.job.hashCode() + job.hashCode());

  }

}
