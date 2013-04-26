package com.cloudbees.eclipse.ui.internal;
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

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.jdt.ui.JavaUI;


public class CloudBeesPerspectiveFactory implements IPerspectiveFactory {


  /**
   * Constructs a new Default layout engine.
   */
  public CloudBeesPerspectiveFactory() {
    super();
  }

  public void createInitialLayout(IPageLayout layout) {
    String editorArea = layout.getEditorArea();

    IFolderLayout folder= layout.createFolder("left", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
    folder.addView(JavaUI.ID_PACKAGES);
    folder.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
    folder.addPlaceholder("org.eclipse.ui.views.ResourceNavigator");
    folder.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER);

    IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
    outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
    outputfolder.addView(JavaUI.ID_JAVADOC_VIEW);
    outputfolder.addView(JavaUI.ID_SOURCE_VIEW);
    outputfolder.addPlaceholder("org.eclipse.search.ui.views.SearchView");
    outputfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
    outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
    outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);

    IFolderLayout cbFolder = layout.createFolder("right", IPageLayout.RIGHT, (float)0.75, editorArea); //$NON-NLS-1$
/*    
    <perspectiveExtension
    targetID="com.cloudbees.eclipse.ui.CloudBeesPerspective">
 <view
       id="com.cloudbees.eclipse.ui.views.CBTreeView"
       ratio="0.5"
       relationship="top"
       relative="org.eclipse.ui.views.ContentOutline">
 </view>
</perspectiveExtension>
*/
    layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);

    cbFolder.addView("com.cloudbees.eclipse.ui.views.CBTreeView");
    //cbFolder.addView(IPageLayout.ID_OUTLINE);
    
    
    cbFolder.addPlaceholder("org.eclipse.ui.texteditor.TemplatesView");

    layout.addActionSet("org.eclipse.debug.ui.launchActionSet");
    layout.addActionSet(JavaUI.ID_ACTION_SET);
    layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
    layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

    // views - java
    layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
    layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
    layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
    layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);


    // views - search
    layout.addShowViewShortcut("org.eclipse.search.ui.views.SearchView");

    // views - debugging
    layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

    // views - standard workbench
    layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
    layout.addShowViewShortcut("org.eclipse.ui.views.ResourceNavigator");
    layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
    layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
    layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
    layout.addShowViewShortcut("org.eclipse.ui.texteditor.TemplatesView");
    layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$

    // new actions - Java project creation wizard
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard");   //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewJavaWorkingSetWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$

    // 'Window' > 'Open Perspective' contributions
    layout.addPerspectiveShortcut(JavaUI.ID_BROWSING_PERSPECTIVE);
    layout.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective");

  }
}
