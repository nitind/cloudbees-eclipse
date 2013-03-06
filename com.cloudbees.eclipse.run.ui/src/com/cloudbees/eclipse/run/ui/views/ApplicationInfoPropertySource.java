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
package com.cloudbees.eclipse.run.ui.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.cloudbees.api.ApplicationInfo;

public class ApplicationInfoPropertySource implements IPropertySource {

  private static final String BASE = ApplicationInfoPropertySource.class.getSimpleName().toLowerCase();
  private static final String PROPRTY_ID = BASE + ".id";
  private static final String PROPRTY_TITLE = BASE + ".title";
  private static final String PROPRTY_STATUS = BASE + ".status";
  private static final String PROPRTY_CREATED_DATE = BASE + ".created.date";
  private static final String PROPERTY_URL = BASE + ".url";

  private IPropertyDescriptor[] propertyDescriptors;
  private final ApplicationInfo appInfo;

  public ApplicationInfoPropertySource(ApplicationInfo appInfo) {
    this.appInfo = appInfo;
  }

  @Override
  public Object getEditableValue() {
    return this;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    if (this.propertyDescriptors == null) {

      PropertyDescriptor idDescriptor = new PropertyDescriptor(PROPRTY_ID, "App ID");

      PropertyDescriptor titleDescriptor = new PropertyDescriptor(PROPRTY_TITLE, "Title");

      PropertyDescriptor statusDescriptor = new PropertyDescriptor(PROPRTY_STATUS, "Status");

      PropertyDescriptor createdDateDescriptor = new PropertyDescriptor(PROPRTY_CREATED_DATE, "Created Date");
      createdDateDescriptor.setLabelProvider(new LabelProvider() {
        @Override
        public String getText(Object element) {
          if (element instanceof Date) {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm").format((Date) element);
          } else {
            return super.getText(element);
          }
        }
      });

      PropertyDescriptor urlDescriptor = new PropertyDescriptor(PROPERTY_URL, "URL");
      urlDescriptor.setLabelProvider(new LabelProvider() {
        @Override
        public String getText(Object element) {
          StringBuilder urls = new StringBuilder();
          String http = "http://";
          String separator = " , ";
          for (String url : (String[]) element) {
            urls.append(http).append(url).append(separator);
          }
          urls.delete(urls.length() - separator.length(), urls.length() - 1);
          return urls.toString();
        }
      });

      this.propertyDescriptors = new IPropertyDescriptor[] { idDescriptor, titleDescriptor, statusDescriptor,
          createdDateDescriptor, urlDescriptor };
    }

    return this.propertyDescriptors;
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(PROPRTY_ID)) {
      return this.appInfo.getId();
    }

    if (id.equals(PROPRTY_TITLE)) {
      return this.appInfo.getTitle();
    }

    if (id.equals(PROPRTY_STATUS)) {
      return this.appInfo.getStatus();
    }

    if (id.equals(PROPRTY_CREATED_DATE)) {
      return this.appInfo.getCreated();
    }

    if (id.equals(PROPERTY_URL)) {
      return this.appInfo.getUrls();
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
