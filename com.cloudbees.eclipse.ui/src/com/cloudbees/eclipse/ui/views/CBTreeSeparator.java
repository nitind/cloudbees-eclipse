package com.cloudbees.eclipse.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

public class CBTreeSeparator extends Separator implements CBTreeContributor {

  public enum SeparatorLocation {
    POP_UP, PULL_DOWN, TOOL_BAR
  }

  private final SeparatorLocation location;

  public CBTreeSeparator(SeparatorLocation location) {
    this.location = location;
  }

  public void contributeTo(IMenuManager menuManager) {
    menuManager.add(this);
  }

  public void contributeTo(IToolBarManager toolBarManager) {
    toolBarManager.add(this);
  }

  public boolean isPopup() {
    return this.location == SeparatorLocation.POP_UP;
  }

  public boolean isPullDown() {
    return this.location == SeparatorLocation.PULL_DOWN;
  }

  public boolean isToolbar() {
    return this.location == SeparatorLocation.TOOL_BAR;
  }
}
