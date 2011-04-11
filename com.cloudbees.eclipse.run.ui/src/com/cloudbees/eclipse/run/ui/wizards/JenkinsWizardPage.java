package com.cloudbees.eclipse.run.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class JenkinsWizardPage extends CBWizardPage {

  public static final String PAGE_NAME = JenkinsWizardPage.class.getSimpleName();
  private static final String PAGE_TITLE = "New Jenkins Job";
  private static final String PAGE_DESCRIPTION = "Optionally you can create a new Jenkins job for this project.";
  private static final String JENKINS_JOB_CHECK_LABEL = "Make new Jenkins job for this project";
  private static final String JENKINS_INSTANCE_LABEL = "Jenkins instance:";
  private static final String JOB_NAME_LABEL = "Job Name:";
  private static final String ERR_JOB_NAME = "Please provide a job name";
  private static final String ERR_JENKINS_INSTANCE = "Please provide a Jenkins instance";

  private Button makeJobCheck;
  private Text jobNameText;
  private Combo jenkinsInstancesCombo;
  private ComboViewer jenkinsComboViewer;
  private JenkinsInstance jenkinsInstance;
  private Label jenkinsInstanceLabel;
  private Label jobNameLabel;

  protected JenkinsWizardPage() {
    super(PAGE_NAME);
    setTitle(PAGE_TITLE);
    setDescription(PAGE_DESCRIPTION);
  }

  @Override
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
    this.makeJobCheck.setText(JENKINS_JOB_CHECK_LABEL);
    this.makeJobCheck.setSelection(false);
    this.makeJobCheck.setLayoutData(data);
    this.makeJobCheck.addSelectionListener(new MakeJenkinsJobSelectionListener());

    data = new GridData();
    data.verticalAlignment = SWT.CENTER;

    this.jenkinsInstanceLabel = new Label(container, SWT.NULL);
    this.jenkinsInstanceLabel.setLayoutData(data);
    this.jenkinsInstanceLabel.setText(JENKINS_INSTANCE_LABEL);
    this.jenkinsInstanceLabel.setEnabled(false);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jenkinsInstancesCombo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
    this.jenkinsInstancesCombo.setLayoutData(data);
    this.jenkinsInstancesCombo.setEnabled(false);
    this.jenkinsComboViewer = new ComboViewer(this.jenkinsInstancesCombo);
    this.jenkinsComboViewer.setLabelProvider(new JenkinsInstanceLabelProvider());
    this.jenkinsComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
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

    this.jobNameLabel = new Label(container, SWT.NULL);
    this.jobNameLabel.setLayoutData(data);
    this.jobNameLabel.setText(JOB_NAME_LABEL);
    this.jobNameLabel.setEnabled(false);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jobNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
    this.jobNameText.setLayoutData(data);
    this.jobNameText.setEnabled(false);
    this.jobNameText.addModifyListener(new ModifyListener() {
      @Override
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

    @Override
    public void widgetSelected(SelectionEvent e) {
      handleEvent();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      handleEvent();
    }

    private void handleEvent() {
      boolean selected = isMakeNewJob();
      JenkinsWizardPage.this.jobNameText.setEnabled(selected);
      JenkinsWizardPage.this.jenkinsInstancesCombo.setEnabled(selected);
      JenkinsWizardPage.this.jenkinsInstanceLabel.setEnabled(selected);
      JenkinsWizardPage.this.jobNameLabel.setEnabled(selected);
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

  public void loadJenkinsInstances() {
    if (this.jenkinsInstancesCombo.getItemCount() > 0) {
      return;
    }

    final Display display = getShell().getDisplay();
    IRunnableWithProgress operation = new IRunnableWithProgress() {

      @Override
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          CloudBeesUIPlugin plugin = CloudBeesUIPlugin.getDefault();
          List<JenkinsInstance> manualInstances = plugin.loadManualJenkinsInstances();
          List<JenkinsInstance> cloudInstances = plugin.loadDevAtCloudInstances(monitor);
          List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
          instances.addAll(manualInstances);
          instances.addAll(cloudInstances);
          final Object[] items = instances.toArray();

          display.syncExec(new Runnable() {
            @Override
            public void run() {
              JenkinsWizardPage.this.jenkinsComboViewer.add(items);
            }
          });

        } catch (Exception e) {
          CBRunUiActivator.logError(e);
        }
      }
    };

    try {
      getContainer().run(true, false, operation);
    } catch (Exception e) {
      CBRunUiActivator.logError(e);
    }

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
      updateErrorStatus(ERR_JOB_NAME);
      return;
    }

    if (getJenkinsInstance() == null) {
      updateErrorStatus(ERR_JENKINS_INSTANCE);
      return;
    }

    updateErrorStatus(null);
  }

  @Override
  boolean canFinish() {
    return true;
  }

}
