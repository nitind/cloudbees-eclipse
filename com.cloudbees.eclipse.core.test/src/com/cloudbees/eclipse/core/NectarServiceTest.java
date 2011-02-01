package com.cloudbees.eclipse.core;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.cloudbees.eclipse.core.domain.NectarInstance;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;

public class NectarServiceTest {

  @Test
  public void testViewAndJobsRetrieval() throws CloudBeesException {
    NectarService s = new NectarService(new NectarInstance("Hudson", "http://deadlock.netbeans.org/hudson/"));

    System.out.println("Query tree: " + NectarInstanceResponse.getTreeQuery());

    NectarInstanceResponse vs = s.getInstance();
    NectarInstanceResponse.View[] views = vs.views;
    System.out.println("Primary view: " + vs.primaryView.name + ";" + vs.primaryView.url);
    System.out.println("Received views:");

    for (NectarInstanceResponse.View v : views) {
      System.out.println("view: " + v.name + "; " + v.url);
    }

    assertTrue(views.length > 0);

    System.err.println("Jobs:");
    NectarJobsResponse jb = s.getJobs(null);
    NectarJobsResponse.Job[] jobs = jb.jobs;
    System.out.println("Received builds:");

    for (NectarJobsResponse.Job j : jobs) {
      System.out.println("job: " + j.displayName);
    }

    assertTrue(jobs.length > 0);

  }

}
