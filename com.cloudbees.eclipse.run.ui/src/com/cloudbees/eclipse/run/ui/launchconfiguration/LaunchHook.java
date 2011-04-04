package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;

import com.cloudbees.eclipse.run.ui.console.ConsoleUtil;

public class LaunchHook implements com.cloudbees.eclipse.run.core.launchconfiguration.LaunchHook {

  public LaunchHook() {
  }

  public void handle(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) {
    MessageConsole console = ConsoleUtil.getOrCreateConsole(configuration.getName());
    IOConsoleOutputStream out = console.newOutputStream();
    System.setOut(new PrintStream(out));
    ConsoleUtil.revealConsole(console);
  }

}
