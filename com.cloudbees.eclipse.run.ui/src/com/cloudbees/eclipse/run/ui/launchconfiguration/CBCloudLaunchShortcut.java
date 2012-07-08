package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

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
