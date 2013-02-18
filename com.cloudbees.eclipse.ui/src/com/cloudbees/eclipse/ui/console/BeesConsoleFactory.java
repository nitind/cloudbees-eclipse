package com.cloudbees.eclipse.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

public class BeesConsoleFactory implements IConsoleFactory {

  private static IConsoleManager consoleManager;

  private static BeesConsole console;


  public void openConsole() {
    openGlobalConsole();
  }

  public static void openGlobalConsole() {
    if (consoleManager == null) {
      consoleManager = ConsolePlugin.getDefault().getConsoleManager();

      consoleManager.addConsoleListener(new IConsoleListener() {
        public void consolesAdded(IConsole[] consoles) {
        }

        public void consolesRemoved(IConsole[] consoles) {
          for (int i = 0; i < consoles.length; i++) {
            if (consoles[i] == console) {
              console = null;
            }
          }
        }

      });
    }
    
    if (console == null) {
      console = new BeesConsole();
      consoleManager.addConsoles(new IConsole[] { console });
    }
    consoleManager.showConsoleView(console);
  }

}
