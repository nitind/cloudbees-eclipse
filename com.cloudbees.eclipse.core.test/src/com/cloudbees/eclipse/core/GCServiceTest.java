/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.core;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;

public class GCServiceTest {

  static {
    //System.setProperty("cloudbees.host", "beescloud.com");
  }

  @Test
  public void testAccountNamesRetrieval() throws CloudBeesException {
    GrandCentralService gcs = new GrandCentralService();
    gcs.setAuthInfo("ahti@codehoop.com", "xxx");
    
    assertTrue(gcs.validateUser(null));

    List<JenkinsInstance> instances = gcs.loadDevAtCloudInstances(null);

    Iterator<JenkinsInstance> it = instances.iterator();
    while (it.hasNext()) {
      JenkinsInstance inst = (JenkinsInstance) it.next();
      System.out.println("Inst url:" + inst.url);
    }

    /*    JenkinsService s = new JenkinsService(new JenkinsInstance("12345", "Hudson", "http://deadlock.netbeans.org/hudson/"));

        System.out.println("Query tree: " + JenkinsInstanceResponse.getTreeQuery());

        JenkinsInstanceResponse vs = s.getInstance();
        JenkinsInstanceResponse.View[] views = vs.views;
        System.out.println("Primary view: " + vs.primaryView.name + ";" + vs.primaryView.url);
        System.out.println("Received views:");

        for (JenkinsInstanceResponse.View v : views) {
          System.out.println("view: " + v.name + "; " + v.url);
        }

        assertTrue(views.length > 0);

        System.err.println("Jobs:");
        JenkinsJobsResponse jb = s.getJobs(null);
        JenkinsJobsResponse.Job[] jobs = jb.jobs;
        System.out.println("Received builds:");

        for (JenkinsJobsResponse.Job j : jobs) {
          System.out.println("job: " + j.displayName);
        }

        assertTrue(jobs.length > 0);
    */
  }


}
