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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBCloudLaunchShortcut implements ILaunchShortcut {

  @Override
  public void launch(ISelection selection, String mode) {
    if (selection instanceof IStructuredSelection) {

      IStructuredSelection structuredSelection = (IStructuredSelection) selection;

      String name = null;
      Object element = structuredSelection.getFirstElement();

      if (element instanceof IProject) {
        name = ((IProject) element).getName();
      } else if (element instanceof IJavaProject) {
        name = ((IJavaProject) element).getProject().getName();
      }

      try {
        List<ILaunchConfiguration> launchConfigurations = CBRunUtil
            .getOrCreateCloudBeesLaunchConfigurations(name, true);
        ILaunchConfiguration configuration = launchConfigurations.get(launchConfigurations.size() - 1);

        String accountName = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_ACCOUNT_ID, "");
        if (accountName.equals("")) {
          Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
          AccountSelectionDialog dialog = new AccountSelectionDialog(shell);
          dialog.open();
          String account = dialog.getSelectedAccountName();

          if (dialog.getReturnCode() != IDialogConstants.OK_ID || account == null || account.length() == 0) {
            String errorMsg = MessageFormat.format("Account is not specified.", name);
            Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, errorMsg);
            ErrorDialog.openError(shell, "Error", "Launch error", status);
            return;
          }

          ILaunchConfigurationWorkingCopy copy = configuration.getWorkingCopy();
          copy.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_ACCOUNT_ID, account);
          configuration = copy.doSave();
        }

        DebugUITools.launch(configuration, mode);
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
