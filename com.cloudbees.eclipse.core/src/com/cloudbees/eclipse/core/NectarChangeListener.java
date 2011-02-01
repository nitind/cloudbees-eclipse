package com.cloudbees.eclipse.core;

import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;

public interface NectarChangeListener {

  void activeJobViewChanged(NectarJobsResponse newView);

}
