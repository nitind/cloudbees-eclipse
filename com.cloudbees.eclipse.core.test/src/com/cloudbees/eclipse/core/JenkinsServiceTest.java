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
package com.cloudbees.eclipse.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;

public class JenkinsServiceTest {


  @Test
  public void testViewAndJobsRetrieval() throws CloudBeesException {
    JenkinsService s = new JenkinsService(new JenkinsInstance("Hudson", "http://deadlock.netbeans.org/hudson/"));

    System.out.println("Query tree: " + JenkinsInstanceResponse.QTREE);

    JenkinsInstanceResponse vs = s.getInstance(null);
    JenkinsInstanceResponse.View[] views = vs.views;
    System.out.println("Primary view: " + vs.primaryView.name + ";" + vs.primaryView.url);
    System.out.println("Received views:");

    for (JenkinsInstanceResponse.View v : views) {
      System.out.println("view: " + v.name + "; " + v.url);
    }

    assertTrue(views.length > 0);

    System.err.println("Jobs:");
    JenkinsJobsResponse jb = s.getJobs(null, new NullProgressMonitor());
    JenkinsJobsResponse.Job[] jobs = jb.jobs;
    System.out.println("Received builds:");

    for (JenkinsJobsResponse.Job j : jobs) {
      System.out.println("job: " + j.getDisplayName());
    }

    assertTrue(jobs.length > 0);

  }

  @Test
  public void testJobDetailRetrieval() throws CloudBeesException {
    JenkinsService s = new JenkinsService(new JenkinsInstance("Hudson", "http://deadlock.netbeans.org/hudson/"));

    JenkinsBuildDetailsResponse details = s
.getJobDetails("http://deadlock.netbeans.org/hudson/job/jackpot30/304/",
        null);

    System.out.println("QTree: http://deadlock.netbeans.org/hudson/job/jackpot30/304/api/json/?tree="
        + JenkinsBuildDetailsResponse.QTREE);

    System.out.println("Received details:");

    ChangeSet changeset = details.changeSet;

    assertNotNull(changeset);

    for (JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem set : changeset.items) {
      System.out.println("item: " + set.msg);
    }

    System.out.println("Display name: " + details.getDisplayName());

    assertTrue(details.getDisplayName().length() > 0);

    assertTrue(changeset.items.length > 0);


  }

}
