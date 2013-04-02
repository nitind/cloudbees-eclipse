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
package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;
import com.cloudbees.eclipse.run.ui.console.ConsoleUtil;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class CBLocalLaunchShortcut implements ILaunchShortcut {

  protected static final String CONSOLE_TYPE = "com.cloudbees.eclipse.run.ui.runLocalLog";

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

  @Override
  public void launch(ISelection selection, String mode) {

    if (!DebugUITools.saveBeforeLaunch()) {
      return;
    }

    if (selection instanceof IStructuredSelection) {

      IStructuredSelection structuredSelection = (IStructuredSelection) selection;

      final Object element = structuredSelection.getFirstElement();

      String name = null;
      IFile file = null;
      IProject project = null;

      if (element instanceof IProject) {
        project = (IProject) element;
        name = ((IProject) element).getName();
      } else if (element instanceof IJavaProject) {
        project = ((IJavaProject) element).getProject();
        name = ((IJavaProject) element).getProject().getName();
      } else if (element instanceof IFile) {
        file = ((IFile) element);
        name = ((IFile) element).getName();
      }

      if (name == null) {
        throw new RuntimeException("Element type not detected: " + element);
      }

      final IFile file1 = file;
      final IProject project1 = project;

      try {
        List<ILaunchConfiguration> launchConfigurations = CBRunUtil.getOrCreateLocalCloudBeesLaunchConfigurations(file);
        ILaunchConfiguration configuration = launchConfigurations.get(launchConfigurations.size() - 1);
        DebugUITools.launch(configuration, mode);
      } catch (CoreException e) {
        CBRunUiActivator.logError(e);
      }
/*
      org.eclipse.core.runtime.jobs.Job sjob = new org.eclipse.core.runtime.jobs.Job("Deploying " + name + " to local") {
        @Override
        protected IStatus run(final IProgressMonitor monitor) {
          try {
            CBLocalLaunchShortcut.internalLaunch(monitor, file1, project1, false);
            return org.eclipse.core.runtime.Status.OK_STATUS;
          } catch (Exception e) {
            //CloudBeesUIPlugin.getDefault().getLogger().error(e);
            return new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.Status.ERROR,
                CloudBeesUIPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
          }
        }
      };
      sjob.setUser(true);
      sjob.schedule();
*/
      /*      if (buildXml.exists()) {
              try {
                List<ILaunchConfiguration> launchConfigurations = CBRunUtil.getOrCreateCloudBeesLaunchConfigurations(name,
                    null);
                ILaunchConfiguration configuration = launchConfigurations.get(launchConfigurations.size() - 1);
                DebugUITools.launch(configuration, mode);
              } catch (CoreException e) {
                CBRunUiActivator.logError(e);
              }
            } else {
              final String msg = MessageFormat.format("Cannot launch {0}, could not find {1}", name, buildXml.getLocation());

              Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                  Shell shell = Display.getDefault().getActiveShell();
                  Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, msg);
                  ErrorDialog.openError(shell, "Error", null, status);
                }
              });
            }
          }
      */}
  }



  private static void wrappedDeployLocal(final IProject project, final IFile file, final boolean debug,
      final IProgressMonitor monitor) throws CloudBeesException {

    /*
    IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();

    manager.addConsoleListener(listener);

    String artifactId = file == null ? project.getName() : file.getRawLocation().toOSString();

    String consoleName = "BeesSDK Embedded Tomcat: " + artifactId;

    final IConsole console = getBeesConsole(consoleName);

    ConsoleUtil.activateConsole(console);

    manager.showConsoleView(console);

        Thread t = new Thread(new Runnable() {

          @Override
          public void run() {
    IOConsoleOutputStream consoleOutputStream = ((IOConsole) console).newOutputStream();
    try {

      if (file == null) { // project run --> include build 
        return BeesSDK.deployProjectLocal(project, true, debug, monitor, consoleOutputStream);
      } else {
        return BeesSDK.deployFileLocal(project, file, debug, monitor, consoleOutputStream);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;*/
    //DebugUITools.launch(configuration, mode)
    /*      }
    
    });
    */
    //    t.start();

    //tmap.put(consoleName, t);
  }

  private static IConsole getBeesConsole(String consoleName) {
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

  @Override
  public void launch(IEditorPart editor, String mode) {
    // not currently supported
  }
  /*
    private IFile getBuildXml(IProject project) {
      return project.getFile("build.xml");
    }
  */
}
