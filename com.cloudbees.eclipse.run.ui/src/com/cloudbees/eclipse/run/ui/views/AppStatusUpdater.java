package com.cloudbees.eclipse.run.ui.views;

import java.util.ArrayList;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.run.core.ApplicationStatusUpdater;

public class AppStatusUpdater implements ApplicationStatusUpdater {

  private static final ArrayList<ApplicationStatusUpdater> listeners = new ArrayList<ApplicationStatusUpdater>();

  public AppStatusUpdater() {
  }

  @Override
  public void update(String id, String status, ApplicationInfo info) {
    // Don't care about single updates
  }

  @Override
  public void update(ApplicationListResponse response) {
    for (ApplicationStatusUpdater l : listeners) {
      l.update(response);
    }
  }

  public static void addListener(ApplicationStatusUpdater listener) {
    listeners.add(listener);
  }

  public static void removeListener(ApplicationStatusUpdater listener) {
    listeners.remove(listener);
  }
}
