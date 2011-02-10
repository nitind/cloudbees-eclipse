package com.cloudbees.eclipse.ui.views.build;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
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

  private Form form;
  private Label textTopSummary;
  private Composite compBuildSummary;
  private Label contentBuildSummary;
  private Composite contentBuildHistory;
  private Label contentJUnitTests;
  private JenkinsJobBuildsResponse dataJobDetails;
  private Action invokeBuild;
  private Label statusIcon;
  private Composite compMain;
  private Composite healthTest;
  private Composite healthBuild;
  private Composite contentBuildHistoryHolder;
  private TreeViewer treeViewerRecentChanges;
  private Section sectBuildHistory;
  private Section sectRecentChanges;
  private Composite compInterm;
  private ScrolledComposite scrolledComposite;
  private RecentChangesContentProvider changesContentProvider;
  private Label changesetLabel;

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

    form = formToolkit.createForm(parent);
    //form.setDelayedReflow(true);
    formToolkit.decorateFormHeading(form/*.getForm()*/);
    formToolkit.paintBordersFor(form);
    form.setText("n/a");
    form.getBody().setLayout(new GridLayout(1, true));

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
    textTopSummary.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
    textTopSummary.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
    textTopSummary.setForeground(formToolkit.getColors().getColor(IFormColors.TITLE));

    Section sectSummary = formToolkit.createSection(compMain, Section.TITLE_BAR);
    GridData gd_sectSummary = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd_sectSummary.verticalIndent = 10;
    sectSummary.setLayoutData(gd_sectSummary);
    sectSummary.setSize(107, 45);
    formToolkit.paintBordersFor(sectSummary);
    sectSummary.setText("Build Summary");

    compBuildSummary = new Composite(sectSummary, SWT.NONE);
    formToolkit.adapt(compBuildSummary);
    formToolkit.paintBordersFor(compBuildSummary);
    sectSummary.setClient(compBuildSummary);
    compBuildSummary.setLayout(new GridLayout(1, false));

    contentBuildSummary = formToolkit.createLabel(compBuildSummary, "n/a", SWT.NONE);
    contentBuildSummary.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

    Section sectTests = formToolkit.createSection(compMain, Section.TITLE_BAR);
    GridData gd_sectTests = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
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

    sectBuildHistory = formToolkit.createSection(compMain, Section.TITLE_BAR);
    GridData gd_sectBuildHistory = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd_sectBuildHistory.verticalIndent = 20;
    sectBuildHistory.setLayoutData(gd_sectBuildHistory);
    sectBuildHistory.setSize(94, 55);
    formToolkit.paintBordersFor(sectBuildHistory);
    sectBuildHistory.setText("Build History");

    ScrolledComposite scrolledHistory = new ScrolledComposite(sectBuildHistory, SWT.H_SCROLL | SWT.V_SCROLL);
    contentBuildHistoryHolder = new Composite(scrolledHistory, SWT.NONE);

    scrolledHistory.setExpandHorizontal(true);
    scrolledHistory.setExpandVertical(true);

    scrolledHistory.setContent(contentBuildHistoryHolder);

    sectBuildHistory.setClient(scrolledHistory);

    formToolkit.adapt(contentBuildHistoryHolder);
    formToolkit.paintBordersFor(contentBuildHistoryHolder);
    contentBuildHistoryHolder.setLayout(new GridLayout(1, false));

    formToolkit.adapt(scrolledHistory);
    formToolkit.paintBordersFor(scrolledHistory);
    scrolledHistory.setLayout(new GridLayout(1, false));

    //contentBuildHistory = formToolkit.createHyperlink(composite_2, "n/a", SWT.NONE);

    contentBuildHistory = formToolkit.createComposite(contentBuildHistoryHolder, SWT.NONE);
    contentBuildHistory.setLayout(new GridLayout(1, false));

    sectRecentChanges = formToolkit.createSection(compMain, Section.TITLE_BAR);
    GridData gd_sectRecentChanges = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd_sectRecentChanges.verticalIndent = 20;
    sectRecentChanges.setLayoutData(gd_sectRecentChanges);

    formToolkit.paintBordersFor(sectRecentChanges);
    sectRecentChanges.setText("Changes");


    compInterm = formToolkit.createComposite(sectRecentChanges, SWT.NONE);

    GridLayout gl_compInterm = new GridLayout(1, false);
    gl_compInterm.verticalSpacing = 0;
    gl_compInterm.marginWidth = 0;
    gl_compInterm.marginHeight = 0;
    compInterm.setLayout(gl_compInterm);
    compInterm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    sectRecentChanges.setClient(compInterm);

    changesetLabel = formToolkit.createLabel(compInterm, "");

    scrolledComposite = new ScrolledComposite(compInterm, SWT.H_SCROLL | SWT.V_SCROLL);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);
    scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    formToolkit.adapt(scrolledComposite);
    formToolkit.paintBordersFor(scrolledComposite);


    treeViewerRecentChanges = new TreeViewer(scrolledComposite, SWT.BORDER);
    Tree treeRecentChanges = treeViewerRecentChanges.getTree();

    changesContentProvider = new RecentChangesContentProvider();
    treeViewerRecentChanges.setContentProvider(changesContentProvider);
    treeViewerRecentChanges.setLabelProvider(new RecentChangesLabelProvider());

    treeViewerRecentChanges.setInput("n/a");

    formToolkit.adapt(treeRecentChanges);
    formToolkit.paintBordersFor(treeRecentChanges);
    scrolledComposite.setContent(treeRecentChanges);
    scrolledComposite.setMinSize(treeRecentChanges.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    //new Label(recentChangesComp, SWT.NONE);

    //compMain.layout(true);

    //recentChangesComp.layout(true);

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
        final String jobUrl = BuildPart.this.getBuildEditorInput().getJobUrl();
        final JenkinsService ns = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(jobUrl);

        org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Building job...") {
          protected IStatus run(IProgressMonitor monitor) {
            try {
              ns.invokeBuild(jobUrl, monitor);
              return org.eclipse.core.runtime.Status.OK_STATUS;
            } catch (CloudBeesException e) {
              //CloudBeesUIPlugin.getDefault().getLogger().error(e);
              return new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.Status.ERROR,
                  CloudBeesUIPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
            }
          }
        };

        job.setUser(true);
        job.schedule();
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
      if (contentBuildHistory != null && !contentBuildHistory.isDisposed()) {
        contentBuildHistory.dispose();
      }

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

        String topStr = dataBuildDetail.result != null ? dataBuildDetail.result /*+ " ("
                                                                                + new Date(dataBuildDetail.timestamp) + ")"*/
        : "";

        if (dataBuildDetail.building) {
          topStr = "BUILDING";
        } else if (dataJobDetails.inQueue) {
          topStr = "IN QUEUE";
        } /*else {
          topStr = topStr + " " + Utils.humanReadableTime((System.currentTimeMillis() - dataBuildDetail.timestamp))
              + " ago";
          }
          */
        textTopSummary.setText(topStr);


        loadBuildSummary();

        // Load JUnit Tests
        loadUnitTests();

        loadBuildHistory();

        // Recent Changes      
        loadRecentChanges();

        invokeBuild.setEnabled(dataJobDetails.buildable);

        if ("SUCCESS".equalsIgnoreCase(dataBuildDetail.result)) {
          statusIcon.setImage(CloudBeesUIPlugin.getImage(CBImages.IMG_COLOR_16_BLUE));
        } else if ("FAILURE".equalsIgnoreCase(dataBuildDetail.result)) {
          statusIcon.setImage(CloudBeesUIPlugin.getImage(CBImages.IMG_COLOR_16_RED));
        } else {
          statusIcon.setImage(null);
        }

        //form.layout();
        //form.getBody().layout(true);
        //BuildPart.this.compMain.pack(true);
        //form.pack();
        BuildPart.this.compMain.layout(true);

        //form.reflow(true);
        form.layout(true);
        form.getBody().layout(true);
        //details..form.layout();

      }
    });
  }

  private void loadBuildHistory() {

    if (contentBuildHistory != null && !contentBuildHistory.isDisposed()) {
      contentBuildHistory.dispose();
    }

    //contentBuildHistoryHolder.layout(true);

    contentBuildHistory = formToolkit.createComposite(contentBuildHistoryHolder, SWT.NONE);
    GridLayout gl = new GridLayout(1, false);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    gl.verticalSpacing = 0;
    contentBuildHistory.setLayout(gl);
    

    if (dataJobDetails.builds == null || dataJobDetails.builds.length == 0) {
      formToolkit.createLabel(contentBuildHistory, "No recent builds.");//TODO i18n
      contentBuildHistoryHolder.layout(true);
      return;
    }

    //contentBuildHistory.setBackground(composite.getBackground());

    StringBuffer val = new StringBuffer();
    for (JenkinsJobBuildsResponse.Build b : dataJobDetails.builds) {

      //String result = b.result != null && b.result.length() > 0 ? " - " + b.result : "";

      String timeComp = (Utils.humanReadableTime((System.currentTimeMillis() - b.timestamp))) + " ago";

      Image image = null;
      if ("success".equalsIgnoreCase(b.result)) {
        image = CloudBeesUIPlugin.getImage(CBImages.IMG_COLOR_16_BLUE);
      } else if ("failure".equalsIgnoreCase(b.result)) {
        image = CloudBeesUIPlugin.getImage(CBImages.IMG_COLOR_16_RED);
      } else {
        image = CloudBeesUIPlugin.getImage(CBImages.IMG_COLOR_16_GREY);
      }

      Composite comp;
      if (b.number != dataBuildDetail.number) {
        comp = createImageLink(contentBuildHistory, "<a>#" + b.number + "</a>  " + timeComp, image, new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            if (e.text != null && e.text.startsWith("#")) {
              long buildNo = new Long(e.text.substring(1)).longValue();
              BuildPart.this.switchToBuild(buildNo);
            }
          }
        });
      } else {
        comp = createImageLabel(contentBuildHistory, "#" + b.number + "  " + timeComp, image);        
      }

      GridData gd = new GridData();
      gd.verticalIndent = 2;
      comp.setLayoutData(gd);

    }

    //BuildPart.this.compMain.layout();
    contentBuildHistoryHolder.layout(true);
    //contentBuildHistory.setText(val.toString());

  }

  private void loadBuildSummary() {
    //details.getJob().buildable;
    //details.getJob().inQueue;
    //details.getJob().healthReport;

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

    //summary.append("Buildable: " + details.getJob().buildable + "\n");
    //summary.append("Build number: " + dataBuildDetail.number + "\n");

    contentBuildSummary.setText(summary.toString());

    if (healthTest != null && !healthTest.isDisposed()) {
      healthTest.dispose();
    }

    if (healthBuild != null && !healthBuild.isDisposed()) {
      healthBuild.dispose();
    }

    //compBuildSummary.redraw();
    //compBuildSummary.layout(true);

    HealthReport[] hr = dataJobDetails.healthReport;
    if (hr != null && hr.length > 0) {
      //summary.append("\nProject Health\n");
      for (HealthReport rep : hr) {
        //summary.append(rep.description + "\n"); // + " Score:" + rep.score + "%\n"
        //System.out.println("ICON URL: " + rep.iconUrl);
        String testMatch = "Test Result: ";
        if (rep.description.startsWith(testMatch)) {
          healthTest = createImageLabel(compBuildSummary, rep.description.substring(testMatch.length()),
              CloudBeesUIPlugin.getDefault().getImage(CBImages.IMG_HEALTH_PREFIX + CBImages.IMG_24 + rep.iconUrl));
        } else {
          String buildMatch = "Build stability: ";
          if (rep.description.startsWith(buildMatch)) {
            healthBuild = createImageLabel(compBuildSummary, rep.description.substring(buildMatch.length()),
                CloudBeesUIPlugin.getDefault().getImage(CBImages.IMG_HEALTH_PREFIX + CBImages.IMG_24 + rep.iconUrl));
          }
        }
      }

    }

    compBuildSummary.layout(true);

  }

  private Composite createImageLabel(Composite parent, String text, Image image) {
    Composite comp = formToolkit.createComposite(parent);
    GridLayout gl = new GridLayout(2, false);
    comp.setLayout(gl);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    gl.verticalSpacing = 0;
    Label imgLabel = formToolkit.createLabel(comp, "", SWT.NONE);
    imgLabel.setImage(image);
    Label label = formToolkit.createLabel(comp, text, SWT.NONE);
    return comp;
  }

  private Composite createImageLink(Composite parent, String text, Image image, SelectionListener selectionListener) {
    Composite comp = formToolkit.createComposite(parent);
    GridLayout gl = new GridLayout(2, false);
    comp.setLayout(gl);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    gl.verticalSpacing = 0;

    Label imgLabel = formToolkit.createLabel(comp, "", SWT.NONE);
    imgLabel.setImage(image);
    Link link = new Link(comp, SWT.NONE);
    link.setText(text);
    link.addSelectionListener(selectionListener);
    link.setBackground(formToolkit.getColors().getBackground());
    return comp;
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

    //StringBuffer changes = new StringBuffer();
    //Point origSize = treeViewerRecentChanges.getTree().getSize();
    if (dataBuildDetail.changeSet != null && dataBuildDetail.changeSet.items != null
        && dataBuildDetail.changeSet.items.length > 0) {
      //changesContentProvider.setModel(dataBuildDetail.changeSet.items);
      treeViewerRecentChanges.setInput(dataBuildDetail.changeSet.items);
      changesetLabel.setVisible(false);
      treeViewerRecentChanges.getTree().setVisible(true);
      treeViewerRecentChanges.refresh();
      //createChangeTreeViewer(dataBuildDetail.changeSet.items);
    } else {
      //changesContentProvider.setModel(new ChangeSetItem[0]);
      treeViewerRecentChanges.setInput(new ChangeSetItem[0]);
      changesetLabel.setVisible(true);
      changesetLabel.setText("No changes");
      treeViewerRecentChanges.getTree().setVisible(false);
      //createChangeTreeViewer(new ChangeSetItem[0]);
    }



    //treeViewerRecentChanges.refresh();
    //treeViewerRecentChanges.getTree().setSize(origSize);

    //treeViewerRecentChanges.setAutoExpandLevel(2);
    /*    if (changes.length() == 0) {
          changes.append("none");
        }
    */   

    //recentChangesComp.layout(true);
    //recentChangesScroll.setMinSize(recentChangesComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    //recentChangesComp.setSize(recentChangesComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    //recentChangesScroll.setMinSize(sectRecentChanges.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    

    //contentRecentChanges.setText(changes.toString());

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
