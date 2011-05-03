package com.cloudbees.eclipse.run.core;

import com.cloudbees.api.ApplicationInfo;

public interface IStatusUpdater {
  public void update(String id, String status, ApplicationInfo info);
}
