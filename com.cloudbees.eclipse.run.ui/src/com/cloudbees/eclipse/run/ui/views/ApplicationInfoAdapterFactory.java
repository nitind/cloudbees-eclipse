package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.views.properties.IPropertySource;

import com.cloudbees.api.ApplicationInfo;

public class ApplicationInfoAdapterFactory implements IAdapterFactory {

  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {

    if (!(adaptableObject instanceof ApplicationInfo)) {
      return null;
    }

    if (adapterType == IPropertySource.class) {
      return new ApplicationInfoPropertySource((ApplicationInfo) adaptableObject);
    }

    if (adapterType == IActionFilter.class) {
      return new StatusActionFilter();
    }

    return null;
  }

  @Override
  public Class[] getAdapterList() {
    return new Class[] { IPropertySource.class, IActionFilter.class };
  }

}
