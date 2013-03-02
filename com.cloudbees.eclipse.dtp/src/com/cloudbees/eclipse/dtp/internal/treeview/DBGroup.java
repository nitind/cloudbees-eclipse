package com.cloudbees.eclipse.dtp.internal.treeview;

import com.cloudbees.eclipse.ui.views.ICBGroup;

public class DBGroup implements ICBGroup {

  String name;

  public DBGroup(final String name) {
    this.name = name;
  }

  @Override
  public int getOrder() {
    return 5;
  }

}
