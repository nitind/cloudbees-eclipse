package com.cloudbees.eclipse.run.ui.popup.actions;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

@SuppressWarnings("restriction")
public class TailAction implements IObjectActionDelegate {

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {

      ISelection selection = ((ObjectPluginAction) action).getSelection();

      if (selection instanceof StructuredSelection) {
        final Object firstElement = ((StructuredSelection) selection).getFirstElement();

        if (firstElement instanceof IProject) {
          final IProject project = (IProject) firstElement;
          Display.getCurrent().asyncExec(new Runnable() {

            @Override
            public void run() {
              tail(project);
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

  private IOConsole getTailConsole(String consoleName) {
    ImageDescriptor descriptor = CBRunUiActivator.imageDescriptorFromPlugin(CBRunUiActivator.PLUGIN_ID,
        Images.CLOUDBEES_ICON_16x16_PATH);

    IOConsole console = new IOConsole(consoleName, descriptor);

    IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
    boolean foundConsole = false;

    for (IConsole c : manager.getConsoles()) {
      if (console.getName().equals(c.getName())) {
        foundConsole = true;
      }
    }

    if (!foundConsole) {
      manager.addConsoles(new IConsole[] { console });
    }

    return console;
  }

  private void tail(final IProject project) {
    IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
    final String logName = "access"; // TODO
    String consoleName = MessageFormat.format("Tail {0} :: {1}", logName, project.getName());
    final IOConsole console = getTailConsole(consoleName);
    manager.showConsoleView(console);

    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        IOConsoleOutputStream consoleOutputStream = console.newOutputStream();
        try {
          BeesSDK.tail("imade/alpacentauri", logName, consoleOutputStream); // TODO
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    t.start();
  }
}
