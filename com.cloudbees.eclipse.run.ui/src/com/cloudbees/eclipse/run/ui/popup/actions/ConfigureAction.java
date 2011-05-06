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
public class ConfigureAction implements IObjectActionDelegate {

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      final ISelection selection = ((ObjectPluginAction) action).getSelection();
      if (selection instanceof StructuredSelection) {
        ApplicationInfo appInfo = (ApplicationInfo) ((StructuredSelection) selection).getFirstElement();

        String id = appInfo.getId();
        int i = id.indexOf("/");
        String account = id.substring(0, i);

        CloudBeesUIPlugin.getDefault().openWithBrowser(
            "https://run.cloudbees.com/a/" + account + "#app-manage/development:" + appInfo.getId());
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
