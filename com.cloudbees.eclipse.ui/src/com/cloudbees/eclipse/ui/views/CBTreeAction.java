package com.cloudbees.eclipse.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;

public abstract class CBTreeAction extends Action implements CBTreeContributor {

  public void contributeTo(IMenuManager menuManager) {
    menuManager.add(this);
  }

  public void contributeTo(IToolBarManager toolBarManager) {
    toolBarManager.add(this);
  }
}
