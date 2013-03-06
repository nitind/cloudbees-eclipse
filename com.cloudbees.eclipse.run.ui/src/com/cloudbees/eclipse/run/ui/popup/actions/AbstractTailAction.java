/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.run.ui.popup.actions;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.internal.ObjectPluginAction;
import org.eclipse.ui.internal.console.ConsoleView;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

@SuppressWarnings("restriction")
public abstract class AbstractTailAction implements IObjectActionDelegate {

  public static final String LOG_NAME_SERVER = "server";
  public static final String LOG_NAME_ACCESS = "access";
  public static final String LOG_NAME_ERROR = "error";
  protected static final String CONSOLE_TYPE = "com.cloudbees.eclipse.run.ui.tailLogs";

  private static Map<String, Thread> tmap = new ConcurrentHashMap<String, Thread>();

  private static IConsoleListener listener = new IConsoleListener() {
    public void consolesRemoved(IConsole[] arg0) {
      /*      if (silenceThreadKiller) {
              return;
            }
      */// make sure the thread that shows output is interrupted.
      for (int i = 0; i < arg0.length; i++) {
        IConsole c = arg0[0];
        if (c.getType() != null && c.getType().startsWith(CONSOLE_TYPE)) {

          Thread t = tmap.get(c.getName());
          if (t != null) {
            tmap.remove(t);
            try {
              t.interrupt();
            } catch (Exception e) {

            }
          }
        }
      }
    }

    public void consolesAdded(IConsole[] arg0) {

    }
  };

  //volatile private static boolean silenceThreadKiller = false;

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      ObjectPluginAction pluginAction = (ObjectPluginAction) action;
      ISelection selection = pluginAction.getSelection();

      if (selection instanceof IStructuredSelection) {
        IStructuredSelection structSelection = (IStructuredSelection) selection;
        Object element = structSelection.getFirstElement();

        if (element instanceof ApplicationInfo) {
          final ApplicationInfo appInfo = (ApplicationInfo) element;

          Display.getCurrent().asyncExec(new Runnable() {
            @Override
            public void run() {
              tail(appInfo, getLogName());
            }
          });
        }
      }
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  private IConsole getTailConsole(String consoleName) {
    ImageDescriptor descriptor = CBRunUiActivator.imageDescriptorFromPlugin(CBRunUiActivator.PLUGIN_ID,
        Images.CLOUDBEES_ICON_16x16_PATH);

    IConsole console = null;

    IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();

    for (IConsole c : manager.getConsoles()) {
      if (consoleName.equals(c.getName())) {
        console = c;
      }
    }

    if (console == null) {
      console = new IOConsole(consoleName, CONSOLE_TYPE, descriptor, true);
      manager.addConsoles(new IConsole[] { console });
      //manager.showConsoleView(console);      
    } else {
      // activate existing console. Have not found a way to force top position in console stack without removing it temporarily.

      //      try {
      //silenceThreadKiller = true;
      //manager.removeConsoles(new IConsole[] { foundConsole });
      //manager.addConsoles(new IConsole[] { foundConsole });
      //((IOConsole) foundConsole).activate();
      //manager.refresh(foundConsole);
      //manager.showConsoleView(foundConsole);
      //} finally {
      //silenceThreadKiller = false;
      //}
      //
      //((ConsoleManager)manager).
      //manager.showConsoleView(foundConsole);
      //console.activate();
    }

    return console;
  }

  private void tail(final ApplicationInfo appInfo, final String logName) {
    IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();

    manager.addConsoleListener(listener);

    String consoleName = MessageFormat.format("Tail {0} log :: {1}", logName, appInfo.getTitle());
    final IConsole console = getTailConsole(consoleName);

    activateConsole(console);

    manager.showConsoleView(console);

    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        IOConsoleOutputStream consoleOutputStream = ((IOConsole) console).newOutputStream();
        try {
          BeesSDK.tail(appInfo.getId(), logName, consoleOutputStream);
        } catch (Exception e) {
          //CBRunUiActivator.logErrorAndShowDialog(e);
          //e.printStackTrace();
          // Safe to not log as when console is closed this thread will stop as writing to outputstream fails
        }
      }
    });

    t.start();

    tmap.put(consoleName, t);
  }

  private void activateConsole(final IConsole console) {
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

  protected abstract String getLogName();

  public static class TailServerLogAction extends AbstractTailAction {

    @Override
    protected String getLogName() {
      return LOG_NAME_SERVER;
    }

  }

  public static class TailAccessLogAction extends AbstractTailAction {

    @Override
    protected String getLogName() {
      return LOG_NAME_ACCESS;
    }

  }

  public static class TailErrorLogAction extends AbstractTailAction {

    @Override
    protected String getLogName() {
      return LOG_NAME_ERROR;
    }

  }
}
