package com.cloudbees.eclipse.ui.views;

import org.eclipse.jface.action.Action;

public abstract class CBTreeAction extends Action {

  public abstract boolean isPopup();

  public abstract boolean isPullDown();

  public abstract boolean isToolbar();

}
