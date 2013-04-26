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
package com.cloudbees.eclipse.run.core;

import org.eclipse.ant.core.IAntPropertyValueProvider;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;
import com.cloudbees.eclipse.run.sdk.CBSdkActivator;

public class BeesProperty implements IAntPropertyValueProvider {

  public String getAntPropertyValue(String prop) {
    if ("bees.home".equalsIgnoreCase(prop)) {
      return CBSdkActivator.getDefault().getBeesHome();
    }

    /*    if ("bees.apiSecret".equalsIgnoreCase(prop)) {
          AuthInfo auth = getAuth();
          if (auth == null) {
            return null;
          }
          return auth.getAuth().secret_key;
        }

        if ("bees.apiKey".equalsIgnoreCase(prop)) {
          AuthInfo auth = getAuth();
          if (auth == null) {
            return null;
          }
          return auth.getAuth().api_key;
        }
    */
    return null;
  }

  private AuthInfo getAuth() {
    GrandCentralService grandCentralService;
    try {
      grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
      return grandCentralService.getCachedAuthInfo(false);
    } catch (CloudBeesException e) {
      return null;
    }
  }
}
