package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import com.cloudbees.api.ApplicationInfo;

public class ApplicationInfoAdapterFactory implements IAdapterFactory {

  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (adapterType == IPropertySource.class && adaptableObject instanceof ApplicationInfo) {
      return new ApplicationInfoPropertySource((ApplicationInfo) adaptableObject);
    }
    return null;
  }

  @Override
  public Class[] getAdapterList() {
    return new Class[] { IPropertySource.class };
  }

}
