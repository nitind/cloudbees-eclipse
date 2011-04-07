package com.cloudbees.eclipse.run.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class JenkinsWizardPage extends WizardPage {

  private static final String PAGE_NAME = JenkinsWizardPage.class.getSimpleName();
  private static final String PAGE_TITLE = "New Jenkins Job";
  private static final String PAGE_DESCRIPTION = "Optionally you can create a new Jenkins job for this project.";
  private static final String JOB_NAME_LABEL = "Job Name:";

  private Button makeJobCheck;
  private Text jobNameText;

  protected JenkinsWizardPage() {
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
    data.horizontalSpan = 2;
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.LEFT;

    this.makeJobCheck = new Button(container, SWT.CHECK);
    this.makeJobCheck.setText("Make new Jenkins job for this project");
    this.makeJobCheck.setSelection(false);
    this.makeJobCheck.setLayoutData(data);
    this.makeJobCheck.addSelectionListener(new MakeJenkinsJobSelectionListener());

    data = new GridData();
    data.verticalAlignment = SWT.CENTER;

    Label label = new Label(container, SWT.NULL);
    label.setLayoutData(data);
    label.setText(JOB_NAME_LABEL);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jobNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
    this.jobNameText.setLayoutData(data);
    this.jobNameText.setEnabled(false);

    setControl(container);
  }

  public Text getJobNameText() {
    return this.jobNameText;
  }

  public Button getMakeJobCheck() {
    return this.makeJobCheck;
  }

  private class MakeJenkinsJobSelectionListener implements SelectionListener {

    public void widgetSelected(SelectionEvent e) {
      handleEvent();
    }

    public void widgetDefaultSelected(SelectionEvent e) {
      handleEvent();
    }

    private void handleEvent() {
      boolean selected = JenkinsWizardPage.this.getMakeJobCheck().getSelection();
      JenkinsWizardPage.this.jobNameText.setEnabled(selected);
    }
  }
}
