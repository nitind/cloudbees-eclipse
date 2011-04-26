package com.cloudbees.eclipse.ui.wizard;

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

public abstract class NewJenkinsJobComposite extends Composite {

  private static final String GROUP_LABEL = "Jenkins";
  private static final String JENKINS_JOB_CHECK_LABEL = "New Jenkins job";
  private static final String JENKINS_INSTANCE_LABEL = "Jenkins instance:";
  private static final String JOB_NAME_LABEL = "Job Name:";

  public static final String ERR_JOB_NAME = "Please provide a job name";
  public static final String ERR_JENKINS_INSTANCE = "Please provide a Jenkins instance";

  private JenkinsInstance[] jenkinsInstancesArray;
  private JenkinsInstance jenkinsInstance;

  private Button makeJobCheck;
  private Label jenkinsInstanceLabel;
  private Combo jenkinsInstancesCombo;
  private ComboViewer jenkinsComboViewer;
  private Label jobNameLabel;
  private Text jobNameText;
  private final Group group;

  public NewJenkinsJobComposite(Composite parent) {
    super(parent, SWT.NONE);

    FillLayout layout = new FillLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.spacing = 0;

    setLayout(layout);

    this.group = new Group(this, SWT.NONE);
    this.group.setText(GROUP_LABEL);
    this.group.setLayout(new GridLayout(2, false));

    createComponents();
  }

  public String getJobNameText() {
    if (this.jobNameText == null) {
      return null;
    }
    return this.jobNameText.getText();
  }

  public void setJobNameText(String text) {
    this.jobNameText.setText(text);
  }

  public boolean isMakeNewJob() {
    return this.makeJobCheck != null && this.makeJobCheck.getSelection();
  }

  public JenkinsInstance getJenkinsInstance() {
    return this.jenkinsInstance;
  }

  public void addJobCheckListener(SelectionListener listener) {
    if (listener != null && this.makeJobCheck != null) {
      this.makeJobCheck.addSelectionListener(listener);
    }
  }

  protected void createJobCheck() {
    GridData data = new GridData();
    data.horizontalSpan = 2;
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.LEFT;

    this.makeJobCheck = new Button(this.group, SWT.CHECK);
    this.makeJobCheck.setText(JENKINS_JOB_CHECK_LABEL);
    this.makeJobCheck.setSelection(false);
    this.makeJobCheck.setLayoutData(data);
    this.makeJobCheck.addSelectionListener(new MakeJenkinsJobSelectionListener());
  }

  protected void createInstanceChooser() {
    GridData data = new GridData();
    data.verticalAlignment = SWT.CENTER;

    this.jenkinsInstanceLabel = new Label(this.group, SWT.NULL);
    this.jenkinsInstanceLabel.setLayoutData(data);
    this.jenkinsInstanceLabel.setText(JENKINS_INSTANCE_LABEL);
    this.jenkinsInstanceLabel.setEnabled(this.makeJobCheck == null);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jenkinsInstancesCombo = new Combo(this.group, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
    this.jenkinsInstancesCombo.setLayoutData(data);
    this.jenkinsInstancesCombo.setEnabled(this.makeJobCheck == null);
    this.jenkinsComboViewer = new ComboViewer(this.jenkinsInstancesCombo);
    this.jenkinsComboViewer.setLabelProvider(new JenkinsInstanceLabelProvider());
    this.jenkinsComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = NewJenkinsJobComposite.this.jenkinsComboViewer.getSelection();
        if (selection instanceof StructuredSelection) {
          StructuredSelection structSelection = (StructuredSelection) selection;
          NewJenkinsJobComposite.this.jenkinsInstance = (JenkinsInstance) structSelection.getFirstElement();
        }
        validate();
      }
    });
  }

  protected void createJobText() {
    GridData data = new GridData();
    data.verticalAlignment = SWT.CENTER;

    this.jobNameLabel = new Label(this.group, SWT.NULL);
    this.jobNameLabel.setLayoutData(data);
    this.jobNameLabel.setText(JOB_NAME_LABEL);
    this.jobNameLabel.setEnabled(this.makeJobCheck == null);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.jobNameText = new Text(this.group, SWT.BORDER | SWT.SINGLE);
    this.jobNameText.setLayoutData(data);
    this.jobNameText.setEnabled(this.makeJobCheck == null);
    this.jobNameText.addModifyListener(new ModifyListener() {

      public void modifyText(ModifyEvent e) {
        validate();
      }
    });
  }

  protected abstract void createComponents();

  protected abstract void validate();

  protected abstract JenkinsInstance[] loadJenkinsInstances();

  protected void addJenkinsInstancesToUI() {
    NewJenkinsJobComposite.this.jenkinsInstancesArray = loadJenkinsInstances();
    NewJenkinsJobComposite.this.jenkinsComboViewer.add(NewJenkinsJobComposite.this.jenkinsInstancesArray);
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
      if (selected && NewJenkinsJobComposite.this.jenkinsInstancesArray == null) {
        addJenkinsInstancesToUI();
      }
      NewJenkinsJobComposite.this.jobNameText.setEnabled(selected);
      NewJenkinsJobComposite.this.jenkinsInstancesCombo.setEnabled(selected);
      NewJenkinsJobComposite.this.jenkinsInstanceLabel.setEnabled(selected);
      NewJenkinsJobComposite.this.jobNameLabel.setEnabled(selected);
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
