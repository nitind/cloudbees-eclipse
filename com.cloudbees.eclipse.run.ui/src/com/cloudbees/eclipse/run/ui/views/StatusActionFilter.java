package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.ui.IActionFilter;

import com.cloudbees.api.ApplicationInfo;

public class StatusActionFilter implements IActionFilter {

  public static final String NAME = "status";

  @Override
  public boolean testAttribute(Object target, String name, String value) {
    if (name.equals(NAME)) {
      ApplicationInfo appInfo = (ApplicationInfo) target;
      return appInfo.getStatus().equals(value);
    }
    return false;
  }

}
