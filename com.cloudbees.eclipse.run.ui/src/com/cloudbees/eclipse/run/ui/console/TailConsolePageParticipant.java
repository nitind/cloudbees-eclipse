package com.cloudbees.eclipse.run.ui.console;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.*;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;

public class TailConsolePageParticipant implements IConsolePageParticipant {

  private CloseConsoleAction closeAction;

  public Object getAdapter(Class adapter) {
    return null;
  }

  public void init(IPageBookViewPage page, IConsole console) {
    closeAction = new CloseConsoleAction(console);
    IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
    manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, closeAction);
  }

  public void dispose() {
    if (closeAction!=null) {
      closeAction = null;  
    }
    
  }

  public void activated() {
  }

  public void deactivated() {
  }

}