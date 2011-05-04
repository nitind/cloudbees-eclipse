package com.cloudbees.eclipse.run.ui.views;

import java.util.ArrayList;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.run.core.IStatusUpdater;

public class AppStatusUpdater implements IStatusUpdater {

  private static final ArrayList<IStatusUpdater> listeners = new ArrayList<IStatusUpdater>();

  public AppStatusUpdater() {
  }

  @Override
  public void update(String id, String status, ApplicationInfo info) {
    // Don't care about single updates
  }

  @Override
  public void update(ApplicationListResponse response) {
    for (IStatusUpdater l : listeners) {
      l.update(response);
    }
  }

  public static void addListener(IStatusUpdater listener) {
    listeners.add(listener);
  }

  public static void removeListener(IStatusUpdater listener) {
    listeners.remove(listener);
  }
}
