package com.cloudbees.eclipse.run.ui.views;

import com.cloudbees.eclipse.ui.views.ICBGroup;

public class AppGroup implements ICBGroup {

  String name;

  public AppGroup(final String name) {
    this.name = name;
  }

  @Override
  public int getOrder() {
    return 5;
  }

}
