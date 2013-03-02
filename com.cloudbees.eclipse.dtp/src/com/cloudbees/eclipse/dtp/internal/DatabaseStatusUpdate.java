package com.cloudbees.eclipse.dtp.internal;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.api.DatabaseListResponse;

public interface DatabaseStatusUpdate {
  public void update(String id, String status, DatabaseInfo info);

  public void update(DatabaseListResponse response);
}
