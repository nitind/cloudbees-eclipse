package com.cloudbees.eclipse.dev.ui.views.jobs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job.Build;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class JobConsole {

  final static String CONSOLE_TYPE = "com.cloudbees.eclipse.dev.ui.views.jobs.JobConsole";
  final static String ATTRIBUTE_BUILD = "com.cloudbees.eclipse.dev.ui.views.jobs.JobConsole.build";

  private final IConsoleManager consoleManager;
  private MessageConsoleStream stream;
  private Build build;

  public JobConsole(final IConsoleManager consoleManager, final Build build) {
    Assert.isNotNull(consoleManager);
    Assert.isNotNull(build);
    this.consoleManager = consoleManager;
    this.build = build;
  }

  public MessageConsole show() {
    MessageConsole console = null;
    if (this.stream == null) {
      console = new MessageConsole(NLS.bind("Output for Build \"{0}\"", this.build.fullDisplayName), CONSOLE_TYPE,
          CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BUILD_CONSOLE_LOG), true);
      this.consoleManager.addConsoles(new IConsole[] { console });
      console.setAttribute(ATTRIBUTE_BUILD, this.build);

      this.stream = console.newMessageStream();
    }

    retrieveLog();

    this.consoleManager.showConsoleView(console);
    return console;
  }

  public void dispose() {
  }

  private void retrieveLog() {
    try {
      JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(this.build.url);
      String logPart = service.getBuildLog(this.build.url, 0, new NullProgressMonitor());
      JobConsole.this.stream.println(logPart);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
