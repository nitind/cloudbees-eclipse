/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
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
