package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.io.PrintStream;

import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;

import com.cloudbees.eclipse.run.ui.console.ConsoleUtil;

public class LaunchHook implements com.cloudbees.eclipse.run.core.launchconfiguration.LaunchHook {

  public LaunchHook() {
  }

  public void preStartHook(String projectName) {
    MessageConsole console = ConsoleUtil.getOrCreateConsole(projectName);
    IOConsoleOutputStream out = console.newOutputStream();
    System.setOut(new PrintStream(out));
    ConsoleUtil.revealConsole(console);
  }

  public void preStopHook() {
    System.setOut(System.out);
  }

}
