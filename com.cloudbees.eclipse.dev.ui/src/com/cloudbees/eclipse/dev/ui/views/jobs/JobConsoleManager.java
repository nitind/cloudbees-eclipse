package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job.Build;

public class JobConsoleManager {

  private final Map<Build, JobConsole> consoles = new HashMap<Build, JobConsole>();

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
      Object build = ((MessageConsole) console).getAttribute(JobConsole.ATTRIBUTE_BUILD);
      if (build instanceof Build) {
        JobConsole jobConsole = this.consoles.get(build);
        if (jobConsole != null) {
          jobConsole.dispose();
          this.consoles.remove(build);
        }
      }
    }
  }

  public JobConsole showConsole(final JenkinsJobsResponse.Job.Build build) {
    Assert.isNotNull(build);
    JobConsole console = this.consoles.get(build);
    if (console == null) {
      console = new JobConsole(this.consoleManager, build);
      this.consoles.put(build, console);
    }
    console.show();
    return console;
  }

}
