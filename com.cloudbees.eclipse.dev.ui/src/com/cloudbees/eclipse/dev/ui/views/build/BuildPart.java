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
import org.eclipse.swt.custom.SashForm;
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
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Action.Cause;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.actions.OpenJunitViewAction;
import com.cloudbees.eclipse.dev.ui.actions.OpenLogAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class BuildPart extends EditorPart {

  public static final String ID = "com.cloudbees.eclipse.dev.ui.views.build.BuildPart"; //$NON-NLS-1$
  private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

  private JenkinsBuildDetailsResponse dataBuildDetail;
  protected JenkinsJobAndBuildsResponse dataJobDetails;

  private Form form;

  private Label statusIcon;
  private Composite compMain;
  private Composite compTop;
  private Composite healthTest;
  private Composite healthBuild;
  private Label textTopSummary;
  private Section sectSummary;
  private Composite compSummary;
  private Composite compBuildSummary;
  private Label contentBuildSummary;

  private Section sectBuildHistory;
  private Composite contentBuildHistory;
  private Composite contentBuildHistoryHolder;

  private Label contentJUnitTests;
  private OpenJunitViewAction openJunitAction;
  private Composite testsLink;

  private Section sectRecentChanges;
  private TreeViewer treeViewerRecentChanges;
  private Label changesetLabel;

  private Section sectArtifacts;
  private TreeViewer treeViewerArtifacts;
  private Label artifactsLabel;

  private Action openBuildHistory;
  private Action invokeBuild;
  private OpenLogAction openLogs;
  private ScrolledComposite scrolledRecentChanges;
  private Label labelSpace;

  public BuildPart() {
    super();
  }

  public void setData(final JenkinsBuildDetailsResponse dataBuildDetail,
      final JenkinsJobAndBuildsResponse dataJobDetails) {
    this.dataBuildDetail = dataBuildDetail;
    this.dataJobDetails = dataJobDetails;

    this.openLogs.setBuild(dataBuildDetail);
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
    GridLayout gl_compMain = new GridLayout(1, true);
    this.compMain.setLayout(gl_compMain);
    this.compMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.formToolkit.adapt(this.compMain);
    this.formToolkit.paintBordersFor(this.compMain);

    Composite compStatusHead = new Composite(this.compMain, SWT.NONE);
    compStatusHead.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
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

    this.openJunitAction = new OpenJunitViewAction();

    //createBuildHistorySection();

    createSections();

    //compMain.layout(true);

    createActions();

    loadInitialData();
  }

  private void createSections() {

    SashForm sashForm = new SashForm(this.compMain, SWT.VERTICAL);
    sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.formToolkit.adapt(sashForm);
    this.formToolkit.paintBordersFor(sashForm);

    this.compTop = new Composite(sashForm, SWT.NONE);
    this.formToolkit.adapt(this.compTop);
    this.formToolkit.paintBordersFor(this.compTop);
    GridLayout gl_compTop = new GridLayout(2, true);
    gl_compTop.marginWidth = 0;
    this.compTop.setLayout(gl_compTop);

    this.sectSummary = this.formToolkit.createSection(this.compTop, Section.TITLE_BAR);
    this.sectSummary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.sectSummary.setSize(107, 45);
    this.formToolkit.adapt(this.sectSummary);
    this.formToolkit.paintBordersFor(this.sectSummary);
    this.sectSummary.setText("Build Summary");

    this.compSummary = new Composite(this.sectSummary, SWT.NONE);
    this.formToolkit.adapt(this.compSummary);
    this.formToolkit.paintBordersFor(this.compSummary);
    this.sectSummary.setClient(this.compSummary);
    this.compSummary.setLayout(new GridLayout(1, false));

    this.compBuildSummary = new Composite(this.compSummary, SWT.NONE);
    this.formToolkit.adapt(this.compBuildSummary);
    this.formToolkit.paintBordersFor(this.compBuildSummary);
    this.compBuildSummary.setLayout(new GridLayout(1, false));

    this.contentBuildSummary = this.formToolkit.createLabel(this.compBuildSummary, "n/a", SWT.NONE);
    this.contentBuildSummary.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

    //    Section sectTests = this.formToolkit.createSection(this.compMain, Section.TITLE_BAR);
    //    GridData gd_sectTests = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    //    gd_sectTests.verticalIndent = 10;
    //    sectTests.setLayoutData(gd_sectTests);
    //    sectTests.setSize(80, 45);
    //    this.formToolkit.paintBordersFor(sectTests);
    //    sectTests.setText("JUnit Tests");
    //
    Composite compTests = new Composite(this.compSummary, SWT.NONE);
    this.formToolkit.adapt(compTests);
    this.formToolkit.paintBordersFor(compTests);
    //    sectTests.setClient(compTests);
    GridLayout gl_compTests = new GridLayout(4, false);
    compTests.setLayout(gl_compTests);

    Label label = this.formToolkit.createLabel(compTests, "");
    label.setImage(CloudBeesDevUiPlugin.getImage(CBImages.IMG_JUNIT));
    label.setText("");

    this.contentJUnitTests = this.formToolkit.createLabel(compTests, "n/a", SWT.NONE);
    this.contentJUnitTests.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

    this.labelSpace = new Label(compTests, SWT.NONE);
    this.formToolkit.adapt(this.labelSpace, true, true);
    this.testsLink = new Composite(compTests, SWT.NONE);
    this.formToolkit.adapt(this.testsLink);
    this.testsLink.setLayout(new GridLayout(2, false));
    Link link = new Link(this.testsLink, SWT.FLAT);
    link.setText("Show in <a>JUnit View</a>");
    link.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        BuildPart.this.openJunitAction.run();
      }
    });
    this.formToolkit.adapt(link, false, false);
    this.testsLink.setVisible(false);

    this.sectArtifacts = this.formToolkit.createSection(this.compTop, Section.TITLE_BAR);
    this.sectArtifacts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.sectArtifacts.setSize(107, 45);
    this.formToolkit.adapt(this.sectArtifacts);
    this.formToolkit.paintBordersFor(this.sectArtifacts);
    this.sectArtifacts.setText("Artifacts");

    Composite compInterm_1 = this.formToolkit.createComposite(this.sectArtifacts, SWT.NONE);

    GridLayout gl_compInterm_1 = new GridLayout(1, false);
    gl_compInterm_1.verticalSpacing = 0;
    gl_compInterm_1.marginWidth = 0;
    gl_compInterm_1.marginHeight = 0;
    compInterm_1.setLayout(gl_compInterm_1);
    compInterm_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    this.sectArtifacts.setClient(compInterm_1);

    this.artifactsLabel = this.formToolkit.createLabel(compInterm_1, "");

    ScrolledComposite scrolledArtifacts = new ScrolledComposite(compInterm_1, SWT.H_SCROLL | SWT.V_SCROLL);
    scrolledArtifacts.setExpandHorizontal(true);
    scrolledArtifacts.setExpandVertical(true);
    scrolledArtifacts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.formToolkit.adapt(scrolledArtifacts);
    this.formToolkit.paintBordersFor(scrolledArtifacts);

    this.treeViewerArtifacts = new TreeViewer(scrolledArtifacts, SWT.BORDER);
    Tree treeArtifacts = this.treeViewerArtifacts.getTree();

    this.treeViewerArtifacts.setContentProvider(new ArtifactsContentProvider());
    this.treeViewerArtifacts.setLabelProvider(new ArtifactsLabelProvider());
    this.treeViewerArtifacts.addDoubleClickListener(new ArtifactsClickListener());

    this.treeViewerArtifacts.setInput("n/a");

    this.formToolkit.adapt(treeArtifacts);
    this.formToolkit.paintBordersFor(treeArtifacts);
    scrolledArtifacts.setContent(treeArtifacts);
    scrolledArtifacts.setMinSize(treeArtifacts.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    this.sectRecentChanges = this.formToolkit.createSection(sashForm, Section.TITLE_BAR);

    this.formToolkit.paintBordersFor(this.sectRecentChanges);
    this.sectRecentChanges.setText("Changes");

    Composite compInterm = this.formToolkit.createComposite(this.sectRecentChanges, SWT.NONE);

    GridLayout gl_compInterm = new GridLayout(1, false);
    gl_compInterm.verticalSpacing = 0;
    gl_compInterm.marginWidth = 0;
    gl_compInterm.marginHeight = 0;
    compInterm.setLayout(gl_compInterm);
    compInterm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    this.sectRecentChanges.setClient(compInterm);

    this.changesetLabel = this.formToolkit.createLabel(compInterm, "");
    this.scrolledRecentChanges = new ScrolledComposite(compInterm, SWT.H_SCROLL | SWT.V_SCROLL);
    this.scrolledRecentChanges.setExpandHorizontal(true);
    this.scrolledRecentChanges.setExpandVertical(true);
    this.scrolledRecentChanges.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.formToolkit.adapt(this.scrolledRecentChanges);
    this.formToolkit.paintBordersFor(this.scrolledRecentChanges);

    this.treeViewerRecentChanges = new TreeViewer(this.scrolledRecentChanges, SWT.BORDER);
    Tree treeRecentChanges = this.treeViewerRecentChanges.getTree();

    this.treeViewerRecentChanges.setContentProvider(new RecentChangesContentProvider());
    this.treeViewerRecentChanges.setLabelProvider(new RecentChangesLabelProvider());
    this.treeViewerRecentChanges.addDoubleClickListener(new RecentChangesClickListener());

    this.treeViewerRecentChanges.setInput("n/a");

    this.formToolkit.adapt(treeRecentChanges);
    this.formToolkit.paintBordersFor(treeRecentChanges);
    this.scrolledRecentChanges.setContent(treeRecentChanges);
    this.scrolledRecentChanges.setMinSize(treeRecentChanges.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    sashForm.setWeights(new int[] { 1, 1 });
  }

  private void createBuildHistorySection() {
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

    this.openLogs = new OpenLogAction();
    this.openLogs.setBuild(this.dataBuildDetail);
    //    Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
    //      @Override
    //      public void run() {
    //        if (BuildPart.this.dataBuildDetail != null && BuildPart.this.dataBuildDetail.url != null) {
    //          CloudBeesUIPlugin.getDefault().openWithBrowser(BuildPart.this.dataBuildDetail.url + "/consoleText");
    //          return;
    //        }
    //
    //      }
    //    };
    //    openLogs.setToolTipText("Open build log"); //TODO i18n
    //    openLogs.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_CONSOLE));

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

    this.openBuildHistory = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        try {
          final String jobUrl = BuildPart.this.getBuildEditorInput().getJobUrl();
          org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Opening build history...") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
              try {
                CloudBeesDevUiPlugin.getDefault().showBuildHistory(jobUrl, true);
        //                ns.invokeBuild(jobUrl, props, monitor);
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
    this.openBuildHistory.setToolTipText("Open build history for this job"); //TODO i18n
    this.openBuildHistory.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BUILD_HISTORY));

    this.form.getToolBarManager().add(this.invokeBuild);
    this.form.getToolBarManager().add(new Separator());
    this.form.getToolBarManager().add(this.openLogs);
    this.form.getToolBarManager().add(this.openBuildHistory);
    this.form.getToolBarManager().add(openInWeb);
    this.form.getToolBarManager().add(new Separator());
    this.form.getToolBarManager().add(reload);

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

          setData(service.getJobDetails(details.getBuildUrl(), monitor),
              service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor));

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
    CloudBeesUIPlugin.getDefault().openWithBrowser(getBuildEditorInput().getJobUrl());
  }

  protected void switchToBuild(final long buildNo) {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        BuildEditorInput details = (BuildEditorInput) getEditorInput();
        String newBuildUrl = getBuildEditorInput().getJobUrl() + "/" + buildNo + "/";

        JenkinsService service = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(
            getBuildEditorInput().getJobUrl());

        try {
          setData(service.getJobDetails(newBuildUrl, monitor),
              service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor));

          details.setBuildUrl(BuildPart.this.dataBuildDetail.url);
          details.setDisplayName(BuildPart.this.dataBuildDetail.fullDisplayName);

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

    if (details == null || details.getBuildUrl() == null) {

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

            setData(service.getJobDetails(getBuildEditorInput().getBuildUrl(), monitor),
                service.getJobBuilds(getBuildEditorInput().getJobUrl(), monitor));

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
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      public void run() {

        BuildEditorInput details = (BuildEditorInput) getEditorInput();

        //        if (BuildPart.this.dataBuildDetail != null) {
        //          setPartName(details.getDisplayName() + " #" + BuildPart.this.dataBuildDetail.number);
        //        } else {
          setPartName(details.getDisplayName());
        //        }

        //setContentDescription(detail.fullDisplayName);

        if (BuildPart.this.form != null) {
          //          if (BuildPart.this.dataBuildDetail != null) {
            BuildPart.this.form.setText("Build" + /*" #" + BuildPart.this.dataBuildDetail.number +*/" ["
                + details.getDisplayName() + "]");
          //          } else {
          //            BuildPart.this.form.setText(details.getDisplayName());
          //          }
        }

        if (BuildPart.this.dataBuildDetail != null) {
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
          if ("SUCCESS".equalsIgnoreCase(BuildPart.this.dataBuildDetail.result)) {
            BuildPart.this.statusIcon.setImage(CloudBeesDevUiPlugin.getImage(CBImages.IMG_COLOR_16_BLUE));
          } else if ("FAILURE".equalsIgnoreCase(BuildPart.this.dataBuildDetail.result)) {
            BuildPart.this.statusIcon.setImage(CloudBeesDevUiPlugin.getImage(CBImages.IMG_COLOR_16_RED));
          } else if ("UNSTABLE".equalsIgnoreCase(BuildPart.this.dataBuildDetail.result)) {
            BuildPart.this.statusIcon.setImage(CloudBeesDevUiPlugin.getImage(CBImages.IMG_COLOR_16_YELLOW));
          } else {
            BuildPart.this.statusIcon.setImage(null);
          }
        }

        loadBuildSummary();

        loadUnitTests();

        //loadBuildHistory();

        loadArtifacts();

        loadRecentChanges();

        BuildPart.this.invokeBuild.setEnabled(BuildPart.this.dataJobDetails != null
            && BuildPart.this.dataJobDetails.buildable != null && BuildPart.this.dataJobDetails.buildable);

        //form.layout();
        //form.getBody().layout(true);
        //BuildPart.this.compMain.pack(true);
        //form.pack();
        BuildPart.this.compMain.layout(true);

        //form.reflow(true);
        BuildPart.this.form.layout(true);
        BuildPart.this.form.getBody().layout(true);
        //details..form.layout();

        //        BuildPart.this.form.getBody().redraw();
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

    for (JenkinsBuild b : this.dataJobDetails.builds) {

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

  @SuppressWarnings("unused")
  private void loadBuildSummary() {
    //details.getJob().buildable;
    //details.getJob().inQueue;
    //details.getJob().healthReport;

    if (this.dataBuildDetail != null) {
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
          summary.append("Built on: " + this.dataBuildDetail.builtOn + " at "
              + (new Date(this.dataBuildDetail.timestamp)) + "\n");
        } else {
          summary.append("Built at " + (new Date(this.dataBuildDetail.timestamp)) + "\n");
        }
      }

      //summary.append("Buildable: " + details.getJob().buildable + "\n");
      //summary.append("Build number: " + dataBuildDetail.number + "\n");

      this.contentBuildSummary.setText(summary.toString());
    }

    if (this.healthTest != null && !this.healthTest.isDisposed()) {
      this.healthTest.dispose();
    }

    if (this.healthBuild != null && !this.healthBuild.isDisposed()) {
      this.healthBuild.dispose();
    }

    //compBuildSummary.redraw();
    //compBuildSummary.layout(true);

    if (this.dataJobDetails != null) {
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
              this.healthBuild = createImageLabel(this.compBuildSummary,
                  rep.description.substring(buildMatch.length()),
                  CloudBeesDevUiPlugin.getImage(CBImages.IMG_HEALTH_PREFIX + CBImages.IMG_24 + rep.iconUrl));
            }
          }
        }
      }
    }

    BuildPart.this.compBuildSummary.layout(true);
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
    @SuppressWarnings("unused")
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
    if (this.dataBuildDetail == null || this.dataBuildDetail.actions == null) {
      this.contentJUnitTests.setText("No Tests");
      this.testsLink.setVisible(false);
      this.testsLink.getParent().layout(true);
      return;
    }

    for (com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Action action : this.dataBuildDetail.actions) {
      if ("testReport".equalsIgnoreCase(action.urlName)) {
        String val = "Tests: " + action.totalCount + "  Failed: " + action.failCount + "  Skipped: " + action.skipCount;
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
    if (this.dataBuildDetail != null && this.dataBuildDetail.changeSet != null
        && this.dataBuildDetail.changeSet.items != null
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

  private void loadArtifacts() {
    if (this.dataBuildDetail != null && this.dataBuildDetail.artifacts != null
        && this.dataBuildDetail.artifacts.length > 0) {
      this.treeViewerArtifacts.setInput(this.dataBuildDetail);
      this.artifactsLabel.setVisible(false);
      this.treeViewerArtifacts.getTree().setVisible(true);
      this.treeViewerArtifacts.refresh();
    } else {
      this.treeViewerArtifacts.setInput(null);
      this.artifactsLabel.setVisible(true);
      this.artifactsLabel.setText("No artifacts");
      this.treeViewerArtifacts.getTree().setVisible(false);
    }
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
