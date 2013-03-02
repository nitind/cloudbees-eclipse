package com.cloudbees.eclipse.dtp.internal;

import org.eclipse.ui.IActionFilter;

import com.cloudbees.api.DatabaseInfo;

public class DatabaseStatusActionFilter implements IActionFilter {

  public static final String NAME = "status";

  @Override
  public boolean testAttribute(Object target, String name, String value) {
    if (name.equals(NAME)) {
      DatabaseInfo appInfo = (DatabaseInfo) target;
      return appInfo.getStatus().equals(value);
    }
    return false;
  }

}
