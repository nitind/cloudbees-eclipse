package com.cloudbees.eclipse.run.ui.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class OpenInBrowserAction implements IObjectActionDelegate {

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      final ISelection selection = ((ObjectPluginAction) action).getSelection();
      if (selection instanceof StructuredSelection) {
        ApplicationInfo appInfo = (ApplicationInfo) ((StructuredSelection) selection).getFirstElement();
        if (appInfo.getUrls().length > 0) {
          CloudBeesUIPlugin.getDefault().openWithBrowser("http://" + appInfo.getUrls()[0]);
        }
      }
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
