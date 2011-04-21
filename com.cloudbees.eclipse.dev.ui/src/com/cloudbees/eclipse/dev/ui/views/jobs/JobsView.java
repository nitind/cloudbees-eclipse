package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
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
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.actions.DeleteJobAction;
import com.cloudbees.eclipse.dev.ui.actions.OpenLastBuildAction;
import com.cloudbees.eclipse.dev.ui.actions.OpenLogAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadBuildHistoryAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadJobsAction;
import com.cloudbees.eclipse.dev.ui.utils.FavouritesUtils;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * View showing jobs for both Jenkins offline installations and JaaS Nectar instances
 *
 * @author ahtik
 */
public class JobsView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.dev.ui.views.jobs.JobsView";

  private TableViewer table;

  protected ReloadJobsAction actionReloadJobs;
  private Action actionInvokeBuild;
  private Action actionOpenJobInBrowser;

  private Action actionOpenLastBuildDetails;
  private ReloadBuildHistoryAction actionOpenBuildHistory;
  private OpenLogAction actionOpenLog;
  private Action actionDeleteJob;
  private Action actionAddFavourite;
  private Action actionRemoveFavourite;

  private final Map<String, Image> stateIcons = new HashMap<String, Image>();

  private JenkinsChangeListener jenkinsChangeListener;

  private JobsContentProvider contentProvider;

  protected Runnable regularRefresher;

  private String viewUrl;

  protected Object selectedJob;

  public JobsView() {
    super();
  }

  public Object getSelectedJob() {
    return this.selectedJob;
  }

  public void setSelectedJob(final Object job) {
    this.selectedJob = job;
    boolean enable = this.selectedJob != null;
    this.actionInvokeBuild.setEnabled(enable);
    this.actionOpenLastBuildDetails.setEnabled(enable);
    this.actionOpenLog.setBuild(this.selectedJob instanceof Job ? ((Job) this.selectedJob).lastBuild : null);
    this.actionOpenLog.setEnabled(enable);
    this.actionDeleteJob.setEnabled(enable);
    this.actionOpenJobInBrowser.setEnabled(enable);
    this.actionOpenBuildHistory.setViewUrl(this.selectedJob instanceof Job ? ((Job) this.selectedJob).url : null);
    this.actionOpenBuildHistory.setEnabled(enable);

    if (this.selectedJob instanceof Job) {
      boolean isFavourite = FavouritesUtils.isFavourite(((Job) this.selectedJob).url);
      JobsView.this.actionAddFavourite.setEnabled(!isFavourite);
      JobsView.this.actionRemoveFavourite.setEnabled(isFavourite);
    } else {
      JobsView.this.actionAddFavourite.setEnabled(false);
      JobsView.this.actionRemoveFavourite.setEnabled(false);
    }
  }

  public ReloadJobsAction getReloadJobsAction() {
    return this.actionReloadJobs;
  }

  protected void setInput(final JenkinsJobsResponse newView) {

    if (newView != null && newView.viewUrl != null) {
      IViewSite site = getViewSite();
      String secId = site.getSecondaryId();
      String servUrl = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(newView.viewUrl).getUrl();
      if (secId != null && servUrl != null && !secId.equals(Long.toString(servUrl.hashCode()))) {
        return; // another view
      }
    }

    if (newView == null || newView.jobs == null) {
      setContentDescription("No jobs available.");
      this.contentProvider.inputChanged(this.table, null, new ArrayList<JenkinsJobsResponse.Job>());
    } else {
      String label = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(newView.viewUrl).getLabel();

      String viewInfo = "";
      if (newView.name != null && newView.name.length() > 0) {
        viewInfo = newView.name + " [";
      }
      setContentDescription(viewInfo + label + (viewInfo.length() > 0 ? "]" : "") + " (" + new Date() + ")");
      setPartName("Build Jobs [" + label + "]");
      this.contentProvider.inputChanged(this.table, null, Arrays.asList(newView.jobs));
    }

    if (newView != null) {
      this.viewUrl = newView.viewUrl;
    } else {
      this.viewUrl = null;
    }

    this.actionReloadJobs.viewUrl = this.viewUrl;

    this.table.refresh();

    boolean reloadable = newView != null;
    this.actionReloadJobs.setEnabled(reloadable);

    if (reloadable) {
      startRefresher();
    } else {
      stopRefresher();
    }
  }

  protected synchronized void stopRefresher() {
    this.regularRefresher = null;
  }

  protected synchronized void startRefresher() {
    if (this.regularRefresher != null) {
      return; // already running
    }

    if (this.viewUrl == null) {
      return; // nothing to refresh anyway
    }

    boolean enabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_JENKINS_REFRESH_ENABLED);
    int secs = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getInt(PreferenceConstants.P_JENKINS_REFRESH_INTERVAL);
    if (!enabled || secs <= 0) {
      return; // disabled
    }

    this.regularRefresher = new Runnable() {

      @Override
      public void run() {
        if (JobsView.this.regularRefresher == null) {
          return;
        }
        try {
          CloudBeesDevUiPlugin.getDefault().showJobs(JobsView.this.actionReloadJobs.viewUrl, false);
        } catch (CloudBeesException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
        } finally {
          if (JobsView.this.regularRefresher != null) { // not already stopped
            boolean enabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.P_JENKINS_REFRESH_ENABLED);
            int secs = CloudBeesUIPlugin.getDefault().getPreferenceStore()
                .getInt(PreferenceConstants.P_JENKINS_REFRESH_INTERVAL);
            if (enabled && secs > 0) {
              PlatformUI.getWorkbench().getDisplay().timerExec(secs * 1000, JobsView.this.regularRefresher);
            } else {
              stopRefresher();
            }
          }
        }
      }
    };

    PlatformUI.getWorkbench().getDisplay().timerExec(secs * 1000, this.regularRefresher);
  }

  class NameSorter extends ViewerSorter {

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {

      if (e1 instanceof JenkinsJobsResponse.Job && e2 instanceof JenkinsJobsResponse.Job) {
        JenkinsJobsResponse.Job j1 = (JenkinsJobsResponse.Job) e1;
        JenkinsJobsResponse.Job j2 = (JenkinsJobsResponse.Job) e2;

        String displayName1 = j1.displayName;
        String displayName2 = j2.displayName;
        if (displayName1 != null && displayName2 != null) {
          return displayName1.toLowerCase().compareTo(displayName2.toLowerCase());
        }

      }

      return super.compare(viewer, e1, e2);
    }

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

    TableViewerColumn statusCol = createColumn("S", 22, JobSorter.STATE, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();

        String key = job.color;

        /*        ImageData[] imageDatas = new ImageLoader().load(new FileInputStream("myAnimated.gif"));
                Image[] images = new Image[imageDatas.length];
                for (int n = 0; n < imageDatas.length; n++) {
                  // images[n] = new Image(myTable.getDislay(), imageDatas[n]);
                }
         */
        if (job.color != null && job.color.contains("_")) {
          key = job.color.substring(0, job.color.indexOf("_"));
        }

        Image img = JobsView.this.stateIcons.get(key);

        if (img != null) {
          cell.setText("");
          cell.setImage(img);
        } else {
          cell.setImage(null);
          cell.setText(job.color);
        }

      }

      @Override
      public String getToolTipText(final Object element) {
        JenkinsJobsResponse.Job job = (Job) element;
        return job.color;
      }

    });
    statusCol.getColumn().setToolTipText("Status");

    //TODO i18n
    TableViewerColumn namecol = createColumn("Job", 250, JobSorter.JOB, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        String val = job.getDisplayName();
        if (job.inQueue != null && job.inQueue) {
          val = val + " (in queue)";
        } else if (job.color != null && job.color.indexOf('_') > 0) {
          val = val + " (running)";
        }
        cell.setText(val);

      }
    });

    createColumn("Build stability", 250, JobSorter.BUILD_STABILITY, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {

        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();

        cell.setText("");
        cell.setImage(null);

        try {
          if (job.healthReport != null) {
            for (int h = 0; h < job.healthReport.length; h++) {
              String icon = job.healthReport[h].iconUrl;
              String desc = job.healthReport[h].description;
              String matchStr = "Build stability: ";
              if (desc != null && desc.startsWith(matchStr)) {
                cell.setText(" " + desc.substring(matchStr.length()));
                cell.setImage(CloudBeesDevUiPlugin.getImage(CBImages.IMG_HEALTH_PREFIX + CBImages.IMG_16 + icon));
              }
            }
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }

      }
    });

    /*    createColumn("Last build result", 100, new CellLabelProvider() {
          public void update(ViewerCell cell) {
            JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
            cell.setText("n/a");
          }
        });

        createColumn("Last Testsuite result", 100, new CellLabelProvider() {
          public void update(ViewerCell cell) {
            JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
            cell.setText("n/a");
          }
        });
     */
    createColumn("Last build", 150, JobSorter.LAST_BUILD, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastBuild));
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });
    createColumn("Last success", 150, JobSorter.LAST_SUCCESS, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastSuccessfulBuild));
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });
    createColumn("Last failure", 150, JobSorter.LAST_FAILURE, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastFailedBuild));
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });

    /*    createColumn("Comment", 100, new CellLabelProvider() {
          public void update(ViewerCell cell) {
            JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
            cell.setText("n/a");
          }
        });
     */

    this.contentProvider = new JobsContentProvider();

    this.table.setContentProvider(this.contentProvider);
    //viewer.setLabelProvider(new InstanceLabelProvider());

    this.table.setSorter(new JobSorter(JobSorter.JOB));

    //table.setComparator(new ViewerComparator());

    this.table.setInput(getViewSite());
    /*
        table.addFilter(new ViewerFilter() {
          @Override
          public boolean select(Viewer viewer, Object parentElement, Object element) {
            return true;
          }
        });*/

    this.table.addOpenListener(new IOpenListener() {

      @Override
      public void open(final OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof IStructuredSelection) {
          Object el = ((IStructuredSelection) sel).getFirstElement();
          if (el instanceof JenkinsJobsResponse.Job) {
            CloudBeesDevUiPlugin.getDefault().showBuildForJob(((JenkinsJobsResponse.Job) el));
          }
        }

      }

    });

    this.table.getTable().setSortColumn(namecol.getColumn());
    this.table.getTable().setSortDirection(SWT.DOWN);

    makeActions();
    contributeToActionBars();

    MenuManager popupMenu = new MenuManager();

    popupMenu.add(this.actionOpenLastBuildDetails);
    popupMenu.add(this.actionOpenLog);
    popupMenu.add(this.actionOpenBuildHistory);
    popupMenu.add(new Separator());
    popupMenu.add(this.actionOpenJobInBrowser);
    popupMenu.add(this.actionInvokeBuild);
    popupMenu.add(new Separator());
    popupMenu.add(this.actionDeleteJob);
    popupMenu.add(new Separator());
    popupMenu.add(this.actionAddFavourite);
    popupMenu.add(this.actionRemoveFavourite);
    popupMenu.add(new Separator());
    popupMenu.add(this.actionReloadJobs);

    Menu menu = popupMenu.createContextMenu(this.table.getTable());
    this.table.getTable().setMenu(menu);

    this.table.addPostSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(final SelectionChangedEvent event) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        setSelectedJob(sel.getFirstElement());
      }
    });

    this.jenkinsChangeListener = new JenkinsChangeListener() {
      @Override
      public void activeJobViewChanged(final JenkinsJobsResponse newView) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            JobsView.this.setInput(newView);
          }
        });
      }

      @Override
      public void activeJobHistoryChanged(final JenkinsJobAndBuildsResponse newView) {
      }

      @Override
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
      unit = Utils.humanReadableTime((System.currentTimeMillis() - build.timestamp.longValue()));
    }
    String timeComp = build.duration != null ? /*", " + */unit + " ago" : "";
    //String buildComp = build.number != null ? "#" + build.number : "n/a";
    String buildComp = build.number != null ? " #" + build.number : "n/a";
    return timeComp + buildComp;
  }

  private void initImages() {

    String[] icons = { "blue", "red", "yellow", "grey" };

    for (int i = 0; i < icons.length; i++) {
      //TODO Refactor to use CBImages!
      Image img = ImageDescriptor.createFromURL(
          CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/" + icons[i] + ".gif"))
          .createImage();
      this.stateIcons.put(icons[i], img);
    }

    this.stateIcons.put(
        "disabled",
        ImageDescriptor.createFromURL(
            CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/grey.gif"))
            .createImage());
    this.stateIcons.put(
        "aborted",
        ImageDescriptor.createFromURL(
            CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/stop.gif"))
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

          if (JobsView.this.table.getTable().getSortColumn().equals(treeViewerColumn.getColumn())
              && JobsView.this.table.getTable().getSortDirection() == SWT.DOWN) {
            newOrder = SWT.UP;
          }

          JobsView.this.table.getTable().setSortColumn(treeViewerColumn.getColumn());
          JobsView.this.table.getTable().setSortDirection(newOrder);
          JobSorter newSorter = new JobSorter(sortCol);
          newSorter.setDirection(newOrder);
          JobsView.this.table.setSorter(newSorter);
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
    manager.add(this.actionOpenJobInBrowser);
    manager.add(this.actionInvokeBuild);
    manager.add(new Separator());
    manager.add(this.actionReloadJobs);
  }

  private void fillLocalPullDown(final IMenuManager manager) {
    //manager.add(this.actionOpenLastBuildDetails);
    manager.add(this.actionOpenJobInBrowser);
    manager.add(this.actionInvokeBuild);
    manager.add(new Separator());
    manager.add(this.actionReloadJobs);
  }

  private void makeActions() {

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    this.actionReloadJobs = new ReloadJobsAction();
    this.actionReloadJobs.setEnabled(false);

    this.actionOpenLastBuildDetails = new OpenLastBuildAction(this);
    this.actionOpenBuildHistory = new ReloadBuildHistoryAction(false);
    this.actionOpenLog = new OpenLogAction();
    this.actionDeleteJob = new DeleteJobAction(this);

    this.actionOpenJobInBrowser = new Action("Open with Browser...", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        if (JobsView.this.selectedJob != null) {
          JenkinsJobsResponse.Job job = (Job) JobsView.this.selectedJob;
          CloudBeesUIPlugin.getDefault().openWithBrowser(job.url);
        }
      }
    };

    this.actionOpenJobInBrowser.setToolTipText("Open with Browser"); //TODO i18n
    this.actionOpenJobInBrowser.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_BROWSER));
    this.actionOpenJobInBrowser.setEnabled(false);

    this.actionInvokeBuild = new Action("Run a new build for this job", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        try {
          final JenkinsJobsResponse.Job job = (Job) JobsView.this.selectedJob;
          final JenkinsService ns = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(job.url);
          final Map<String, String> props = CloudBeesUIPlugin.getDefault().getJobPropValues(job.property);
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

    this.actionAddFavourite = new Action("Add to Favourites") {
      @Override
      public void run() {
        final JenkinsJobsResponse.Job job = (Job) JobsView.this.selectedJob;
        FavouritesUtils.addFavourite(job.url, job.name);
        JobsView.this.actionAddFavourite.setEnabled(false);
        JobsView.this.actionRemoveFavourite.setEnabled(true);
      };
    };

    this.actionRemoveFavourite = new Action("Remove from Favourites") {
      @Override
      public void run() {
        final JenkinsJobsResponse.Job job = (Job) JobsView.this.selectedJob;
        FavouritesUtils.removeFavourite(job.url);
        JobsView.this.actionAddFavourite.setEnabled(true);
        JobsView.this.actionRemoveFavourite.setEnabled(false);
      };
    };
    /*    action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

  }

  @Override
  public void setFocus() {
    this.table.getControl().setFocus();
  }

  @Override
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
        CloudBeesDevUiPlugin.getDefault().showJobs(this.viewUrl, false);
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
        startRefresher(); // start it if it was disabled by 0 value, do nothing if it was already running
      } else {
        stopRefresher();
      }
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    stopRefresher();

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
