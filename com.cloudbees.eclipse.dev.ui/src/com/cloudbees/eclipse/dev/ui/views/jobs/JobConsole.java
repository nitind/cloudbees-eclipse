package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsConsoleLogResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job.Build;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class JobConsole {

  final static String CONSOLE_TYPE = "com.cloudbees.eclipse.dev.ui.views.jobs.JobConsole";
  final static String ATTRIBUTE_BUILD = "com.cloudbees.eclipse.dev.ui.views.jobs.JobConsole.build";

  private final IConsoleManager consoleManager;
  private volatile MessageConsoleStream stream;
  protected Build build;

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
    try {
      this.stream.close();
    } catch (Exception e) {
      e.printStackTrace(); // TODO log
    } finally {
      this.stream = null;
    }
    this.build = null;
  }

  private void retrieveLog() {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading console output for \""
        + this.build.fullDisplayName + "\"") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        try {
          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(JobConsole.this.build.url);
          JenkinsConsoleLogResponse requestResponse = new JenkinsConsoleLogResponse();
          requestResponse.viewUrl = JobConsole.this.build.url;

          long safety = 60 * 60;
          do {
            if (requestResponse.hasMore) {
              try {
                Thread.sleep(1000);
              } catch (Exception e) {
                // ignore
              }
            }

            if (JobConsole.this.stream == null || JobConsole.this.stream.isClosed()) {
              break;
            }

            requestResponse = service.getBuildLog(requestResponse, monitor);
            if (requestResponse.logPart != null) {
              BufferedReader in = new BufferedReader(new InputStreamReader(requestResponse.logPart));
              String data = null;
              while ((data = in.readLine()) != null) {
                if (JobConsole.this.stream.isClosed()) {
                  throw new OperationCanceledException();
                }
                JobConsole.this.stream.println(data);
              }
            }
          } while (requestResponse.hasMore && --safety > 0);

          return Status.OK_STATUS;
        } catch (OperationCanceledException e) {
          return Status.CANCEL_STATUS;
        } catch (Exception e) {
          e.printStackTrace(); // TODO
          return new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID, e.getMessage(), e);
        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(false);
    job.schedule();
  }

}
