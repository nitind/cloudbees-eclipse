package com.cloudbees.eclipse.run.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CBSampleWebAppWizardPage extends WizardPage {

  public static final String PAGE_NAME = CBSampleWebAppWizardPage.class.getSimpleName();
  private static final String PAGE_TITLE = "Sample Web Application";
  private static final String PAGE_DESCRIPTION = "This wizard creates a new sample web application.";
  private static final String PROJECT_NAME_LABEL = "Project Name:";
  private static final String PROJECT_NAME_HINT = "Please enter the project name";

  private Text text;

  public CBSampleWebAppWizardPage() {
    super(PAGE_NAME);
    setTitle(PAGE_TITLE);
    setDescription(PAGE_DESCRIPTION);
  }

  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);

    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 20;
    layout.marginHeight = 20;

    container.setLayout(layout);

    GridData data = new GridData();
    data.verticalAlignment = SWT.CENTER;

    Label label = new Label(container, SWT.NULL);
    label.setLayoutData(data);
    label.setText(PROJECT_NAME_LABEL);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.text = new Text(container, SWT.BORDER | SWT.SINGLE);
    this.text.setMessage(PROJECT_NAME_HINT);
    this.text.setLayoutData(data);
    this.text.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        projectNameInputChanged(CBSampleWebAppWizardPage.this.text.getText());
      }
    });
    this.text.setFocus();

    setPageComplete(false);
    setControl(container);
  }

  private void projectNameInputChanged(String newName) {
    if (newName.isEmpty()) {
      updateErrorStatus("Project name must be specified");
      return;
    } else if (isProjectNameExists(newName)) {
      updateErrorStatus("Project with same name already exists in workspace");
      return;
    }
    updateErrorStatus(null);
  }

  private void updateErrorStatus(String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  public String getProjectName() {
    return this.text.getText();
  }

  private boolean isProjectNameExists(String projectName) {
    boolean nameExists = false;
    for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      if (project.getName().equals(projectName)) {
        nameExists = true;
        break;
      }
    }
    return nameExists;
  }
}
