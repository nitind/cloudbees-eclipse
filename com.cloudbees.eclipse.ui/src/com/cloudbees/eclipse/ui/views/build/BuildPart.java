package com.cloudbees.eclipse.ui.views.build;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Tree;
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
import com.cloudbees.eclipse.core.jenkins.api.HealthReport;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Action.Cause;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobBuildsResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class BuildPart extends EditorPart {

  public static final String ID = "com.cloudbees.eclipse.ui.views.build.BuildPart"; //$NON-NLS-1$
  private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

  private JenkinsBuildDetailsResponse dataBuildDetail;

  private ScrolledForm form;
  private Label textTopSummary;
  private Composite compBuildSummary;
  private Label contentBuildSummary;
  private Link contentBuildHistory;
  private Label contentJUnitTests;
  private Label contentRecentChanges;
  private JenkinsJobBuildsResponse dataJobDetails;
  private Action invokeBuild;
  private Label statusIcon;
  private Composite compMain;

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

    compMain = new Composite(form.getBody(), SWT.NONE);
    GridLayout gl_compMain = new GridLayout(2, true);
    compMain.setLayout(gl_compMain);
    compMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    formToolkit.adapt(compMain);
    formToolkit.paintBordersFor(compMain);

    Composite compStatusHead = new Composite(compMain, SWT.NONE);
    compStatusHead.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    GridLayout rl_compStatusHead = new GridLayout();
    rl_compStatusHead.marginHeight = 0;
    rl_compStatusHead.marginWidth = 0;
    rl_compStatusHead.numColumns = 2;

    compStatusHead.setLayout(rl_compStatusHead);
    formToolkit.adapt(compStatusHead);
    formToolkit.paintBordersFor(compStatusHead);

    statusIcon = formToolkit.createLabel(compStatusHead, "", SWT.NONE);

    textTopSummary = formToolkit.createLabel(compStatusHead, "n/a", SWT.BOLD);
    textTopSummary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Section sectSummary = formToolkit.createSection(compMain, Section.TITLE_BAR);
    GridData gd_sectSummary = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
    gd_sectSummary.verticalIndent = 10;
    sectSummary.setLayoutData(gd_sectSummary);
    sectSummary.setSize(107, 45);
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

    Section sectTests = formToolkit.createSection(compMain, Section.TITLE_BAR);
    GridData gd_sectTests = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
    gd_sectTests.verticalIndent = 10;
    sectTests.setLayoutData(gd_sectTests);
    sectTests.setSize(80, 45);
    formToolkit.paintBordersFor(sectTests);
    sectTests.setText("JUnit Tests");

    Composite compTests = new Composite(sectTests, SWT.NONE);
    formToolkit.adapt(compTests);
    formToolkit.paintBordersFor(compTests);
    sectTests.setClient(compTests);
    compTests.setLayout(new GridLayout(1, false));

    contentJUnitTests = formToolkit.createLabel(compTests, "n/a", SWT.NONE);
    contentJUnitTests.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

    Section sectBuildHistory = formToolkit.createSection(compMain, Section.TITLE_BAR);
    GridData gd_sectBuildHistory = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
    gd_sectBuildHistory.verticalIndent = 30;
    sectBuildHistory.setLayoutData(gd_sectBuildHistory);
    sectBuildHistory.setSize(94, 55);
    formToolkit.paintBordersFor(sectBuildHistory);
    sectBuildHistory.setText("Build History");

    Composite composite_2 = new Composite(sectBuildHistory, SWT.NONE);
    formToolkit.adapt(composite_2);
    formToolkit.paintBordersFor(composite_2);
    sectBuildHistory.setClient(composite_2);
    GridLayout gl_composite_2 = new GridLayout(1, false);
    gl_composite_2.verticalSpacing = 0;
    gl_composite_2.horizontalSpacing = 0;
    gl_composite_2.marginHeight = 0;
    gl_composite_2.marginWidth = 0;
    composite_2.setLayout(gl_composite_2);

    ScrolledComposite scrolledComposite = new ScrolledComposite(composite_2, SWT.H_SCROLL | SWT.V_SCROLL);
    scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    formToolkit.adapt(scrolledComposite);
    formToolkit.paintBordersFor(scrolledComposite);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);

    Composite composite = new Composite(scrolledComposite, SWT.NONE);
    formToolkit.adapt(composite);
    formToolkit.paintBordersFor(composite);
    GridLayout gl_composite = new GridLayout(1, false);
    gl_composite.horizontalSpacing = 0;
    gl_composite.marginHeight = 0;
    gl_composite.marginWidth = 0;
    gl_composite.verticalSpacing = 0;
    composite.setLayout(gl_composite);

    //contentBuildHistory = formToolkit.createHyperlink(composite_2, "n/a", SWT.NONE);
    contentBuildHistory = new Link(composite, SWT.NO_FOCUS);
    GridData gd_contentBuildHistory = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
    gd_contentBuildHistory.verticalIndent = 5;
    gd_contentBuildHistory.horizontalIndent = 5;
    contentBuildHistory.setLayoutData(gd_contentBuildHistory);
    contentBuildHistory.setText("n/a");
    contentBuildHistory.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (e.text != null && e.text.startsWith("#")) {
          long buildNo = new Long(e.text.substring(1)).longValue();
          BuildPart.this.switchToBuild(buildNo);
        }
      }
    });
    contentBuildHistory.setBackground(composite.getBackground());

    scrolledComposite.setContent(composite);
    scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    Section sectRecentChanges = formToolkit.createSection(compMain, Section.TITLE_BAR);
    GridData gd_sectRecentChanges = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
    gd_sectRecentChanges.verticalIndent = 30;
    sectRecentChanges.setLayoutData(gd_sectRecentChanges);
    sectRecentChanges.setSize(68, 45);
    formToolkit.paintBordersFor(sectRecentChanges);
    sectRecentChanges.setText("Changes");

    Composite composite_3 = new Composite(sectRecentChanges, SWT.NONE);
    formToolkit.adapt(composite_3);
    formToolkit.paintBordersFor(composite_3);
    sectRecentChanges.setClient(composite_3);
    GridLayout gl_composite_3 = new GridLayout(1, false);
    gl_composite_3.horizontalSpacing = 0;
    gl_composite_3.verticalSpacing = 0;
    composite_3.setLayout(gl_composite_3);

    contentRecentChanges = formToolkit.createLabel(composite_3, "n/a", SWT.NONE);
    contentRecentChanges.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    TreeViewer treeViewerRecentChanges = new TreeViewer(composite_3, SWT.NONE);
    Tree treeRecentChanges = treeViewerRecentChanges.getTree();
    treeRecentChanges.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    formToolkit.paintBordersFor(treeRecentChanges);

    createActions();

    loadInitialData();
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
        String jobUrl = BuildPart.this.getBuildEditorInput().getJobUrl();
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

  protected BuildEditorInput getBuildEditorInput() {
    return (BuildEditorInput) getEditorInput();
  }

  protected void reloadData() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        try {
          BuildEditorInput details = (BuildEditorInput) getEditorInput();
          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(
              getBuildEditorInput().getJobUrl());
          dataBuildDetail = service.getJobDetails(details.getBuildUrl(), monitor);
          dataJobDetails = service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor);
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
    CloudBeesUIPlugin.getDefault().openWithBrowser(getBuildEditorInput().getJobUrl());
  }

  protected void switchToBuild(final long buildNo) {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        BuildEditorInput details = (BuildEditorInput) getEditorInput();
        String newJobUrl = getBuildEditorInput().getJobUrl() + "/" + buildNo + "/";

        JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(
            getBuildEditorInput().getJobUrl());

        try {
          dataBuildDetail = service.getJobDetails(newJobUrl, monitor);
          details.setBuildUrl(dataBuildDetail.url);
          dataJobDetails = service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor);

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

  private void loadInitialData() {
    if (getEditorInput() == null) {
      return;
    }

    final BuildEditorInput details = (BuildEditorInput) getEditorInput();

    if (details == null || details.getBuildUrl() == null || !getBuildEditorInput().isLastBuildAvailable()) {
      // No last build available
      contentBuildHistory.setText("No data available.");
      contentJUnitTests.setText("No data available.");
      textTopSummary.setText("Latest build not available.");
      form.setText(getBuildEditorInput().getDisplayName());
      setPartName(getBuildEditorInput().getDisplayName());

    } else {

      IRunnableWithProgress op = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(
              getBuildEditorInput().getBuildUrl());

          try {

            dataBuildDetail = service.getJobDetails(getBuildEditorInput().getBuildUrl(), monitor);

            dataJobDetails = service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor);

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
        if (dataBuildDetail != null) {
          setPartName(details.getDisplayName() + " #" + dataBuildDetail.number);
        } else {
          setPartName(details.getDisplayName());
        }

        //setContentDescription(detail.fullDisplayName);

        if (form != null) {

          if (dataBuildDetail != null) {
            form.setText("Build #" + dataBuildDetail.number + " [" + details.getDisplayName() + "]");
          } else {
            form.setText(details.getDisplayName());
          }

        }

        String topStr = dataBuildDetail.result != null ? dataBuildDetail.result + " ("
            + new Date(dataBuildDetail.timestamp) + ")" : "";

        if (dataBuildDetail.building) {
          topStr = "BUILDING";
        } else if (dataJobDetails.inQueue) {
          topStr = "IN QUEUE";
        }

        textTopSummary.setText(topStr);

        // Recent Changes      
        loadRecentChanges();

        // Load JUnit Tests
        loadUnitTests();

        loadBuildSummary();

        loadBuildHistory();

        invokeBuild.setEnabled(dataJobDetails.buildable);

        BuildPart.this.compMain.layout();

      }
    });
  }

  private void loadBuildHistory() {
    if (dataJobDetails.builds == null || dataJobDetails.builds.length == 0) {
      contentBuildHistory.setText("No recent builds.");//TODO i18n
      return;
    }

    StringBuffer val = new StringBuffer();
    for (JenkinsJobBuildsResponse.Build b : dataJobDetails.builds) {

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

    StringBuffer causeBuffer = new StringBuffer();
    if (dataBuildDetail.actions != null && dataBuildDetail.actions.length > 0) {
      for (int i = 0; i < dataBuildDetail.actions.length; i++) {
        com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Action action = dataBuildDetail.actions[i];
        if (action.causes != null) {
          for (int c = 0; c < action.causes.length; c++) {
            Cause cause = action.causes[c];
            causeBuffer.append(cause.shortDescription + "\n");
            break; // For now let's just show the first cause
          }
        }
      }
    }
    if (causeBuffer.length() > 0) {
      summary.append(causeBuffer.toString() + "\n");
    }

    if (dataBuildDetail.builtOn != null && dataBuildDetail.timestamp != null) {
      if (dataBuildDetail.builtOn != null && dataBuildDetail.builtOn.length() > 0) {
        summary.append("Built on: " + dataBuildDetail.builtOn + " at " + (new Date(dataBuildDetail.timestamp)) + "\n");
      } else {
        summary.append("Built at " + (new Date(dataBuildDetail.timestamp)) + "\n");
      }
    }

    summary.append("\n");

    //summary.append("Buildable: " + details.getJob().buildable + "\n");
    //summary.append("Build number: " + dataBuildDetail.number + "\n");

    HealthReport[] hr = dataJobDetails.healthReport;
    if (hr != null && hr.length > 0) {
      //summary.append("\nProject Health\n");
      for (HealthReport rep : hr) {
        summary.append(rep.description + "\n"); // + " Score:" + rep.score + "%\n"
        System.out.println("ICON URL: " + rep.iconUrl);
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
