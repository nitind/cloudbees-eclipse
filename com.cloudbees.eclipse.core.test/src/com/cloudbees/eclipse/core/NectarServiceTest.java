package com.cloudbees.eclipse.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.cloudbees.eclipse.core.domain.NectarInstance;
import com.cloudbees.eclipse.core.nectar.api.NectarBuildDetailsResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarBuildDetailsResponse.ChangeSet;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;

public class NectarServiceTest {


  @Test
  public void testViewAndJobsRetrieval() throws CloudBeesException {
    NectarService s = new NectarService(new NectarInstance("Hudson", "http://deadlock.netbeans.org/hudson/"));

    System.out.println("Query tree: " + NectarInstanceResponse.QTREE);

    NectarInstanceResponse vs = s.getInstance(null);
    NectarInstanceResponse.View[] views = vs.views;
    System.out.println("Primary view: " + vs.primaryView.name + ";" + vs.primaryView.url);
    System.out.println("Received views:");

    for (NectarInstanceResponse.View v : views) {
      System.out.println("view: " + v.name + "; " + v.url);
    }

    assertTrue(views.length > 0);

    System.err.println("Jobs:");
    NectarJobsResponse jb = s.getJobs(null, null);
    NectarJobsResponse.Job[] jobs = jb.jobs;
    System.out.println("Received builds:");

    for (NectarJobsResponse.Job j : jobs) {
      System.out.println("job: " + j.displayName);
    }

    assertTrue(jobs.length > 0);

  }

  @Test
  public void testJobDetailRetrieval() throws CloudBeesException {
    NectarService s = new NectarService(new NectarInstance("Hudson", "http://deadlock.netbeans.org/hudson/"));

    NectarBuildDetailsResponse details = s
        .getJobDetails("http://deadlock.netbeans.org/hudson/job/jackpot30/301/", null);

    System.out.println("QTree: http://deadlock.netbeans.org/hudson/job/jackpot30/301/api/json/?tree="
        + NectarBuildDetailsResponse.QTREE);

    System.out.println("Received details:");

    ChangeSet changeset = details.changeSet;

    assertNotNull(changeset);

    for (NectarBuildDetailsResponse.ChangeSet.ChangeSetItem set : changeset.items) {
      System.out.println("item: " + set.msg);
    }

    System.out.println("Display name: " + details.fullDisplayName);

    assertTrue(details.fullDisplayName.length() > 0);

    assertTrue(changeset.items.length > 0);

  }

}
