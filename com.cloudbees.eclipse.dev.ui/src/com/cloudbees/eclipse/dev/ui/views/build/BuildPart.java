package com.cloudbees.eclipse.dev.ui.views.build;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
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
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.internal.action.OpenJunitViewAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class BuildPart extends EditorPart {

  public static final String ID = "com.cloudbees.eclipse.dev.ui.views.build.BuildPart"; //$NON-NLS-1$
  private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

  private JenkinsBuildDetailsResponse dataBuildDetail;
  protected JenkinsJobAndBuildsResponse dataJobDetails;

  private Form form;
  private Label textTopSummary;
  private Composite compBuildSummary;
  private Label contentBuildSummary;
  private Composite contentBuildHistory;
  private Label contentJUnitTests;
  private OpenJunitViewAction openJunitAction;
  private Composite testsLink;
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
  public void createPartControl(final Composite parent) {

    this.form = this.formToolkit.createForm(parent);
    //form.setDelayedReflow(true);
    this.formToolkit.decorateFormHeading(this.form/*.getForm()*/);
    this.formToolkit.paintBordersFor(this.form);
    this.form.setText("n/a");
    this.form.getBody().setLayout(new GridLayout(1, true));

    this.compMain = new Composite(this.form.getBody(), SWT.NONE);
    GridLayout gl_compMain = new GridLayout(2, true);
    this.compMain.setLayout(gl_compMain);
    this.compMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.formToolkit.adapt(this.compMain);
    this.formToolkit.paintBordersFor(this.compMain);

    Composite compStatusHead = new Composite(this.compMain, SWT.NONE);
    compStatusHead.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    GridLayout rl_compStatusHead = new GridLayout();
    rl_compStatusHead.marginHeight = 0;
    rl_compStatusHead.marginWidth = 0;
    rl_compStatusHead.numColumns = 2;

    compStatusHead.setLayout(rl_compStatusHead);
    this.formToolkit.adapt(compStatusHead);
    this.formToolkit.paintBordersFor(compStatusHead);

    this.statusIcon = this.formToolkit.createLabel(compStatusHead, "", SWT.NONE);

    this.textTopSummary = this.formToolkit.createLabel(compStatusHead, "n/a", SWT.BOLD);
    this.textTopSummary.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
    this.textTopSummary.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
    this.textTopSummary.setForeground(this.formToolkit.getColors().getColor(IFormColors.TITLE));

    Section sectSummary = this.formToolkit.createSection(this.compMain, Section.TITLE_BAR);
    GridData gd_sectSummary = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd_sectSummary.verticalIndent = 10;
    sectSummary.setLayoutData(gd_sectSummary);
    sectSummary.setSize(107, 45);
    this.formToolkit.paintBordersFor(sectSummary);
    sectSummary.setText("Build Summary");

    this.compBuildSummary = new Composite(sectSummary, SWT.NONE);
    this.formToolkit.adapt(this.compBuildSummary);
    this.formToolkit.paintBordersFor(this.compBuildSummary);
    sectSummary.setClient(this.compBuildSummary);
    this.compBuildSummary.setLayout(new GridLayout(1, false));

    this.contentBuildSummary = this.formToolkit.createLabel(this.compBuildSummary, "n/a", SWT.NONE);
    this.contentBuildSummary.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

    Section sectTests = this.formToolkit.createSection(this.compMain, Section.TITLE_BAR);
    GridData gd_sectTests = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd_sectTests.verticalIndent = 10;
    sectTests.setLayoutData(gd_sectTests);
    sectTests.setSize(80, 45);
    this.formToolkit.paintBordersFor(sectTests);
    sectTests.setText("JUnit Tests");

    Composite compTests = new Composite(sectTests, SWT.NONE);
    this.formToolkit.adapt(compTests);
    this.formToolkit.paintBordersFor(compTests);
    sectTests.setClient(compTests);
    compTests.setLayout(new GridLayout(1, false));

    this.contentJUnitTests = this.formToolkit.createLabel(compTests, "n/a", SWT.NONE);
    this.contentJUnitTests.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

    this.openJunitAction = new OpenJunitViewAction();
    this.testsLink = new Composite(compTests, SWT.NONE);
    this.formToolkit.adapt(this.testsLink);
    this.testsLink.setLayout(new GridLayout(2, false));
    Label label = this.formToolkit.createLabel(this.testsLink, "");
    label.setImage(CloudBeesDevUiPlugin.getImage(CBImages.IMG_JUNIT));
    Link link = new Link(this.testsLink, SWT.FLAT);
    link.setText("Show tests results in <a>JUnit View</a>.");
    link.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        BuildPart.this.openJunitAction.run();
      }
    });
    this.formToolkit.adapt(link, false, false);
    this.testsLink.setVisible(false);

    this.sectBuildHistory = this.formToolkit.createSection(this.compMain, Section.TITLE_BAR);
    GridData gd_sectBuildHistory = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd_sectBuildHistory.verticalIndent = 20;
    this.sectBuildHistory.setLayoutData(gd_sectBuildHistory);
    this.sectBuildHistory.setSize(94, 55);
    this.formToolkit.paintBordersFor(this.sectBuildHistory);
    this.sectBuildHistory.setText("Build History");

    ScrolledComposite scrolledHistory = new ScrolledComposite(this.sectBuildHistory, SWT.H_SCROLL | SWT.V_SCROLL);
    this.contentBuildHistoryHolder = new Composite(scrolledHistory, SWT.NONE);

    scrolledHistory.setExpandHorizontal(true);
    scrolledHistory.setExpandVertical(true);

    scrolledHistory.setContent(this.contentBuildHistoryHolder);

    this.sectBuildHistory.setClient(scrolledHistory);

    this.formToolkit.adapt(this.contentBuildHistoryHolder);
    this.formToolkit.paintBordersFor(this.contentBuildHistoryHolder);
    this.contentBuildHistoryHolder.setLayout(new GridLayout(1, false));

    this.formToolkit.adapt(scrolledHistory);
    this.formToolkit.paintBordersFor(scrolledHistory);
    scrolledHistory.setLayout(new GridLayout(1, false));

    //contentBuildHistory = formToolkit.createHyperlink(composite_2, "n/a", SWT.NONE);

    this.contentBuildHistory = this.formToolkit.createComposite(this.contentBuildHistoryHolder, SWT.NONE);
    this.contentBuildHistory.setLayout(new GridLayout(1, false));

    this.sectRecentChanges = this.formToolkit.createSection(this.compMain, Section.TITLE_BAR);
    GridData gd_sectRecentChanges = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd_sectRecentChanges.verticalIndent = 20;
    this.sectRecentChanges.setLayoutData(gd_sectRecentChanges);

    this.formToolkit.paintBordersFor(this.sectRecentChanges);
    this.sectRecentChanges.setText("Changes");


    this.compInterm = this.formToolkit.createComposite(this.sectRecentChanges, SWT.NONE);

    GridLayout gl_compInterm = new GridLayout(1, false);
    gl_compInterm.verticalSpacing = 0;
    gl_compInterm.marginWidth = 0;
    gl_compInterm.marginHeight = 0;
    this.compInterm.setLayout(gl_compInterm);
    this.compInterm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    this.sectRecentChanges.setClient(this.compInterm);

    this.changesetLabel = this.formToolkit.createLabel(this.compInterm, "");

    this.scrolledComposite = new ScrolledComposite(this.compInterm, SWT.H_SCROLL | SWT.V_SCROLL);
    this.scrolledComposite.setExpandHorizontal(true);
    this.scrolledComposite.setExpandVertical(true);
    this.scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.formToolkit.adapt(this.scrolledComposite);
    this.formToolkit.paintBordersFor(this.scrolledComposite);


    this.treeViewerRecentChanges = new TreeViewer(this.scrolledComposite, SWT.BORDER);
    Tree treeRecentChanges = this.treeViewerRecentChanges.getTree();

    this.changesContentProvider = new RecentChangesContentProvider();
    this.treeViewerRecentChanges.setContentProvider(this.changesContentProvider);
    this.treeViewerRecentChanges.setLabelProvider(new RecentChangesLabelProvider());
    this.treeViewerRecentChanges.addDoubleClickListener(new RecentChangesClickListener());

    this.treeViewerRecentChanges.setInput("n/a");

    this.formToolkit.adapt(treeRecentChanges);
    this.formToolkit.paintBordersFor(treeRecentChanges);
    this.scrolledComposite.setContent(treeRecentChanges);
    this.scrolledComposite.setMinSize(treeRecentChanges.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    //new Label(recentChangesComp, SWT.NONE);

    //compMain.layout(true);

    //recentChangesComp.layout(true);

    createActions();

    loadInitialData();
  }


  private void createActions() {

    Action reload = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        BuildPart.this.reloadData();
      }
    };
    reload.setToolTipText("Reload"); //TODO i18n
    reload.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_REFRESH));

    Action openInWeb = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        BuildPart.this.openBuildWithBrowser();
      }
    };
    openInWeb.setToolTipText("Open with Browser"); //TODO i18n
    openInWeb.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BROWSER));

    Action openLogs = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        if (BuildPart.this.dataBuildDetail != null && BuildPart.this.dataBuildDetail.url != null) {
          CloudBeesUIPlugin.getDefault().openWithBrowser(BuildPart.this.dataBuildDetail.url + "/consoleText");
          return;
        }

      }
    };
    openLogs.setToolTipText("Open build log"); //TODO i18n
    openLogs.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_CONSOLE));

    this.invokeBuild = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        try {
          final String jobUrl = BuildPart.this.getBuildEditorInput().getJobUrl();
          final JenkinsService ns = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(jobUrl);
          final Map<String, String> props = CloudBeesUIPlugin.getDefault().getJobPropValues(
              BuildPart.this.dataJobDetails.property);
          org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Building job...") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
              try {
                ns.invokeBuild(jobUrl, props, monitor);
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
        } catch (CancellationException e) {
          // cancelled by user
        }
      }
    };
    this.invokeBuild.setToolTipText("Run a new build for this job"); //TODO i18n
    this.invokeBuild.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_RUN));

    this.form.getToolBarManager().add(reload);
    this.form.getToolBarManager().add(new Separator());
    this.form.getToolBarManager().add(this.invokeBuild);
    this.form.getToolBarManager().add(new Separator());
    this.form.getToolBarManager().add(openLogs);
    this.form.getToolBarManager().add(openInWeb);

    this.form.getToolBarManager().update(false);
  }

  protected BuildEditorInput getBuildEditorInput() {
    return (BuildEditorInput) getEditorInput();
  }

  protected void reloadData() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        try {
          BuildEditorInput details = (BuildEditorInput) getEditorInput();
          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(
              getBuildEditorInput().getJobUrl());
          BuildPart.this.dataBuildDetail = service.getJobDetails(details.getBuildUrl(), monitor);
          BuildPart.this.dataJobDetails = service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor);
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
    if (this.dataBuildDetail != null && this.dataBuildDetail.url != null) {
      CloudBeesUIPlugin.getDefault().openWithBrowser(this.dataBuildDetail.url);
      return;
    }

    // for some reason build details not available (for example, no build was available). fall back to job url
    BuildEditorInput details = (BuildEditorInput) getEditorInput();
    CloudBeesUIPlugin.getDefault().openWithBrowser(getBuildEditorInput().getJobUrl());
  }

  protected void switchToBuild(final long buildNo) {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        BuildEditorInput details = (BuildEditorInput) getEditorInput();
        String newJobUrl = getBuildEditorInput().getJobUrl() + "/" + buildNo + "/";

        JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(
            getBuildEditorInput().getJobUrl());

        try {
          BuildPart.this.dataBuildDetail = service.getJobDetails(newJobUrl, monitor);
          details.setBuildUrl(BuildPart.this.dataBuildDetail.url);
          BuildPart.this.dataJobDetails = service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor);

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

  @Override
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
      if (this.contentBuildHistory != null && !this.contentBuildHistory.isDisposed()) {
        this.contentBuildHistory.dispose();
      }

      this.contentJUnitTests.setText("No data available.");
      this.testsLink.setVisible(false);
      this.testsLink.getParent().layout(true);
      this.textTopSummary.setText("Latest build not available.");
      this.form.setText(getBuildEditorInput().getDisplayName());
      setPartName(getBuildEditorInput().getDisplayName());

    } else {

      IRunnableWithProgress op = new IRunnableWithProgress() {
        public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

          JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(
              getBuildEditorInput().getBuildUrl());

          try {

            BuildPart.this.dataBuildDetail = service.getJobDetails(getBuildEditorInput().getBuildUrl(), monitor);

            BuildPart.this.dataJobDetails = service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor);

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
        if (BuildPart.this.dataBuildDetail != null) {
          setPartName(details.getDisplayName() + " #" + BuildPart.this.dataBuildDetail.number);
        } else {
          setPartName(details.getDisplayName());
        }

        //setContentDescription(detail.fullDisplayName);

        if (BuildPart.this.form != null) {

          if (BuildPart.this.dataBuildDetail != null) {
            BuildPart.this.form.setText("Build #" + BuildPart.this.dataBuildDetail.number + " [" + details.getDisplayName() + "]");
          } else {
            BuildPart.this.form.setText(details.getDisplayName());
          }

        }

        String topStr = BuildPart.this.dataBuildDetail.result != null ? BuildPart.this.dataBuildDetail.result /*+ " ("
                                                                                + new Date(dataBuildDetail.timestamp) + ")"*/
            : "";

        if (BuildPart.this.dataBuildDetail.building) {
          topStr = "BUILDING";
        } else if (BuildPart.this.dataJobDetails.inQueue) {
          topStr = "IN QUEUE";
        } /*else {
          topStr = topStr + " " + Utils.humanReadableTime((System.currentTimeMillis() - dataBuildDetail.timestamp))
              + " ago";
          }
         */
        BuildPart.this.textTopSummary.setText(topStr);


        loadBuildSummary();

        // Load JUnit Tests
        loadUnitTests();

        loadBuildHistory();

        // Recent Changes
        loadRecentChanges();

        BuildPart.this.invokeBuild.setEnabled(BuildPart.this.dataJobDetails.buildable);

        if ("SUCCESS".equalsIgnoreCase(BuildPart.this.dataBuildDetail.result)) {
          BuildPart.this.statusIcon.setImage(CloudBeesDevUiPlugin.getImage(CBImages.IMG_COLOR_16_BLUE));
        } else if ("FAILURE".equalsIgnoreCase(BuildPart.this.dataBuildDetail.result)) {
          BuildPart.this.statusIcon.setImage(CloudBeesDevUiPlugin.getImage(CBImages.IMG_COLOR_16_RED));
        } else {
          BuildPart.this.statusIcon.setImage(null);
        }

        //form.layout();
        //form.getBody().layout(true);
        //BuildPart.this.compMain.pack(true);
        //form.pack();
        BuildPart.this.compMain.layout(true);

        //form.reflow(true);
        BuildPart.this.form.layout(true);
        BuildPart.this.form.getBody().layout(true);
        //details..form.layout();

      }
    });
  }

  private void loadBuildHistory() {

    if (this.contentBuildHistory != null && !this.contentBuildHistory.isDisposed()) {
      this.contentBuildHistory.dispose();
    }

    //contentBuildHistoryHolder.layout(true);

    this.contentBuildHistory = this.formToolkit.createComposite(this.contentBuildHistoryHolder, SWT.NONE);
    GridLayout gl = new GridLayout(1, false);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    gl.verticalSpacing = 0;
    this.contentBuildHistory.setLayout(gl);


    if (this.dataJobDetails.builds == null || this.dataJobDetails.builds.length == 0) {
      this.formToolkit.createLabel(this.contentBuildHistory, "No recent builds.");//TODO i18n
      this.contentBuildHistoryHolder.layout(true);
      return;
    }

    //contentBuildHistory.setBackground(composite.getBackground());

    StringBuffer val = new StringBuffer();
    for (JenkinsJobAndBuildsResponse.Build b : this.dataJobDetails.builds) {

      //String result = b.result != null && b.result.length() > 0 ? " - " + b.result : "";

      String timeComp = (Utils.humanReadableTime((System.currentTimeMillis() - b.timestamp))) + " ago";

      Image image = null;
      if ("success".equalsIgnoreCase(b.result)) {
        image = CloudBeesDevUiPlugin.getImage(CBImages.IMG_COLOR_16_BLUE);
      } else if ("failure".equalsIgnoreCase(b.result)) {
        image = CloudBeesDevUiPlugin.getImage(CBImages.IMG_COLOR_16_RED);
      } else {
        image = CloudBeesDevUiPlugin.getImage(CBImages.IMG_COLOR_16_GREY);
      }

      Composite comp;
      if (b.number != this.dataBuildDetail.number) {
        comp = createImageLink(this.contentBuildHistory, "<a>#" + b.number + "</a>  " + timeComp, image, new SelectionAdapter() {
          @Override
          public void widgetSelected(final SelectionEvent e) {
            if (e.text != null && e.text.startsWith("#")) {
              long buildNo = new Long(e.text.substring(1)).longValue();
              BuildPart.this.switchToBuild(buildNo);
            }
          }
        });
      } else {
        comp = createImageLabel(this.contentBuildHistory, "#" + b.number + "  " + timeComp, image);
      }

      GridData gd = new GridData();
      gd.verticalIndent = 2;
      comp.setLayoutData(gd);

    }

    //BuildPart.this.compMain.layout();
    this.contentBuildHistoryHolder.layout(true);
    //contentBuildHistory.setText(val.toString());

  }

  private void loadBuildSummary() {
    //details.getJob().buildable;
    //details.getJob().inQueue;
    //details.getJob().healthReport;

    StringBuffer summary = new StringBuffer();
    if (this.dataBuildDetail.description != null) {
      summary.append(this.dataBuildDetail.description + "\n");
    }

    StringBuffer causeBuffer = new StringBuffer();
    if (this.dataBuildDetail.actions != null && this.dataBuildDetail.actions.length > 0) {
      for (int i = 0; i < this.dataBuildDetail.actions.length; i++) {
        com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Action action = this.dataBuildDetail.actions[i];
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

    if (this.dataBuildDetail.builtOn != null && this.dataBuildDetail.timestamp != null) {
      if (this.dataBuildDetail.builtOn != null && this.dataBuildDetail.builtOn.length() > 0) {
        summary.append("Built on: " + this.dataBuildDetail.builtOn + " at " + (new Date(this.dataBuildDetail.timestamp)) + "\n");
      } else {
        summary.append("Built at " + (new Date(this.dataBuildDetail.timestamp)) + "\n");
      }
    }

    //summary.append("Buildable: " + details.getJob().buildable + "\n");
    //summary.append("Build number: " + dataBuildDetail.number + "\n");

    this.contentBuildSummary.setText(summary.toString());

    if (this.healthTest != null && !this.healthTest.isDisposed()) {
      this.healthTest.dispose();
    }

    if (this.healthBuild != null && !this.healthBuild.isDisposed()) {
      this.healthBuild.dispose();
    }

    //compBuildSummary.redraw();
    //compBuildSummary.layout(true);

    HealthReport[] hr = this.dataJobDetails.healthReport;
    if (hr != null && hr.length > 0) {
      //summary.append("\nProject Health\n");
      for (HealthReport rep : hr) {
        //summary.append(rep.description + "\n"); // + " Score:" + rep.score + "%\n"
        //System.out.println("ICON URL: " + rep.iconUrl);
        String testMatch = "Test Result: ";
        if (rep.description.startsWith(testMatch)) {
          this.healthTest = createImageLabel(this.compBuildSummary, rep.description.substring(testMatch.length()),
              CloudBeesDevUiPlugin.getImage(CBImages.IMG_HEALTH_PREFIX + CBImages.IMG_24 + rep.iconUrl));
        } else {
          String buildMatch = "Build stability: ";
          if (rep.description.startsWith(buildMatch)) {
            this.healthBuild = createImageLabel(this.compBuildSummary, rep.description.substring(buildMatch.length()),
                CloudBeesDevUiPlugin.getImage(CBImages.IMG_HEALTH_PREFIX + CBImages.IMG_24 + rep.iconUrl));
          }
        }
      }

      this.compBuildSummary.layout(true);
    }
  }

  private Composite createImageLabel(final Composite parent, final String text, final Image image) {
    Composite comp = this.formToolkit.createComposite(parent);
    GridLayout gl = new GridLayout(2, false);
    comp.setLayout(gl);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    gl.verticalSpacing = 0;
    Label imgLabel = this.formToolkit.createLabel(comp, "", SWT.NONE);
    imgLabel.setImage(image);
    Label label = this.formToolkit.createLabel(comp, text, SWT.NONE);
    return comp;
  }

  private Composite createImageLink(final Composite parent, final String text, final Image image, final SelectionListener selectionListener) {
    Composite comp = this.formToolkit.createComposite(parent);
    GridLayout gl = new GridLayout(2, false);
    comp.setLayout(gl);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    gl.verticalSpacing = 0;

    Label imgLabel = this.formToolkit.createLabel(comp, "", SWT.NONE);
    imgLabel.setImage(image);
    Link link = new Link(comp, SWT.NONE);
    link.setText(text);
    link.addSelectionListener(selectionListener);
    link.setBackground(this.formToolkit.getColors().getBackground());
    return comp;
  }

  private void loadUnitTests() {

    if (this.dataBuildDetail.actions == null) {
      this.contentJUnitTests.setText("No Tests");
      this.testsLink.setVisible(false);
      this.testsLink.getParent().layout(true);
      return;
    }

    for (com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Action action : this.dataBuildDetail.actions) {
      if ("testReport".equalsIgnoreCase(action.urlName)) {
        String val = "Total: " + action.totalCount + " Failed: " + action.failCount + " Skipped: " + action.skipCount;
        this.contentJUnitTests.setText(val);
        this.openJunitAction.selectionChanged(new StructuredSelection(this.dataBuildDetail));
        this.testsLink.setVisible(true);
        this.testsLink.getParent().layout(true);
        return;
      }
    }

    this.contentJUnitTests.setText("No Tests");
    this.testsLink.setVisible(false);
    this.testsLink.getParent().layout(true);
  }

  private void loadRecentChanges() {

    //StringBuffer changes = new StringBuffer();
    //Point origSize = treeViewerRecentChanges.getTree().getSize();
    if (this.dataBuildDetail.changeSet != null && this.dataBuildDetail.changeSet.items != null
        && this.dataBuildDetail.changeSet.items.length > 0) {
      //changesContentProvider.setModel(dataBuildDetail.changeSet.items);
      this.treeViewerRecentChanges.setInput(this.dataBuildDetail);
      this.changesetLabel.setVisible(false);
      this.treeViewerRecentChanges.getTree().setVisible(true);
      this.treeViewerRecentChanges.refresh();
      //createChangeTreeViewer(dataBuildDetail.changeSet.items);
    } else {
      //changesContentProvider.setModel(new ChangeSetItem[0]);
      this.treeViewerRecentChanges.setInput(new ChangeSetItem[0]);
      this.changesetLabel.setVisible(true);
      this.changesetLabel.setText("No changes");
      this.treeViewerRecentChanges.getTree().setVisible(false);
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
    this.form.setFocus();
  }

  @Override
  public void doSave(final IProgressMonitor monitor) {
    // Do the Save operation
  }

  @Override
  public void doSaveAs() {
    // Do the Save As operation
  }

  @Override
  public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {

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
    this.form.dispose();
    this.form = null;
    super.dispose();
  }
}
