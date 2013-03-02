package com.cloudbees.eclipse.run.core;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;

public interface ApplicationStatusUpdater {
  public void update(String id, String status, ApplicationInfo info);

  public void update(ApplicationListResponse response);
}
