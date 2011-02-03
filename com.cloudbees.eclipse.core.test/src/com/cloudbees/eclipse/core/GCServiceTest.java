package com.cloudbees.eclipse.core;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.cloudbees.eclipse.core.domain.NectarInstance;

public class GCServiceTest {

  static {
    //System.setProperty("cloudbees.host", "beescloud.com");
  }

  @Test
  public void testAccountNamesRetrieval() throws CloudBeesException {
    GrandCentralService gcs = new GrandCentralService("ahti@codehoop.com", "13DoF02l");

    assertTrue(gcs.validateUser(null));

    List<NectarInstance> instances = gcs.loadDACNectarInstances(null);

    Iterator<NectarInstance> it = instances.iterator();
    while (it.hasNext()) {
      NectarInstance inst = (NectarInstance) it.next();
      System.out.println("Inst url:" + inst.url);
    }

    /*    NectarService s = new NectarService(new NectarInstance("12345", "Hudson", "http://deadlock.netbeans.org/hudson/"));

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
    */
  }


}
