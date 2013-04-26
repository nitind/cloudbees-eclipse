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
package com.cloudbees.eclipse.run.ui.console;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.internal.console.ConsoleView;

public class ConsoleUtil {
 
  public static void activateConsole(final IConsole console) {
    ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
      public void run() {

        IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < workbenchWindows.length; i++) {
          IWorkbenchWindow window = workbenchWindows[i];
          if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
              IViewPart part = page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
              if (part != null && part instanceof IConsoleView) {
                ConsoleView view = (ConsoleView) part;

                view.display(console);

/*                if (view != null && console.getName().equals(view.getConsole().getName())) {
                  StyledText control = (StyledText) view.getCurrentPage().getControl();
                  if (!control.isDisposed()) {
                    control.setCaretOffset(control.getCharCount());
                  }
                }
*/
              }

            }
          }
        }
      }
    });

  }
}
