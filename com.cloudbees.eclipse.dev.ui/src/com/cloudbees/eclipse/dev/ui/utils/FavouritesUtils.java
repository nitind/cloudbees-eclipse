package com.cloudbees.eclipse.dev.ui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class FavouritesUtils {

  public static final String FAVOURITES = "favourites://";//$NON-NLS-1$

  public static JenkinsJobsResponse getFavouritesResponse(final IProgressMonitor monitor) throws CloudBeesException {
    List<String> favourites = getFavourites();
    HashMap<String, JenkinsService> instances = getInstances(favourites);

    ArrayList<Job> filtered = new ArrayList<JenkinsJobsResponse.Job>();

    for (String instance : instances.keySet()) {
      JenkinsService jenkinsService = instances.get(instance);
      JenkinsJobsResponse allJobs = jenkinsService.getJobs(instance, monitor);
      for (Job j : allJobs.jobs) {
        if (favourites.contains(j.url)) {
          filtered.add(j);
        }
      }
    }
    System.out.println(filtered);

    JenkinsJobsResponse jobs = new JenkinsJobsResponse();
    jobs.name = "Favourite jobs";
    jobs.viewUrl = FAVOURITES;
    jobs.jobs = filtered.toArray(new Job[filtered.size()]);

    return jobs;
  }

  private static HashMap<String, JenkinsService> getInstances(List<String> favourites) {
    HashMap<String, JenkinsService> instances = new HashMap<String, JenkinsService>();

    for (String string : favourites) {
      String instance = string.split("job")[0];

      if (instances.get(instance) == null) {
        instances.put(instance, getService(instance));
      }
    }
    return instances;
  }

  private static JenkinsService getService(String instance) {
    List<JenkinsService> allJenkinsServices = CloudBeesUIPlugin.getDefault().getAllJenkinsServices();
    for (JenkinsService jenkinsService : allJenkinsServices) {

      if (instance.startsWith(jenkinsService.getUrl())) {
        return jenkinsService;
      }

    }
    return null;
  }

  private static List<String> getFavourites() {
    ArrayList<String> favourites = new ArrayList<String>();

    favourites.add("https://imeikas.ci.cloudbees.com/job/Build%20Akka/");
    favourites.add("https://imeikas.ci.cloudbees.com/job/Build%20Scala/");
    favourites.add("http://localhost:8080/job/test/");
    favourites.add("http://localhost:8080/job/test/");

    return favourites;
  }

}
