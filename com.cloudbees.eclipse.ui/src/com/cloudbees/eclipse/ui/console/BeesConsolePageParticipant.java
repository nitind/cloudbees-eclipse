/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.ui.console;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.*;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;

public class BeesConsolePageParticipant implements IConsolePageParticipant {

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
