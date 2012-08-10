package com.cloudbees.eclipse.ui.wizard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;

public abstract class NewJenkinsJobComposite extends Composite {

  private static final String GROUP_LABEL = "Jenkins";
  private static final String JENKINS_JOB_CHECK_LABEL = "New Jenkins job";
  private static final String JOB_NAME_LABEL = "Job Name:";

  public static final String ERR_JOB_NAME = "Please provide a job name";
  public static final String ERR_JENKINS_INSTANCE = "Jenkins service not enabled for this account.";
  public static final String ERR_DUPLICATE_JOB_NAME = "Please specify another job name to avoid overriding an existing job configuration";

  //private JenkinsInstance jenkinsInstance;
  private final Map<String, List<Job>> jobs;

  private Button makeJobCheck;
  private Label jobNameLabel;
  private Text jobNameText;
  private final Group group;
  private JenkinsInstance jenkinsInstance;

  public NewJenkinsJobComposite(Composite parent) {
    super(parent, SWT.NONE);

    this.jobs = new HashMap<String, List<Job>>();

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
    if (jenkinsInstance == null) {
      try {
        List<JenkinsInstance> instances = CloudBeesCorePlugin.getDefault().getGrandCentralService()
            .loadDevAtCloudInstances(new NullProgressMonitor());
        if (instances != null) {
          Iterator<JenkinsInstance> it = instances.iterator();
          if (it.hasNext()) {
            jenkinsInstance = it.next();
          }
        }
      } catch (CloudBeesException e) {
        // safetoignore
      }
    }
    return this.jenkinsInstance;
  }

  public List<Job> getInstanceJobs(JenkinsInstance instance) {
    if (!this.jobs.containsKey(instance.id)) {
      this.jobs.put(instance.id, loadJobs(instance));
    }
    return this.jobs.get(instance.id);
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

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

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

  protected abstract List<Job> loadJobs(JenkinsInstance instance);

  private class MakeJenkinsJobSelectionListener implements SelectionListener {

    public void widgetSelected(SelectionEvent e) {
      handleEvent();
    }

    public void widgetDefaultSelected(SelectionEvent e) {
      handleEvent();
    }

    private void handleEvent() {
      boolean selected = isMakeNewJob();
      NewJenkinsJobComposite.this.jobNameText.setEnabled(selected);
      NewJenkinsJobComposite.this.jobNameLabel.setEnabled(selected);
      validate();
    }
  }

}
