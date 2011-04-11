package com.cloudbees.eclipse.run.ui.wizards;

import java.util.List;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;

public class JenkinsWizardPage extends CBWizardPage {

  private static final String PAGE_NAME = JenkinsWizardPage.class.getSimpleName();
  private static final String PAGE_TITLE = "New Jenkins Job";
  private static final String PAGE_DESCRIPTION = "Optionally you can create a new Jenkins job for this project.";
  private static final String JOB_NAME_LABEL = "Job Name:";

  private Button makeJobCheck;
  private Text jobNameText;
  private Combo jenkinsInstancesCombo;
  private ComboViewer jenkinsComboViewer;
  private JenkinsInstance jenkinsInstance;

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

    Label jenkinsInstanceLabel = new Label(container, SWT.NULL);
    jenkinsInstanceLabel.setLayoutData(data);
    jenkinsInstanceLabel.setText("Jenkins instance:");

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jenkinsInstancesCombo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
    this.jenkinsInstancesCombo.setLayoutData(data);
    this.jenkinsInstancesCombo.setEnabled(false);
    this.jenkinsComboViewer = new ComboViewer(this.jenkinsInstancesCombo);
    this.jenkinsComboViewer.setLabelProvider(new JenkinsInstanceLabelProvider());
    this.jenkinsComboViewer.add(getJenkinsInstances());
    this.jenkinsComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = JenkinsWizardPage.this.jenkinsComboViewer.getSelection();
        if (selection instanceof StructuredSelection) {
          StructuredSelection structSelection = (StructuredSelection) selection;
          JenkinsWizardPage.this.jenkinsInstance = (JenkinsInstance) structSelection.getFirstElement();
        }
        validate();
      }
    });

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
    this.jobNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validate();
      }
    });

    setControl(container);
  }

  public String getJobNameText() {
    return this.jobNameText.getText();
  }

  public void setJobNameText(String text) {
    this.jobNameText.setText(text);
  }

  public boolean isMakeNewJob() {
    return this.makeJobCheck.getSelection();
  }

  public JenkinsInstance getJenkinsInstance() {
    return this.jenkinsInstance;
  }

  private class MakeJenkinsJobSelectionListener implements SelectionListener {

    public void widgetSelected(SelectionEvent e) {
      handleEvent();
    }

    public void widgetDefaultSelected(SelectionEvent e) {
      handleEvent();
    }

    private void handleEvent() {
      boolean selected = isMakeNewJob();
      JenkinsWizardPage.this.jobNameText.setEnabled(selected);
      JenkinsWizardPage.this.jenkinsInstancesCombo.setEnabled(selected);
      validate();
    }
  }

  private class JenkinsInstanceLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
      if (element instanceof JenkinsInstance) {
        JenkinsInstance instance = (JenkinsInstance) element;

        if (instance.label != null && instance.label.length() > 0) {
          return instance.label;
        }

        return instance.url;
      }

      return super.getText(element);
    }

  }

  private JenkinsInstance[] getJenkinsInstances() {
    List<JenkinsInstance> instances = ((CBSampleWebAppWizard) getWizard()).getJenkinsInstances();
    JenkinsInstance[] instancesArray = new JenkinsInstance[instances.size()];
    return instances.toArray(instancesArray);
  }

  private void updateErrorStatus(String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  private void validate() {
    if (!isMakeNewJob()) {
      updateErrorStatus(null);
      return;
    }

    String jobName = getJobNameText();
    if (jobName == null || jobName.length() == 0) {
      updateErrorStatus("Please provide a job name");
      return;
    }

    if (getJenkinsInstance() == null) {
      updateErrorStatus("Please provide a Jenkins instance");
      return;
    }

    updateErrorStatus(null);
  }

  @Override
  boolean canFinish() {
    return true;
  }

}
