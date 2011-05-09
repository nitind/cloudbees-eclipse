package com.cloudbees.eclipse.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;

public interface CBTreeContributor {

  boolean isPopup();

  boolean isPullDown();

  boolean isToolbar();

  void contributeTo(IMenuManager menuManager);

  void contributeTo(IToolBarManager toolBarManager);
}
