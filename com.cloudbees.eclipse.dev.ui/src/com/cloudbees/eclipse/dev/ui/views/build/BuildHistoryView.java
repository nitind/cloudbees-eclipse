package com.cloudbees.eclipse.dev.ui.views.build;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.actions.ReloadBuildHistoryAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

public class BuildHistoryView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.dev.ui.views.build.BuildHistoryView";

  private TableViewer table;

  protected ReloadBuildHistoryAction actionReloadJobs;
  private Action actionInvokeBuild;
  private Action actionOpenBuildInBrowser;

  private final Map<String, Image> stateIcons = new HashMap<String, Image>();

  private JenkinsChangeListener jenkinsChangeListener;

  private BuildHistoryContentProvider contentProvider;

  private String jobUrl;

  protected Object selectedBuild;

  public BuildHistoryView() {
    super();
  }

  public Object getSelectedBuild() {
    return this.selectedBuild;
  }

  protected void setInput(final JenkinsJobAndBuildsResponse newView) {
    if (newView != null && newView.viewUrl != null) {
      IViewSite site = getViewSite();
      String secId = site.getSecondaryId();
      String servUrl = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(newView.viewUrl).getUrl();
      if (secId != null && servUrl != null && !secId.equals(Long.toString(servUrl.hashCode()))) {
        return; // another view
      }
    }

    if (newView == null || newView.builds == null) {
      setContentDescription("No builds available.");
      this.contentProvider.inputChanged(this.table, null, null);
    } else {
      String label = newView.name; // CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(newView.viewUrl).getLabel();
      setContentDescription(label + " (" + new Date() + ")");
      setPartName("Build History [" + label + "]");
      this.contentProvider.inputChanged(this.table, null, newView);
    }

    if (newView != null) {
      this.jobUrl = newView.viewUrl;
    } else {
      this.jobUrl = null;
    }

    this.actionReloadJobs.setViewUrl(this.jobUrl);

    this.table.refresh();

    boolean reloadable = newView != null;
    this.actionReloadJobs.setEnabled(reloadable);
  }

  @Override
  public void createPartControl(final Composite parent) {
    initImages();

    this.table = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION
    /*SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL*/);
    //Tree tree = viewer.getTree();
    //tree.setHeaderVisible(true);

    //table.getTable().setLinesVisible(true);
    this.table.getTable().setHeaderVisible(true);

    createColumn("S", 20, BuildSorter.STATE, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild build = (JenkinsBuild) cell.getViewerRow().getElement();

        String key = build.result;

        /*        ImageData[] imageDatas = new ImageLoader().load(new FileInputStream("myAnimated.gif"));
                Image[] images = new Image[imageDatas.length];
                for (int n = 0; n < imageDatas.length; n++) {
                  // images[n] = new Image(myTable.getDislay(), imageDatas[n]);
                }
         */
        //        if (job.color != null && job.color.contains("_")) {
        //          key = job.color.substring(0, job.color.indexOf("_"));
        //        }

        Image img = BuildHistoryView.this.stateIcons.get(key);

        if (img != null) {
          cell.setText("");
          cell.setImage(img);
        } else {
          cell.setImage(null);
          cell.setText(build.result);
        }

      }

    });

    //TODO i18n
    TableViewerColumn namecol = createColumn("Build", 50, BuildSorter.BUILD, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild build = (JenkinsBuild) cell.getViewerRow().getElement();
        String val;
        try {
          if (build.building) {
            val = "building";
          } else {
            val = build.number.toString();
          }
        } catch (Exception e) {
          val = "";
        }
        cell.setText(val);
      }
    });

    createColumn("When", 100, BuildSorter.TIME, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild build = (JenkinsBuild) cell.getViewerRow().getElement();

        cell.setText(Utils.humanReadableTime(System.currentTimeMillis() - build.timestamp) + " ago");
        cell.setImage(null);
      }
    });

    createColumn("Duration", 100, BuildSorter.DURATION, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild build = (JenkinsBuild) cell.getViewerRow().getElement();
        try {
          cell.setText(Utils.humanReadableTime(build.duration));
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });

    createColumn("Tests", 250, BuildSorter.TESTS, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild build = (JenkinsBuild) cell.getViewerRow().getElement();
        try {
          cell.setText(""); // TODO
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });
    createColumn("Cause", 250, BuildSorter.CAUSE, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild job = (JenkinsBuild) cell.getViewerRow().getElement();
        try {
          cell.setText(""); // TODO
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });

    this.contentProvider = new BuildHistoryContentProvider();

    this.table.setContentProvider(this.contentProvider);

    BuildSorter sorter = new BuildSorter(BuildSorter.BUILD);
    sorter.setDirection(SWT.UP);
    this.table.setSorter(sorter);

    this.table.setInput(getViewSite());

    /*
        table.addFilter(new ViewerFilter() {
          @Override
          public boolean select(Viewer viewer, Object parentElement, Object element) {
            return true;
          }
        });*/

    this.table.addOpenListener(new IOpenListener() {

      public void open(final OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof IStructuredSelection) {
          Object el = ((IStructuredSelection) sel).getFirstElement();
          if (el instanceof JenkinsBuild) {
            CloudBeesDevUiPlugin.getDefault().showBuild(((JenkinsBuild) el));
          }
        }

      }

    });

    this.table.getTable().setSortColumn(namecol.getColumn());
    this.table.getTable().setSortDirection(SWT.DOWN);

    makeActions();
    contributeToActionBars();

    MenuManager popupMenu = new MenuManager();

    //    popupMenu.add(this.actionOpenLastBuildDetails);
    //    popupMenu.add(this.actionOpenLog);
    //popupMenu.add(new Separator());
    popupMenu.add(this.actionOpenBuildInBrowser);
    popupMenu.add(this.actionInvokeBuild);
    popupMenu.add(new Separator());
    //    popupMenu.add(this.actionDeleteJob);
    //popupMenu.add(new Separator());
    popupMenu.add(this.actionReloadJobs);

    Menu menu = popupMenu.createContextMenu(this.table.getTable());
    this.table.getTable().setMenu(menu);

    this.table.addPostSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(final SelectionChangedEvent event) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        BuildHistoryView.this.selectedBuild = sel.getFirstElement();
        boolean enable = sel.getFirstElement() != null;
        BuildHistoryView.this.actionInvokeBuild.setEnabled(enable);
        //        BuildHistoryView.this.actionOpenLastBuildDetails.setEnabled(enable);
        //        BuildHistoryView.this.actionOpenLog.setEnabled(enable);
        //        BuildHistoryView.this.actionDeleteJob.setEnabled(enable);
        BuildHistoryView.this.actionOpenBuildInBrowser.setEnabled(enable);
      }
    });

    this.jenkinsChangeListener = new JenkinsChangeListener() {
      public void activeJobViewChanged(final JenkinsJobsResponse newView) {
      }

      public void activeJobHistoryChanged(final JenkinsJobAndBuildsResponse newView) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            BuildHistoryView.this.setInput(newView);
          }
        });
      }

      public void jenkinsChanged(final List<JenkinsInstanceResponse> instances) {
      }
    };

    CloudBeesUIPlugin.getDefault().addJenkinsChangeListener(this.jenkinsChangeListener);
  }

  protected String formatBuildInfo(final JenkinsBuild build) {
    String unit = "";
    if (build.duration != null) {
      //TODO Implement proper human-readable duration conversion, consider using the same conversion rules that Jenkins uses
      //CloudBeesUIPlugin.getDefault().getLogger().info("DURATION: " + build.timestamp);
      unit = Utils.humanReadableTime((System.currentTimeMillis() - build.timestamp));
    }
    String timeComp = build.duration != null ? /*", " + */unit + " ago" : "";
    //String buildComp = build.number != null ? "#" + build.number : "n/a";
    String buildComp = " #" + build.number;
    return timeComp + buildComp;
  }

  private void initImages() {
    //TODO Refactor to use CBImages!
    this.stateIcons.put(
        "",
        ImageDescriptor.createFromURL(
            CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/grey.gif"))
            .createImage());
    this.stateIcons.put(
        "SUCCESS",
        ImageDescriptor.createFromURL(
            CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/blue.gif"))
            .createImage());
    this.stateIcons.put(
        "FAILURE",
        ImageDescriptor.createFromURL(
            CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/red.gif"))
            .createImage());

  }

  private TableViewerColumn createColumn(final String colName, final int width,
      final CellLabelProvider cellLabelProvider) {
    return createColumn(colName, width, -1, cellLabelProvider);
  }

  private TableViewerColumn createColumn(final String colName, final int width, final int sortCol,
      final CellLabelProvider cellLabelProvider) {
    final TableViewerColumn treeViewerColumn = new TableViewerColumn(this.table, SWT.NONE);
    TableColumn col = treeViewerColumn.getColumn();

    if (width > 0) {
      col.setWidth(width);
    }

    col.setText(colName);
    col.setMoveable(true);

    treeViewerColumn.setLabelProvider(cellLabelProvider);

    if (sortCol >= 0) {

      treeViewerColumn.getColumn().addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(final SelectionEvent e) {

          int newOrder = SWT.DOWN;

          if (BuildHistoryView.this.table.getTable().getSortColumn().equals(treeViewerColumn.getColumn())
              && BuildHistoryView.this.table.getTable().getSortDirection() == SWT.DOWN) {
            newOrder = SWT.UP;
          }

          BuildHistoryView.this.table.getTable().setSortColumn(treeViewerColumn.getColumn());
          BuildHistoryView.this.table.getTable().setSortDirection(newOrder);
          BuildSorter newSorter = new BuildSorter(sortCol);
          newSorter.setDirection(newOrder);
          BuildHistoryView.this.table.setSorter(newSorter);
        }
      });
    }
    return treeViewerColumn;
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalToolBar(final IToolBarManager manager) {
    //manager.add(this.actionOpenLastBuildDetails);
    manager.add(this.actionOpenBuildInBrowser);
    manager.add(this.actionInvokeBuild);
    manager.add(new Separator());
    manager.add(this.actionReloadJobs);
  }

  private void fillLocalPullDown(final IMenuManager manager) {
    //manager.add(this.actionOpenLastBuildDetails);
    manager.add(this.actionOpenBuildInBrowser);
    manager.add(this.actionInvokeBuild);
    manager.add(new Separator());
    manager.add(this.actionReloadJobs);
  }

  private void makeActions() {

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    this.actionReloadJobs = new ReloadBuildHistoryAction(true);
    this.actionReloadJobs.setEnabled(false);

    //    this.actionOpenLastBuildDetails = new OpenLastBuildAction(this);
    //    this.actionOpenLog = new OpenLogAction(this);
    //    this.actionDeleteJob = new DeleteJobAction(this);

    this.actionOpenBuildInBrowser = new Action("Open with Browser...", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        if (BuildHistoryView.this.selectedBuild != null) {
          JenkinsBuild build = (JenkinsBuild) BuildHistoryView.this.selectedBuild;
          CloudBeesUIPlugin.getDefault().openWithBrowser(build.url);
        }
      }
    };

    this.actionOpenBuildInBrowser.setToolTipText("Open with Browser"); //TODO i18n
    this.actionOpenBuildInBrowser.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BROWSER));
    this.actionOpenBuildInBrowser.setEnabled(false);

    this.actionInvokeBuild = new Action("Run a new build for this job", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        try {
          final JenkinsBuild job = (JenkinsBuild) BuildHistoryView.this.selectedBuild;
          final JenkinsService ns = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(job.url);
          // TODO get job to get the params?
          final Map<String, String> props = null;//CloudBeesUIPlugin.getDefault().getJobPropValues(job.property);
          org.eclipse.core.runtime.jobs.Job sjob = new org.eclipse.core.runtime.jobs.Job("Building job...") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
              try {
                ns.invokeBuild(job.url, props, monitor);
                return org.eclipse.core.runtime.Status.OK_STATUS;
              } catch (CloudBeesException e) {
                //CloudBeesUIPlugin.getDefault().getLogger().error(e);
                return new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.Status.ERROR,
                    CloudBeesUIPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e.getCause());
              }
            }
          };
          sjob.setUser(true);
          sjob.schedule();
        } catch (CancellationException e) {
          // cancelled by user
        }
      }
    };
    this.actionInvokeBuild.setToolTipText("Run a new build for this job"); //TODO i18n
    this.actionInvokeBuild.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_RUN));
    this.actionInvokeBuild.setEnabled(false);

    /*    action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

  }

  @Override
  public void setFocus() {
    this.table.getControl().setFocus();
  }

  public void propertyChange(final PropertyChangeEvent event) {

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())) {
      boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
      if (!jaasEnabled) {
        setInput(null); // all gone
      }
    }

    if (PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())
        || PreferenceConstants.P_EMAIL.equals(event.getProperty())
        || PreferenceConstants.P_PASSWORD.equals(event.getProperty())) {
      try {
        CloudBeesDevUiPlugin.getDefault().showBuildHistory(this.jobUrl, false);
      } catch (CloudBeesException e) {
        //TODO i18n
        CloudBeesUIPlugin.showError("Failed to reload Jenkins jobs!", e);
      }
    }

    if (PreferenceConstants.P_JENKINS_REFRESH_INTERVAL.equals(event.getProperty())
        || PreferenceConstants.P_JENKINS_REFRESH_ENABLED.equals(event.getProperty())) {
      boolean enabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_JENKINS_REFRESH_ENABLED);
      int secs = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getInt(PreferenceConstants.P_JENKINS_REFRESH_INTERVAL);
      if (enabled && secs > 0) {
        //  startRefresher(); // start it if it was disabled by 0 value, do nothing if it was already running
      } else {
        //  stopRefresher();
      }
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    //    stopRefresher();

    disposeImages();

    super.dispose();
  }

  private void disposeImages() {
    Iterator<Image> it = this.stateIcons.values().iterator();
    while (it.hasNext()) {
      Image img = it.next();
      img.dispose();
    }
    this.stateIcons.clear();
  }
}
