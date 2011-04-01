package com.cloudbees.eclipse.run.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleUtil {

  // TODO add support for eclipse console

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

}
