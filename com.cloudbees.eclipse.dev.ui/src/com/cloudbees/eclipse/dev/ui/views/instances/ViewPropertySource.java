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
package com.cloudbees.eclipse.dev.ui.views.instances;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse.View;

public class ViewPropertySource implements IPropertySource {

  private static final String BASE = ViewPropertySource.class.getSimpleName().toLowerCase();
  private static final Object PROPERTY_NAME = BASE + ".name";
  private static final Object PROPERTY_DESCRIPTION = BASE + ".description";
  private static final Object PROPERTY_URL = BASE + "url";
  private IPropertyDescriptor[] propertyDescriptors;
  private final JenkinsInstanceResponse.View view;

  public ViewPropertySource(View view) {
    this.view = view;
  }

  @Override
  public Object getEditableValue() {
    return this;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    if (this.propertyDescriptors == null) {

      PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "Name");
      PropertyDescriptor descrDescriptor = new PropertyDescriptor(PROPERTY_DESCRIPTION, "Description");
      PropertyDescriptor urlDescriptor = new PropertyDescriptor(PROPERTY_URL, "URL");

      this.propertyDescriptors = new IPropertyDescriptor[] { nameDescriptor, descrDescriptor, urlDescriptor };
    }

    return this.propertyDescriptors;
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(PROPERTY_NAME)) {
      return this.view.name;
    }
    if (id.equals(PROPERTY_DESCRIPTION)) {
      return this.view.description;
    }
    if (id.equals(PROPERTY_URL)) {
      return this.view.url;
    }
    return null;
  }

  @Override
  public boolean isPropertySet(Object id) {
    return false;
  }

  @Override
  public void resetPropertyValue(Object id) {
  }

  @Override
  public void setPropertyValue(Object id, Object value) {
  }

}
