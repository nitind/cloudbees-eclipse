package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public class CBLaunchShortcut implements ILaunchShortcut {

  @Override
  public void launch(ISelection selection, String mode) {
    if (selection instanceof IStructuredSelection) {

      IStructuredSelection structuredSelection = (IStructuredSelection) selection;

      String name = null;
      IFile buildXml = null;
      Object element = structuredSelection.getFirstElement();

      if (element instanceof IProject) {
        IProject project = (IProject) element;
        name = project.getName();
        buildXml = getBuildXml(project);
      } else if (element instanceof IJavaProject) {
        IJavaProject javaProject = (IJavaProject) element;
        name = javaProject.getProject().getName();
        buildXml = getBuildXml(javaProject.getProject());
      }

      if (buildXml.exists()) {
        try {
          List<ILaunchConfiguration> launchConfigurations = CBRunUtil.getOrCreateCloudBeesLaunchConfigurations(name,
              null);
          ILaunchConfiguration configuration = launchConfigurations.get(launchConfigurations.size() - 1);
          DebugUITools.launch(configuration, mode);
        } catch (CoreException e) {
          CBRunUiActivator.logError(e);
        }
      } else {
        final String msg = MessageFormat.format("Cannot launch {0}, could not find {1}", name, buildXml.getLocation());

        Display.getDefault().asyncExec(new Runnable() {
          @Override
          public void run() {
            Shell shell = Display.getDefault().getActiveShell();
            Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, msg);
            ErrorDialog.openError(shell, "Error", null, status);
          }
        });
      }
    }
  }

  @Override
  public void launch(IEditorPart editor, String mode) {
    // not currently supported
  }

  private IFile getBuildXml(IProject project) {
    return project.getFile("build.xml");
  }

}
