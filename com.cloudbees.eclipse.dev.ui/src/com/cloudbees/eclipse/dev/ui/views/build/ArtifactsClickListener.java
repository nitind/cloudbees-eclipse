/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.dev.ui.views.build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.ArtifactPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Artifact;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ArtifactsClickListener implements IDoubleClickListener {

  @Override
  public void doubleClick(final DoubleClickEvent event) {
    Object selection = ((TreeSelection) event.getSelection()).getFirstElement();

    if (!(selection instanceof ArtifactPathItem)) {
      return;
    }

    final ArtifactPathItem item = (ArtifactPathItem) selection;
    final JenkinsBuildDetailsResponse build = item.parent;
    final Artifact artifact = item.item;

    if (!artifact.relativePath.endsWith(".war")) {
      return;
    }

    deployWar(build, artifact);
  }

  protected static String getWarUrl(final JenkinsBuildDetailsResponse build, final Artifact artifact) {
    String warPath = artifact.relativePath;
    if (!warPath.endsWith(".war")) {
      return null;
    }

    final String warUrl = build.url + "artifact/" + warPath;
    return warUrl;
  }

  public static void deployWar(final JenkinsBuildDetailsResponse build, final Artifact selectedWar) {
    final Artifact war;
    final ApplicationInfo app;

    if (build.artifacts == null) {
      return;
    }

    final DeployWarAppDialog[] selector = new DeployWarAppDialog[1];
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        final List<Artifact> wars = new ArrayList<Artifact>();
        final List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
        try {
          PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) {
              try {
                for (Artifact art : build.artifacts) {
                  if (art.relativePath != null && art.relativePath.endsWith(".war")) {
                    wars.add(art);
                    monitor.worked(1);
                  }
                }

                apps.addAll(BeesSDK.getList().getApplications());
                monitor.worked(1);
              } catch (Exception e) {
                CloudBeesDevUiPlugin.getDefault().getLogger().error(e);
              }
            }
          });
        } catch (InterruptedException e) {
          return;
        } catch (Exception e) {
          CloudBeesDevUiPlugin.getDefault().getLogger().error(e);
        }

        if (wars.isEmpty() || apps.isEmpty()) {
          MessageDialog.openInformation(CloudBeesUIPlugin.getActiveWindow().getShell(), "Deploy to RUN@cloud",
              "Deployment is not possible.");
          return;
        }

        selector[0] = new DeployWarAppDialog(CloudBeesUIPlugin.getActiveWindow().getShell(), wars, selectedWar, apps);
        selector[0].open();
      }
    });

    if (selector[0] == null) {
      return;
    }

    war = selector[0].getSelectedWar();
    app = selector[0].getSelectedApp();

    if (war == null || app == null) {
      return; // cancelled
    }

    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Deploy war to RUN@cloud") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        try {
          String warUrl = getWarUrl(build, war);
          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(warUrl);
          BufferedInputStream in = new BufferedInputStream(service.getArtifact(warUrl, monitor));

          String warName = warUrl.substring(warUrl.lastIndexOf("/"));
          final File tempWar = File.createTempFile(warName, null);
          tempWar.deleteOnExit();

          monitor.beginTask("Deploy war to RUN@cloud", 100);
          SubMonitor subMonitor = SubMonitor.convert(monitor, "Downloading war file...", 50);
          OutputStream out = new BufferedOutputStream(new FileOutputStream(tempWar));
          byte[] buf = new byte[1 << 12];
          int len = 0;
          while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
            subMonitor.worked(1);
          }

          out.flush();
          out.close();
          in.close();

          final String[] newAppUrl = new String[1];
          try {
            subMonitor = SubMonitor.convert(monitor, "Deploying war file to RUN@cloud...", 50);
            ApplicationDeployArchiveResponse result = BeesSDK.deploy(null, app.getId(), tempWar.getAbsoluteFile(),
                subMonitor);
            subMonitor.worked(50);
            if (result != null) {
              newAppUrl[0] = result.getUrl();
            }
          } catch (Exception e) {
            return new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID, e.getMessage(), e);
          } finally {
            monitor.done();
          }

          PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
              try {
                if (newAppUrl[0] != null) {
                  boolean openConfirm = MessageDialog.openConfirm(CloudBeesUIPlugin.getActiveWindow().getShell(),
                      "Deploy to RUN@cloud", "Deployment succeeded to RUN@cloud '" + app.getId() + "'.\nOpen "
                          + newAppUrl[0] + " in the browser?");

                  if (openConfirm) {
                    CloudBeesUIPlugin.getDefault().openWithBrowser(newAppUrl[0]);
                  }
                } else {
                  MessageDialog.openWarning(CloudBeesUIPlugin.getActiveWindow().getShell(), "Deploy to RUN@cloud",
                      "Deployment failed to RUN@cloud '" + app.getId() + "'.");
                }
              } catch (Exception e) {
                CloudBeesDevUiPlugin.getDefault().getLogger().error(e);
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
