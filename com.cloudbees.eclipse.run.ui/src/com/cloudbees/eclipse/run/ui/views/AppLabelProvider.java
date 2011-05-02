package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.jface.viewers.LabelProvider;

import com.cloudbees.api.ApplicationInfo;

final class AppLabelProvider extends LabelProvider {
  @Override
  public String getText(final Object element) {
    if (element instanceof ApplicationInfo) {
      ApplicationInfo elem = (ApplicationInfo) element;
      return elem.getId() + " (" + elem.getStatus() + ")";
    } else if (element instanceof ApplicationDetail) {
      ApplicationDetail detail = (ApplicationDetail) element;
      return detail.attributeName + ": " + detail.attributeValue;
    }
    return super.getText(element);
  }

}
