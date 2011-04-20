package com.cloudbees.eclipse.dev.ui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class FavouritesUtils {

  private static final String FAVOURITES_LIST = "FAVOURITES_LIST";

  public static final String FAVOURITES = "favourites://";//$NON-NLS-1$

  private static final IPreferenceStore prefs = CloudBeesDevUiPlugin.getDefault().getPreferenceStore();

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

  private static ArrayList<String> getFavourites() {
    ArrayList<String> favourites = new ArrayList<String>();

    String pref = prefs.getString(FAVOURITES_LIST);

    if (!"".equals(pref)) { // we don't want an empty URL
      for (String string : pref.split(",")) {
        favourites.add(Utils.fromB64(string));
      }
    }

    return favourites;
  }

  private static void storeFavourites(ArrayList<String> favourites) {
    String pref = "";
    for (String string : favourites) {
      pref += Utils.toB64(string) + ",";
    }

    pref = pref.substring(0, pref.length() - 1);
    prefs.setValue(FAVOURITES_LIST, pref);
  }

  public static void removeFavourite(String favourite) {
    ArrayList<String> fav = getFavourites();
    if (fav.contains(favourite)) {
      fav.remove(favourite);
      storeFavourites(fav);
    }
  }

  public static void addFavourite(String favourite) {
    ArrayList<String> fav = getFavourites();
    if (!fav.contains(favourite)) {
      fav.add(favourite);
      storeFavourites(fav);
    }
  }

  public static boolean isFavourite(String favourite) {
    return getFavourites().contains(favourite);
  }

}
