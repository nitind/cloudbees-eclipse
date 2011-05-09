package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBCloudLaunchShortcut implements ILaunchShortcut {

  private ILaunchConfiguration configuration;
  private String name;
  boolean cancelled = false;

  @Override
  public void launch(ISelection selection, String mode) {
    if (selection instanceof IStructuredSelection) {

      IStructuredSelection structuredSelection = (IStructuredSelection) selection;

      Object element = structuredSelection.getFirstElement();

      if (element instanceof IProject) {
        this.name = ((IProject) element).getName();
      } else if (element instanceof IJavaProject) {
        this.name = ((IJavaProject) element).getProject().getName();
      }

      try {
        List<ILaunchConfiguration> launchConfigurations = CBRunUtil.getOrCreateCloudBeesLaunchConfigurations(this.name,
            true, null);
        this.configuration = launchConfigurations.get(launchConfigurations.size() - 1);

        String accountName = this.configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_ACCOUNT_ID,
            "");
        if (accountName.equals("")) {
          Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
              try {
                Shell shell = Display.getDefault().getActiveShell();
                AccountSelectionDialog dialog = new AccountSelectionDialog(shell);
                dialog.open();
                String account = dialog.getSelectedAccountName();

                if (dialog.getReturnCode() != IDialogConstants.OK_ID || account == null || account.length() == 0) {
                  String errorMsg = MessageFormat.format("Account is not specified.", CBCloudLaunchShortcut.this.name);
                  Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, errorMsg);
                  ErrorDialog.openError(shell, "Error", "Launch error", status);
                  CBCloudLaunchShortcut.this.cancelled = true;
                  return;
                }
                CBCloudLaunchShortcut.this.cancelled = false;

                ILaunchConfigurationWorkingCopy copy = CBCloudLaunchShortcut.this.configuration.getWorkingCopy();
                copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_ACCOUNT_ID, account);
                CBCloudLaunchShortcut.this.configuration = copy.doSave();
              } catch (CoreException e) {
                CBRunUiActivator.logError(e);
              }
            }
          });
        }

        if (!this.cancelled) {
          DebugUITools.launch(this.configuration, mode);
        }
      } catch (CoreException e) {
        CBRunUiActivator.logError(e);
      }

    }
  }

  @Override
  public void launch(IEditorPart editor, String mode) {
    // TODO Auto-generated method stub

  }

}
