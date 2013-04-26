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
package com.cloudbees.eclipse.dev.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.views.properties.IPropertySource;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.dev.ui.views.forge.ForgePropertySource;
import com.cloudbees.eclipse.dev.ui.views.instances.InstancePropertySource;
import com.cloudbees.eclipse.dev.ui.views.instances.ViewPropertySource;

public class AdapterFactory implements IAdapterFactory {

  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {

    if (adaptableObject instanceof JenkinsInstanceResponse) {
      if (adapterType == IPropertySource.class) {
        return new InstancePropertySource((JenkinsInstanceResponse) adaptableObject);
      }
    }

    if (adaptableObject instanceof JenkinsInstanceResponse.View) {
      if (adapterType == IPropertySource.class) {
        return new ViewPropertySource((JenkinsInstanceResponse.View) adaptableObject);
      }
    }

    if (adaptableObject instanceof ForgeInstance) {
      if (adapterType == IPropertySource.class) {
        return new ForgePropertySource((ForgeInstance) adaptableObject);
      }
    }

    return null;
  }

  @Override
  public Class[] getAdapterList() {
    return new Class[] { IPropertySource.class, IActionFilter.class };
  }

}
