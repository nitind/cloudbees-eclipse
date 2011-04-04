package com.cloudbees.eclipse.run.ui.console;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;

import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class ConsoleUtil {
  
  public static MessageConsole getOrCreateConsole(String consoleName) {
    IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();

    for (IConsole console : consoleManager.getConsoles()) {
      if (consoleName.equals(console.getName())) {
        return (MessageConsole) console;
      }
    }
    
    MessageConsole newConsole = new MessageConsole(consoleName, null);
    consoleManager.addConsoles(new IConsole[] { newConsole });
    return newConsole;
  }
  
  /**
   * Makes the console visible
   * 
   * @param console
   */
  public static void revealConsole(final IConsole console) {
    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchWindow activeWindow = CBRunUiActivator.getDefault().getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWindow.getActivePage();
        String id = IConsoleConstants.ID_CONSOLE_VIEW;
        try {
          IConsoleView view = (IConsoleView) activePage.showView(id);
          view.display(console);
        } catch (PartInitException e) {
          CBRunUiActivator.logError(e);
        }
      }
    });
  }
}
