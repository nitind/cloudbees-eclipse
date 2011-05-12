package com.cloudbees.eclipse.run.ui.launchconfiguration;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBCloudLaunchDelegate extends LaunchConfigurationDelegate {

  @SuppressWarnings("restriction")
  @Override
  public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
      final IProgressMonitor monitor) throws CoreException {

    monitor.beginTask("Deploying to RUN@cloud", 1);
    try {
      String projectName = configuration.getAttribute(ATTR_CB_PROJECT_NAME, "");

      for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
        if (project.getName().equals(projectName)) {
          String account = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_ACCOUNT_ID, "");
          String appId = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");
          String warPath = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_WAR_PATH, "");

          if (warPath != null && !warPath.isEmpty()) {
            warPath = project.getLocation().toOSString() + File.separatorChar + warPath;
            deployWar(configuration, account, appId, warPath);
          } else {

            if (CBCloudLaunchConfigurationTab.hasBuildXml(projectName)) {
              if (configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_WST_FLAG, false)) {

                start(project, account, appId);
                removeWstFlag(configuration);

              } else {
                deploy(project, account, appId);
              }
            } else {
              // configured war and no build.xml - let's try to find wars
              List<String> wars = WarSelecionComposite.findWars(project);
              if (wars.size() == 1) {
                warPath = project.getLocation().toOSString() + File.separatorChar + wars.get(0);
                deployWar(configuration, account, appId, warPath);
              } else {
                ILaunchConfiguration newconf = openWarSelectionDialog(configuration,
                    wars.toArray(new String[wars.size()]));

                warPath = newconf.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_WAR_PATH, "");
                if (warPath != null && !warPath.isEmpty()) {
                  warPath = project.getLocation().toOSString() + File.separatorChar + warPath;
                  deployWar(configuration, account, appId, warPath);
                }
              }
            }

          }

          handleExtensions(configuration, project);
          monitor.done();

        }
      }
    } catch (Exception e) {
      if (e.getMessage().contains("Target \"dist\" does not exist in the project")) {
        Display.getDefault().syncExec(new Runnable() {

          @Override
          public void run() {

            String message = "Please provide a valid CloudBees Project build.xml or provide a war file location!";
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Incorrect build.xml", message);

          }
        });
        CBRunUiActivator.logError(e);
      } else {
        CBRunUiActivator.logErrorAndShowDialog(e);
      }
    }
  }

  ILaunchConfiguration openWarSelectionDialog(final ILaunchConfiguration configuration, final String[] warPaths) {
    final ILaunchConfiguration[] conf = new ILaunchConfiguration[] { configuration };

    Display.getDefault().syncExec(new Runnable() {

      @Override
      public void run() {
        try {
          Shell shell = Display.getDefault().getActiveShell();
          WarSelectionDialog dialog = new WarSelectionDialog(shell, warPaths);
          dialog.open();
          String war = dialog.getSelectedWarPath();

          if (dialog.getReturnCode() != IDialogConstants.OK_ID || war == null || war.isEmpty()) {
            Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "War file is not specified.");
            ErrorDialog.openError(shell, "Error", "Launch error", status);
            return;
          }

          ILaunchConfigurationWorkingCopy copy = conf[0].getWorkingCopy();
          copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_WAR_PATH, war);
          conf[0] = copy.doSave();
        } catch (CoreException e) {
          CBRunUiActivator.logError(e);
        }
      }
    });

    return conf[0];
  }

  private void deploy(final IProject project, final String account, final String id) throws Exception,
      CloudBeesException, CoreException, FileNotFoundException {
    BeesSDK.deploy(project, account, id, true);
  }

  private void deployWar(final ILaunchConfiguration configuration, final String account, final String id,
      final String warPath)
      throws Exception, CloudBeesException, CoreException, FileNotFoundException {
    final String[] appId = new String[1];
    try {
      appId[0] = BeesSDK.getAppId(account, id, warPath);
    } catch (Exception e) {
      // failed to detect
    }
    if (appId[0] == null || appId[0].isEmpty()) {
      final ILaunchConfiguration[] conf = new ILaunchConfiguration[] { configuration };

      Display.getDefault().syncExec(new Runnable() {

        @Override
        public void run() {
          try {
            Shell shell = Display.getDefault().getActiveShell();
            CustomAppIdDialog dialog = new CustomAppIdDialog(shell);
            dialog.open();

            appId[0] = dialog.getAppId();

            if (dialog.getReturnCode() != IDialogConstants.OK_ID || appId[0] == null || appId[0].isEmpty()) {
              Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Custom App ID is not specified.");
              ErrorDialog.openError(shell, "Error", "Launch error", status);
              return;
            }

            ILaunchConfigurationWorkingCopy copy = conf[0].getWorkingCopy();
            copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, appId[0]);
            copy.doSave();

            if (appId[0].indexOf("/") < 0) {
              appId[0] = account + "/" + appId[0];
            }
          } catch (CoreException e) {
            CBRunUiActivator.logError(e);
          }
        }
      });
    }
    if (appId[0] != null && !appId[0].isEmpty()) {
      BeesSDK.deploy(appId[0], warPath);
    }
  }

  private void start(final IProject project, final String account, final String appId) throws Exception,
      CloudBeesException {
    BeesSDK.start(account, appId.equals("") ? project.getName() : appId);
  }

  private ILaunchConfigurationWorkingCopy removeWstFlag(final ILaunchConfiguration configuration) throws CoreException {
    ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
    workingCopy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_WST_FLAG, false);
    workingCopy.doSave();
    return workingCopy;
  }

  private IExtension[] handleExtensions(final ILaunchConfiguration configuration, final IProject project) {
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunUiActivator.PLUGIN_ID, "launchDelegateAditions").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("actions");
          if (executableExtension instanceof ILaunchExtraAction) {
            ((ILaunchExtraAction) executableExtension).action(configuration, project.getName(), false);
          }
        } catch (CoreException e) {
          CBRunUiActivator.logError(e);
        }
      }
    }
    return extensions;
  }
}
