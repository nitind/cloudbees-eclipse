package com.cloudbees.eclipse.dev.ui.views.build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.forge.api.ForgeSync.ArtifactPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Artifact;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ArtifactsClickListener implements IDoubleClickListener {

  public void doubleClick(final DoubleClickEvent event) {
    Object selection = ((TreeSelection) event.getSelection()).getFirstElement();

    if (!(selection instanceof ArtifactPathItem)) {
      return;
    }

    final ArtifactPathItem item = (ArtifactPathItem) selection;
    final JenkinsBuildDetailsResponse build = item.parent;
    final Artifact artifact = item.item;

    final String warUrl = getWarUrl(build, artifact);
    if (warUrl == null) {
      return; // not war item
    }

    deployWar(build, warUrl);
  }

  protected static String getWarUrl(final JenkinsBuildDetailsResponse build, final Artifact artifact) {
    if (!artifact.relativePath.endsWith(".war")) {
      return null;
    }

    final String warUrl = build.url + "artifact/" + artifact.relativePath;
    return warUrl;
  }

  public static void deployWar(final JenkinsBuildDetailsResponse build, String warUrl) {
    final String appId;

    { // TODO show war & app selector
      try {
        if (warUrl == null) {
          if (build.artifacts == null) {
            return;
          }
          for (Artifact art : build.artifacts) {
            warUrl = getWarUrl(build, art);
            if (warUrl != null) {
              break; // found some war
            }
          }
        }
        if (warUrl == null) {
          return;
        }

        ApplicationListResponse list = BeesSDK.getList();
        ApplicationInfo first = list.getApplications().iterator().next();
        appId = first.getId();
        boolean confirm = MessageDialog.openConfirm(CloudBeesUIPlugin.getActiveWindow().getShell(), "Artifact WAR",
            "War: " + warUrl + " to " + appId);
        if (!confirm) {
          return;
        }
      } catch (Exception e) {
        e.printStackTrace(); // TODO report to user?
        return;
      }
    }

    final String warUrl_ = warUrl;
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Deploying Jenkins war artifact...") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        try {
          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(warUrl_);
          BufferedInputStream in = new BufferedInputStream(service.getArtifact(warUrl_, monitor));

          String warName = warUrl_.substring(warUrl_.lastIndexOf("/"));
          final File tempWar = File.createTempFile(warName, null);
          tempWar.deleteOnExit();

          OutputStream out = new BufferedOutputStream(new FileOutputStream(tempWar));
          byte[] buf = new byte[1 << 12];
          int len = 0;
          while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
          }

          out.flush();
          out.close();
          in.close();

          final String[] newAppUrl = new String[1];
          try {
            ApplicationDeployArchiveResponse result = BeesSDK.deploy(appId, tempWar.getCanonicalPath());
            newAppUrl[0] = result.getUrl();
          } catch (Exception e) {
            e.printStackTrace();
            // TODO report
          }

          PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
              try {
                monitor.done();

                if (newAppUrl[0] != null) {
                  boolean openConfirm = MessageDialog.openConfirm(CloudBeesUIPlugin.getActiveWindow().getShell(),
                      "Deploy to RUN@cloud", "Deployment succeeded to RUN@cloud '" + appId + "'.\nOpen " + newAppUrl[0]
                          + "?");

                  if (openConfirm) {
                    CloudBeesUIPlugin.getDefault().openWithBrowser(newAppUrl[0]);
                  }
                } else {
                  MessageDialog.openWarning(CloudBeesUIPlugin.getActiveWindow().getShell(), "Deploy to RUN@cloud",
                      "Deployment failed to RUN@cloud '" + appId + "'.");
                }
              } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          });

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

    job.setUser(true);
    job.schedule();
  }

}
