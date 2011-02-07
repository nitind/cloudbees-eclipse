package com.cloudbees.eclipse.core;

import java.util.List;

import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;

public interface NectarChangeListener {

  void activeJobViewChanged(NectarJobsResponse newView);

  void nectarsChanged(List<NectarInstanceResponse> instances);

}
