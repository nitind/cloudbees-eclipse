/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
