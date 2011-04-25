package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class JobConsoleManager {

  private final Map<String, JobConsole> consoles = new HashMap<String, JobConsole>();

  private final IConsoleManager consoleManager;

  private final IConsoleListener listener = new IConsoleListener() {
    public void consolesAdded(final IConsole[] consoles) {
      // ignore
    }

    public void consolesRemoved(final IConsole[] consoles) {
      for (IConsole console : consoles) {
        remove(console);
      }
    }
  };

  public JobConsoleManager() {
    this.consoleManager = ConsolePlugin.getDefault().getConsoleManager();
    this.consoleManager.addConsoleListener(this.listener);
  }

  public void unregister() {
    this.consoleManager.removeConsoleListener(this.listener);
  }

  protected void remove(final IConsole console) {
    if (JobConsole.CONSOLE_TYPE.equals(console.getType())) {
      Object url = ((MessageConsole) console).getAttribute(JobConsole.ATTRIBUTE_URL);
      if (url instanceof String) {
        JobConsole jobConsole = this.consoles.get(url);
        if (jobConsole != null) {
          jobConsole.dispose();
          this.consoles.remove(url);
        }
      }
    }
  }

  public JobConsole showConsole(final String name, final String url) {
    Assert.isNotNull(name);
    Assert.isNotNull(url);
    JobConsole console = this.consoles.get(url);
    if (console == null) {
      console = new JobConsole(this.consoleManager, name, url);
      this.consoles.put(url, console);
    }
    console.show();
    return console;
  }

}
