package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.jface.viewers.LabelProvider;

import com.cloudbees.api.ApplicationInfo;

final class AppLabelProvider extends LabelProvider {
  @Override
  public String getText(final Object element) {
    if (element instanceof ApplicationInfo) {
      ApplicationInfo elem = (ApplicationInfo) element;
      return elem.getId() + " (" + elem.getStatus() + ")";
    }
    return super.getText(element);
  }
}