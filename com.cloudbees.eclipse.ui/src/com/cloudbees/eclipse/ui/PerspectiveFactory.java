package com.cloudbees.eclipse.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

  public void createInitialLayout(IPageLayout layout) {
    String editorArea = layout.getEditorArea();

    layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.75f, editorArea);
    layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.25f, editorArea);

    IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.66f, editorArea);
    bottom.addView("com.cloudbees.eclipse.dev.ui.views.jobs.JobsView");
    bottom.addView("com.cloudbees.eclipse.dev.ui.views.instances.JenkinsTreeView");
    bottom.addView("com.cloudbees.eclipse.dev.ui.views.build.BuildHistoryView");
    bottom.addView("com.cloudbees.eclipse.run.ui.views.AppListView");

  }

}
