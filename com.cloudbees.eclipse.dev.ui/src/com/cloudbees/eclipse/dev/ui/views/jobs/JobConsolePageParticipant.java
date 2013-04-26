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
package com.cloudbees.eclipse.dev.ui.views.jobs;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;

public class JobConsolePageParticipant implements IConsolePageParticipant {

  public void init(final IPageBookViewPage page, final IConsole console) {
    IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
    manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, new CloseConsoleAction(console));
  }

  public void dispose() {
  }

  public void activated() {
  }

  public void deactivated() {
  }

  public Object getAdapter(final Class adapter) {
    return null;
  }

}
