/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.WorkbenchJob;

import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

@SuppressWarnings("restriction")
public abstract class DeployArtifactSelecionComposite extends Composite {

  private static final String GROUP_TITLE = "Custom Deploy Artifact";
  private static final String BUTTON_LABEL = "Choose...";
  private static final String HINT = "Choose a custom ear, war or jar file to deploy...";
  private static final String ERROR_TITLE = "Error";

  private Text filePathText;
  private Button chooseFileButton;
  private List<String> filePaths;
  private IProject project;
  private Button useCustomFile;

  public DeployArtifactSelecionComposite(final Composite parent) {
    super(parent, SWT.NONE);
    prepare(parent);
    createComponents(parent);
  }

  public void setProject(final IProject project) {
    if (this.project != null && project != null && this.project.getFullPath().equals(project.getFullPath())) {
      return;
    }

    this.project = project;
    if (this.project != null) {
      this.filePaths = findSupportedFiles(project);
    } else {
      this.filePaths = new ArrayList<String>();
    }

    this.filePathText.setText("");
    updateFields();
  }

  public void setUseCustomFile(final boolean enable) {
    this.useCustomFile.setSelection(enable);
    updateFields();
  }

  private void prepare(final Composite parent) {
    this.filePaths = findSupportedFiles(this.project);
  }

  public static List<String> findSupportedFiles(final IProject project) {
    final List<String> files = new ArrayList<String>();
    WorkbenchJob job = new WorkbenchJob("Searching for supported files") {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {

        if (project == null) {
          return Status.OK_STATUS;
        }

        try {
          project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(final IResource resource) throws CoreException {
              if (resource.getType() == IResource.FILE && 
                  (
                  "war".equalsIgnoreCase(resource.getFileExtension()) ||
                  "ear".equalsIgnoreCase(resource.getFileExtension()) ||
                  "jar".equalsIgnoreCase(resource.getFileExtension())
                  )
                  ) {
                files.add(resource.getProjectRelativePath().toOSString());
              }
              return true;
            }
          });
        } catch (CoreException e) {
          return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Failed to find supported files (ear, war, jar)", e);
        }

        return Status.OK_STATUS;
      }
    };

    IStatus status = job.runInUIThread(new NullProgressMonitor());
    if (!status.isOK()) {
      handleException("Exception while searching for files", status);
    }
    return files;
  }

  private void createComponents(final Composite parent) {
    setLayout(new FillLayout());

    Composite content = new Composite(this, SWT.NONE);
    content.setLayout(new GridLayout());

    Group group = new Group(content, SWT.NONE);
    group.setText(GROUP_TITLE);
    group.setLayout(new GridLayout(2, false));
    GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    group.setLayoutData(data);

    this.useCustomFile = new Button(group, SWT.CHECK);
    this.useCustomFile.setText("Specify custom ear, war or jar to deploy");
    new Label(group, SWT.NONE);

    this.filePathText = new Text(group, SWT.SINGLE | SWT.BORDER);
    this.filePathText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(final ModifyEvent e) {
        handleUpdate();
      }
    });

    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    this.filePathText.setLayoutData(data);
    this.filePathText.setFont(parent.getFont());
    this.filePathText.setMessage(HINT);
    this.filePathText.setText(getDefaultFilePath());

    this.chooseFileButton = SWTFactory.createPushButton(group, BUTTON_LABEL, null);
    this.chooseFileButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        openFileSelectionDialog();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
        openFileSelectionDialog();
      }

    });

    this.useCustomFile.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        updateFields();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
        widgetSelected(e);
      }
    });

    updateFields();
  }

  public abstract void handleUpdate();

  public IStatus validate() {
    String currentText = this.filePathText.getText();

    if (this.useCustomFile.getSelection() && (currentText == null || currentText.isEmpty())) {
      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Please provide ear, war or jar path.");
    }

    return Status.OK_STATUS;
  }

  public String getFilePath() {
    if (this.useCustomFile.getSelection()) {
      return this.filePathText.getText();
    } else {
      return "";
    }
  }

  private String getDefaultFilePath() {
    if (!this.filePaths.isEmpty()) {
      return this.filePaths.get(0);
    }
    return "";
  }

  private void handleException(final String msg, final Throwable t) {
    Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, msg, t);
    handleException(msg, status);
  }

  private static void handleException(final String msg, final IStatus status) {
    if (status.getException() != null) {
      CBRunUiActivator.logError(status.getException());
    }
    ErrorDialog.openError(CBRunUiActivator.getDefault().getWorkbench().getDisplay().getActiveShell(), ERROR_TITLE, msg, status);
  }

  private void openFileSelectionDialog() {
    FileSelectionDialog dialog = new FileSelectionDialog(getShell(), this.filePaths.toArray(new String[this.filePaths
        .size()]));
    dialog.open();
    if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
      if (dialog.getSelectedFilePath()!=null) {
        this.filePathText.setText(dialog.getSelectedFilePath());
      } else {
        this.filePathText.setText("");
      }
    }
  }

  public void setFilePath(final String filePath) {
    this.filePathText.setText(filePath);
    if (!this.useCustomFile.getSelection() && filePath != null && !filePath.isEmpty()) {
      this.useCustomFile.setSelection(true);
    }
    updateFields();
  }

  public void updateFields() {
    boolean enable = DeployArtifactSelecionComposite.this.useCustomFile.getSelection();
    DeployArtifactSelecionComposite.this.filePathText.setEnabled(enable);
    DeployArtifactSelecionComposite.this.chooseFileButton.setEnabled(enable);
    if (enable && this.filePathText.getText().isEmpty() && this.filePaths.size() == 1) {
      this.filePathText.setText(getDefaultFilePath());
    }
    handleUpdate();
  }
}
