package com.cloudbees.eclipse.run.ui.wizards;

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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;

public abstract class CBJenkinsComposite extends Composite {

  private static final String GROUP_LABEL = "Jenkins";
  private static final String JENKINS_JOB_CHECK_LABEL = "New Jenkins job";
  private static final String JENKINS_INSTANCE_LABEL = "Jenkins instance:";
  private static final String JOB_NAME_LABEL = "Job Name:";
  private static final String ERR_JOB_NAME = "Please provide a job name";
  private static final String ERR_JENKINS_INSTANCE = "Please provide a Jenkins instance";

  private JenkinsInstance[] jenkinsInstancesArray;
  private JenkinsInstance jenkinsInstance;

  private Button makeJobCheck;
  private Label jenkinsInstanceLabel;
  private Combo jenkinsInstancesCombo;
  private ComboViewer jenkinsComboViewer;
  private Label jobNameLabel;
  private Text jobNameText;

  public CBJenkinsComposite(Composite parent) {
    super(parent, SWT.NONE);
    init();
  }

  private void init() {

    FillLayout layout = new FillLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.spacing = 0;

    setLayout(layout);

    Group group = new Group(this, SWT.NONE);
    group.setText(GROUP_LABEL);
    group.setLayout(new GridLayout(2, false));

    GridData data = new GridData();
    data.horizontalSpan = 2;
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.LEFT;

    this.makeJobCheck = new Button(group, SWT.CHECK);
    this.makeJobCheck.setText(JENKINS_JOB_CHECK_LABEL);
    this.makeJobCheck.setSelection(false);
    this.makeJobCheck.setLayoutData(data);
    this.makeJobCheck.addSelectionListener(new MakeJenkinsJobSelectionListener());

    data = new GridData();
    data.verticalAlignment = SWT.CENTER;

    this.jenkinsInstanceLabel = new Label(group, SWT.NULL);
    this.jenkinsInstanceLabel.setLayoutData(data);
    this.jenkinsInstanceLabel.setText(JENKINS_INSTANCE_LABEL);
    this.jenkinsInstanceLabel.setEnabled(false);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jenkinsInstancesCombo = new Combo(group, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
    this.jenkinsInstancesCombo.setLayoutData(data);
    this.jenkinsInstancesCombo.setEnabled(false);
    this.jenkinsComboViewer = new ComboViewer(this.jenkinsInstancesCombo);
    this.jenkinsComboViewer.setLabelProvider(new JenkinsInstanceLabelProvider());
    this.jenkinsComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = CBJenkinsComposite.this.jenkinsComboViewer.getSelection();
        if (selection instanceof StructuredSelection) {
          StructuredSelection structSelection = (StructuredSelection) selection;
          CBJenkinsComposite.this.jenkinsInstance = (JenkinsInstance) structSelection.getFirstElement();
        }
        validate();
      }
    });

    data = new GridData();
    data.verticalAlignment = SWT.CENTER;

    this.jobNameLabel = new Label(group, SWT.NULL);
    this.jobNameLabel.setLayoutData(data);
    this.jobNameLabel.setText(JOB_NAME_LABEL);
    this.jobNameLabel.setEnabled(false);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jobNameText = new Text(group, SWT.BORDER | SWT.SINGLE);
    this.jobNameText.setLayoutData(data);
    this.jobNameText.setEnabled(false);
    this.jobNameText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        validate();
      }
    });
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

  protected abstract void updateErrorStatus(String errorMsg);

  protected abstract JenkinsInstance[] loadJenkinsInstances();

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
      if (selected && CBJenkinsComposite.this.jenkinsInstancesArray == null) {
        CBJenkinsComposite.this.jenkinsInstancesArray = loadJenkinsInstances();
        CBJenkinsComposite.this.jenkinsComboViewer.add(CBJenkinsComposite.this.jenkinsInstancesArray);
      }
      CBJenkinsComposite.this.jobNameText.setEnabled(selected);
      CBJenkinsComposite.this.jenkinsInstancesCombo.setEnabled(selected);
      CBJenkinsComposite.this.jenkinsInstanceLabel.setEnabled(selected);
      CBJenkinsComposite.this.jobNameLabel.setEnabled(selected);
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
}
