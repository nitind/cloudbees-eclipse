package com.cloudbees.eclipse.ui.views.build;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.IProgressService;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job.HealthReport;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class BuildPart extends EditorPart {

  public static final String ID = "com.cloudbees.eclipse.ui.views.build.BuildPart"; //$NON-NLS-1$
  private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

  private JenkinsBuildDetailsResponse dataBuildDetail;

  private boolean lastBuildAvailable = false;
  private ScrolledForm form;
  private Label textTopSummary;
  private Composite compBuildSummary;
  private Label contentBuildSummary;
  private Link contentBuildHistory;
  private Label contentJUnitTests;
  private Label contentRecentChanges;
  private JenkinsJobBuildsResponse dataJobBuilds;
  private Action invokeBuild;

  public BuildPart() {
    super();
  }

  /**
   * Create contents of the editor part.
   * 
   * @param parent
   */
  @Override
  public void createPartControl(Composite parent) {

    form = formToolkit.createScrolledForm(parent);
    formToolkit.decorateFormHeading(form.getForm());
    formToolkit.paintBordersFor(form);
    form.setText("n/a");
    form.getBody().setLayout(new GridLayout(1, false));

    Composite compStatusHead = new Composite(form.getBody(), SWT.NONE);
    RowLayout rl_compStatusHead = new RowLayout(SWT.HORIZONTAL);
    rl_compStatusHead.fill = true;
    compStatusHead.setLayout(rl_compStatusHead);
    compStatusHead.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    formToolkit.adapt(compStatusHead);
    formToolkit.paintBordersFor(compStatusHead);

    Label statusIcon = formToolkit.createLabel(compStatusHead, "", SWT.NONE);

    textTopSummary = formToolkit.createLabel(compStatusHead, "n/a", SWT.BOLD);

    Composite compUpper = formToolkit.createComposite(form.getBody(), SWT.NONE);
    compUpper.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    formToolkit.paintBordersFor(compUpper);
    compUpper.setLayout(new GridLayout(2, true));

    Section sectSummary = formToolkit.createSection(compUpper, Section.TITLE_BAR);
    sectSummary.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    formToolkit.paintBordersFor(sectSummary);
    sectSummary.setText("Build Summary");

    compBuildSummary = new Composite(sectSummary, SWT.NONE);
    formToolkit.adapt(compBuildSummary);
    formToolkit.paintBordersFor(compBuildSummary);
    sectSummary.setClient(compBuildSummary);
    ColumnLayout cl_composite_1 = new ColumnLayout();
    cl_composite_1.maxNumColumns = 1;
    compBuildSummary.setLayout(cl_composite_1);

    contentBuildSummary = formToolkit.createLabel(compBuildSummary, "n/a", SWT.NONE);

    Section sectTests = formToolkit.createSection(compUpper, Section.TITLE_BAR);
    sectTests.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    formToolkit.paintBordersFor(sectTests);
    sectTests.setText("JUnit Tests");

    Composite compTests = new Composite(sectTests, SWT.NONE);
    formToolkit.adapt(compTests);
    formToolkit.paintBordersFor(compTests);
    sectTests.setClient(compTests);
    ColumnLayout cl_composite_4 = new ColumnLayout();
    cl_composite_4.maxNumColumns = 1;
    compTests.setLayout(cl_composite_4);

    contentJUnitTests = formToolkit.createLabel(compTests, "n/a", SWT.NONE);

    Composite compLower = formToolkit.createComposite(form.getBody(), SWT.NONE);
    compLower.setLayout(new GridLayout(2, true));
    compLower.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    formToolkit.paintBordersFor(compLower);

    Section sectBuildHistory = formToolkit.createSection(compLower, Section.TITLE_BAR);
    sectBuildHistory.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    formToolkit.paintBordersFor(sectBuildHistory);
    sectBuildHistory.setText("Build History");

    Composite composite_2 = new Composite(sectBuildHistory, SWT.NONE);
    formToolkit.adapt(composite_2);
    formToolkit.paintBordersFor(composite_2);
    sectBuildHistory.setClient(composite_2);
    composite_2.setLayout(new GridLayout(1, false));

    Composite composite = new Composite(composite_2, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
    formToolkit.adapt(composite);
    formToolkit.paintBordersFor(composite);
    composite.setLayout(new GridLayout(1, false));

    //contentBuildHistory = formToolkit.createHyperlink(composite_2, "n/a", SWT.NONE);
    contentBuildHistory = new Link(composite, SWT.NO_FOCUS);
    contentBuildHistory.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
    contentBuildHistory.setText("n/a");
    contentBuildHistory.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (e.text != null && e.text.startsWith("#")) {
          long buildNo = new Long(e.text.substring(1)).longValue();
          BuildPart.this.switchToBuild(buildNo);
        }
      }
    });

    Section sectRecentChanges = formToolkit.createSection(compLower, Section.TITLE_BAR);
    sectRecentChanges.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    formToolkit.paintBordersFor(sectRecentChanges);
    sectRecentChanges.setText("Changes");

    Composite composite_3 = new Composite(sectRecentChanges, SWT.NONE);
    formToolkit.adapt(composite_3);
    formToolkit.paintBordersFor(composite_3);
    sectRecentChanges.setClient(composite_3);
    ColumnLayout cl_composite_3 = new ColumnLayout();
    cl_composite_3.maxNumColumns = 1;
    composite_3.setLayout(cl_composite_3);

    contentRecentChanges = formToolkit.createLabel(composite_3, "n/a", SWT.NONE);

    createActions();

    loadData();
  }

  private void createActions() {

    Action reload = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      public void run() {
        BuildPart.this.reloadData();
      }
    };
    reload.setToolTipText("Reload"); //TODO i18n
    reload.setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.IMG_REFRESH));

    Action openInWeb = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      public void run() {
        BuildPart.this.openBuildWithBrowser();
      }
    };
    openInWeb.setToolTipText("Open with Browser"); //TODO i18n
    openInWeb.setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.IMG_BROWSER));

    Action openLogs = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      public void run() {
        if (dataBuildDetail != null && dataBuildDetail.url != null) {
          CloudBeesUIPlugin.getDefault().openWithBrowser(dataBuildDetail.url + "/consoleText");
          return;
        }

      }
    };
    openLogs.setToolTipText("Open build log"); //TODO i18n
    openLogs.setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.IMG_CONSOLE));

    invokeBuild = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      public void run() {
        BuildEditorInput details = (BuildEditorInput) getEditorInput();
        String jobUrl = details.getJob().url;
        JenkinsService ns = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(jobUrl);

        try {
          ns.invokeBuild(jobUrl, new NullProgressMonitor());
        } catch (CloudBeesException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
        }

      }
    };
    invokeBuild.setToolTipText("Run a new build for this job"); //TODO i18n
    invokeBuild.setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.IMG_RUN));

    form.getToolBarManager().add(reload);
    form.getToolBarManager().add(new Separator());
    form.getToolBarManager().add(invokeBuild);
    form.getToolBarManager().add(new Separator());
    form.getToolBarManager().add(openLogs);
    form.getToolBarManager().add(openInWeb);

    form.getToolBarManager().update(false);
  }

  protected void reloadData() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        try {
          BuildEditorInput details = (BuildEditorInput) getEditorInput();
          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(details.getJob().url);
          dataBuildDetail = service.getJobDetails(details.getBuildUrl(), monitor);
        } catch (CloudBeesException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
        }

        reloadUI();
      }
    };

    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    try {
      service.run(false, true, op);
    } catch (InvocationTargetException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
    } catch (InterruptedException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
    }
  }

  protected void openBuildWithBrowser() {
    if (dataBuildDetail != null && dataBuildDetail.url != null) {
      CloudBeesUIPlugin.getDefault().openWithBrowser(dataBuildDetail.url);
      return;
    }

    // for some reason build details not available (for example, no build was available). fall back to job url
    BuildEditorInput details = (BuildEditorInput) getEditorInput();
    CloudBeesUIPlugin.getDefault().openWithBrowser(details.getJob().url);
  }

  protected void switchToBuild(final long buildNo) {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        BuildEditorInput details = (BuildEditorInput) getEditorInput();
        String newJobUrl = details.getJob().url + "/" + buildNo + "/";

        JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(details.getJob().url);

        try {
          dataBuildDetail = service.getJobDetails(newJobUrl, monitor);
          details.setBuildUrl(dataBuildDetail.url);

        } catch (CloudBeesException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
        }

        reloadUI();

      }
    };

    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    try {
      service.run(false, true, op);
    } catch (InvocationTargetException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
    } catch (InterruptedException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
    }
  }

  public IEditorSite getEditorSite() {
    return (IEditorSite) getSite();
  }

  private void loadData() {
    if (getEditorInput() == null) {
      return;
    }

    IProgressMonitor monitor = new NullProgressMonitor();

    final BuildEditorInput details = (BuildEditorInput) getEditorInput();

    this.lastBuildAvailable = false;

    if (details == null || details.getLastBuild() == null || details.getLastBuild().url == null) {
      // No last build available
      contentBuildHistory.setText("No data available.");
      contentJUnitTests.setText("No data available.");
    } else {

      IRunnableWithProgress op = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(details.getLastBuild().url);

          try {

            dataBuildDetail = service.getJobDetails(details.getLastBuild().url, monitor);

            dataJobBuilds = service.getJobBuilds(details.getJob().url, monitor);

            BuildPart.this.lastBuildAvailable = true;
          } catch (CloudBeesException e) {
            CloudBeesUIPlugin.getDefault().getLogger().error(e);
            return;
          }

          reloadUI();

        }
      };

      IProgressService service = PlatformUI.getWorkbench().getProgressService();
      try {
        service.run(false, true, op);
      } catch (InvocationTargetException e) {
        CloudBeesUIPlugin.getDefault().getLogger().error(e);
      } catch (InterruptedException e) {
        CloudBeesUIPlugin.getDefault().getLogger().error(e);
      }
    }
  }

  protected void reloadUI() {
    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
      public void run() {

        BuildEditorInput details = (BuildEditorInput) getEditorInput();

        //setPartName();
        setPartName(details.getJob().displayName + " #" + dataBuildDetail.number);

        //setContentDescription(detail.fullDisplayName);

        String topStr = dataBuildDetail.result != null ? dataBuildDetail.result + " ("
            + new Date(dataBuildDetail.timestamp) + ")" : "";

        textTopSummary.setText(topStr);

        if (form != null) {
          form.setText("Build #" + dataBuildDetail.number + " [" + details.getJob().displayName + "]");
          //TODO Add image for build status! form.setImage(image);
        }

        // Recent Changes      
        loadRecentChanges();

        // Load JUnit Tests
        loadUnitTests();

        loadBuildSummary();

        loadBuildHistory();

        invokeBuild.setEnabled(details.getJob().buildable);
      }
    });
  }

  private void loadBuildHistory() {
    if (dataJobBuilds.builds == null || dataJobBuilds.builds.length == 0) {
      contentBuildHistory.setText("No recent builds.");//TODO i18n
      return;
    }

    StringBuffer val = new StringBuffer();
    for (JenkinsJobBuildsResponse.Build b : dataJobBuilds.builds) {

      String result = b.result != null && b.result.length() > 0 ? " - " + b.result : "";

      String timeComp = (Utils.humanReadableTime((System.currentTimeMillis() - b.timestamp))) + " ago";

      if (b.number != dataBuildDetail.number) {
        val.append("<a>#" + b.number + "</a>    " + timeComp + result.toLowerCase() + "\n");
      } else {
        val.append("#" + b.number + "    " + timeComp + result.toLowerCase() + " \n");
      }
    }

    contentBuildHistory.setText(val.toString());

  }

  private void loadBuildSummary() {
    //details.getJob().buildable;
    //details.getJob().inQueue;
    //details.getJob().healthReport;

    BuildEditorInput details = (BuildEditorInput) getEditorInput();

    StringBuffer summary = new StringBuffer();
    if (dataBuildDetail.description != null) {
      summary.append(dataBuildDetail.description + "\n");
    }

    if (dataBuildDetail.builtOn != null && dataBuildDetail.timestamp != null) {
      if (dataBuildDetail.builtOn != null && dataBuildDetail.builtOn.length() > 0) {
        summary.append("Built on: " + dataBuildDetail.builtOn + " at " + (new Date(dataBuildDetail.timestamp)) + "\n");
      } else {
        summary.append("Built at " + (new Date(dataBuildDetail.timestamp)) + "\n");
      }
    }

    summary.append("\n");

    summary.append("Building: " + dataBuildDetail.building + "\n");
    //summary.append("Buildable: " + details.getJob().buildable + "\n");
    summary.append("Build number: " + dataBuildDetail.number + "\n");

    HealthReport[] hr = details.getJob().healthReport;
    if (hr != null && hr.length > 0) {
      summary.append("\nProject Health\n");
      for (HealthReport rep : hr) {
        summary.append("    " + rep.description + " Score:" + rep.score + "%\n");
      }

    }

    contentBuildSummary.setText(summary.toString());
  }

  private void loadUnitTests() {

    if (dataBuildDetail.actions == null) {
      contentJUnitTests.setText("No Tests");
      return;
    }

    for (com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Action action : dataBuildDetail.actions) {
      if ("testReport".equalsIgnoreCase(action.urlName)) {
        String val = "Total: " + action.totalCount + " Failed: " + action.failCount + " Skipped: " + action.skipCount;
        contentJUnitTests.setText(val);
        return;
      }
    }

    contentJUnitTests.setText("No Tests");

  }

  private void loadRecentChanges() {
    StringBuffer changes = new StringBuffer();
    if (dataBuildDetail.changeSet != null && dataBuildDetail.changeSet.items != null) {
      for (ChangeSetItem item : dataBuildDetail.changeSet.items) {
        String authinfo = item.author != null && item.author.fullName != null ? " by " + item.author.fullName : "";
        String line = "rev" + item.rev + ": '" + item.msg + "' " + authinfo + "\n";
        changes.append(line);
      }
    }
    if (changes.length() == 0) {
      changes.append("none");
    }
    contentRecentChanges.setText(changes.toString());
  }

  @Override
  public void setFocus() {
    // Set the focus
    form.setFocus();
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
    // Do the Save operation
  }

  @Override
  public void doSaveAs() {
    // Do the Save As operation
  }

  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {

    // Initialize the editor part
    setSite(site);
    setInput(input);

  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void dispose() {
    form.dispose();
    form = null;
    super.dispose();
  }
}
