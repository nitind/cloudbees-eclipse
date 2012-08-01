package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CBRemoteChangeAdapter;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.JobViewGeneric;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.View;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.actions.DeleteJobAction;
import com.cloudbees.eclipse.dev.ui.actions.InvokeBuildAction;
import com.cloudbees.eclipse.dev.ui.actions.OpenBuildAction;
import com.cloudbees.eclipse.dev.ui.actions.OpenLogAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadBuildHistoryAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadJobsAction;
import com.cloudbees.eclipse.dev.ui.views.forge.ForgeTreeView;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * View showing jobs for both Jenkins offline installations and JaaS Nectar instances
 * 
 * @author ahtik
 */
public class JobsView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.dev.ui.views.jobs.JobsView";

  private TreeViewer treeViewer;

  protected ReloadJobsAction actionReloadJobs;
  private InvokeBuildAction actionInvokeBuild;
  private Action actionOpenJobInBrowser;

  private OpenBuildAction actionOpenLastBuildDetails;
  private ReloadBuildHistoryAction actionOpenBuildHistory;
  private OpenLogAction actionOpenLog;
  private Action actionDeleteJob;

  private final Map<String, Image> stateIcons = new HashMap<String, Image>();

  private CBRemoteChangeListener jenkinsChangeListener;

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
    this.actionInvokeBuild.setJob(this.selectedJob);
    this.actionOpenLastBuildDetails.setBuild(this.selectedJob instanceof Job ? ((Job) this.selectedJob).lastBuild
        : null);
    this.actionOpenLog.setBuild(this.selectedJob instanceof Job ? ((Job) this.selectedJob).lastBuild : null);
    this.actionDeleteJob.setEnabled(this.selectedJob instanceof Job && ((Job) this.selectedJob).color != null);
    this.actionOpenJobInBrowser.setEnabled(enable);
    this.actionOpenBuildHistory
        .setViewUrl(this.selectedJob instanceof Job && ((Job) this.selectedJob).color != null ? ((Job) this.selectedJob).url
            : null);

    //FIXME color!=null is currently the only known way to know if this job is not a folder. 
    this.actionOpenBuildHistory.setEnabled(this.selectedJob instanceof Job && ((Job) this.selectedJob).color != null);

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

    if (newView == null || (newView.jobs == null && newView.views == null)) {
      String post = "";
      if (newView.name != null && newView.name.length() > 0) {
        post = " for " + newView.name;
      }
      setContentDescription("No jobs available" + post);
      this.contentProvider.inputChanged(this.treeViewer, null, new ArrayList<JenkinsJobsResponse.JobViewGeneric>());
    } else {
      JenkinsService ss = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(newView.viewUrl);
      String label = ss.getLabel();

      String viewInfo = "";
      String post = "";
      if (newView.name != null && newView.name.length() > 0 && label != null && !newView.name.equals(label)) {
        viewInfo = newView.name + " [";
        post = "#" + newView.name;
      }
      setContentDescription(viewInfo + label + (viewInfo.length() > 0 ? "]" : "") + " (" + new Date() + ")");
      setPartName("Build Jobs [" + label + post + "]");

      List<JenkinsJobsResponse.JobViewGeneric> reslist = new ArrayList<JobViewGeneric>();

      // Also add views if it's not the main url
      if (!newView.viewUrl.equals(ss.getUrl() + "/") && newView.views != null) {

        for (View view : newView.views) {
          if (view.url != null && (newView.primaryView == null || !view.url.equals(newView.primaryView.url))) {
            reslist.add(view);
          }
        }

      }

      if (newView.jobs != null) {
        reslist.addAll(Arrays.asList(newView.jobs));
      }
      this.contentProvider.inputChanged(this.treeViewer, null, reslist);

    }

    if (newView != null) {
      this.viewUrl = newView.viewUrl;
    }
    this.actionReloadJobs.viewUrl = this.viewUrl;
    this.actionReloadJobs.treeViewer = this.treeViewer;

    // Preserve the expended state and refresh
    TreePath[] paths = treeViewer.getExpandedTreePaths();
    this.treeViewer.refresh();
    treeViewer.setExpandedTreePaths(paths);

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

      if (e1 instanceof JenkinsJobsResponse.JobViewGeneric && e2 instanceof JenkinsJobsResponse.JobViewGeneric) {
        JenkinsJobsResponse.JobViewGeneric j1 = (JenkinsJobsResponse.JobViewGeneric) e1;
        JenkinsJobsResponse.JobViewGeneric j2 = (JenkinsJobsResponse.JobViewGeneric) e2;

        String displayName1 = j1.getName();
        String displayName2 = j2.getName();
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

    this.treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
    treeViewer.getTree().setHeaderVisible(true);

    initColumns();

    this.contentProvider = new JobsContentProvider();
    this.treeViewer.setContentProvider(this.contentProvider);
    this.treeViewer.setSorter(new JobSorter(JobSorter.JOB));
    this.treeViewer.setInput(getViewSite());
    this.treeViewer.addOpenListener(new IOpenListener() {

      @Override
      public void open(final OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof IStructuredSelection) {
          Object el = ((IStructuredSelection) sel).getFirstElement();
          if (el instanceof JenkinsJobsResponse.Job) {
            Job job = (JenkinsJobsResponse.Job) el;

            //assuming it's a folder..
            if (job.color == null || job.color.length() == 0) {
              JobsView.this.toggle(el);
              return; // do nothing
            } else {
              CloudBeesDevUiPlugin.getDefault().showBuildForJob(job);
            }

          }
          
          if (el instanceof JenkinsJobsResponse.View) {
            JobsView.this.toggle(el);
            return; // do nothing
          }
          
        }

      }

    });

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
    popupMenu.add(this.actionReloadJobs);
    Menu menu = popupMenu.createContextMenu(treeViewer.getTree());
    treeViewer.getTree().setMenu(menu);
    treeViewer.getTree().setSortDirection(SWT.DOWN);

    this.treeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(final SelectionChangedEvent event) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        setSelectedJob(sel.getFirstElement());
      }
    });

    this.jenkinsChangeListener = new CBRemoteChangeAdapter() {
      @Override
      public void activeJobViewChanged(final JenkinsJobsResponse newView) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            JobsView.this.setInput(newView);
          }
        });
      }
    };

    CloudBeesUIPlugin.getDefault().addCBRemoteChangeListener(this.jenkinsChangeListener);
  }

  protected void toggle(Object el) {
    boolean exp = treeViewer.getExpandedState(el);
    if (exp) {
      treeViewer.collapseToLevel(el, 1);
    } else {
      treeViewer.expandToLevel(el, 1);
    }
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
        "folder",
        ImageDescriptor.createFromURL(
            CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/folder.gif"))
            .createImage());

    this.stateIcons.put(
        "aborted",
        ImageDescriptor.createFromURL(
            CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/stop.gif"))
            .createImage());
  }

  /* private TreeViewerColumn createColumn(final String colName, final int width, ColumnLabelProvider labelProvider) {
     return createColumn(colName, width, -1, labelProvider);
   }*/

  private TreeViewerColumn createColumn(final String colName, final int width, final int sortCol,
      ColumnLabelProvider labelProvider) {
    final TreeViewerColumn col = new TreeViewerColumn(treeViewer, SWT.NONE);

    col.setLabelProvider(labelProvider);

    if (width > 0) {
      col.getColumn().setWidth(width);
    }

    col.getColumn().setText(colName);
    col.getColumn().setMoveable(true);

    if (sortCol >= 0) {

      col.getColumn().addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(final SelectionEvent e) {

          int newOrder = SWT.DOWN;

          if (treeViewer.getTree().getSortColumn().equals(col) && treeViewer.getTree().getSortDirection() == SWT.DOWN) {
            newOrder = SWT.UP;
          }

          treeViewer.getTree().setSortColumn(col.getColumn());
          treeViewer.getTree().setSortDirection(newOrder);
          JobSorter newSorter = new JobSorter(sortCol);
          newSorter.setDirection(newOrder);
          JobsView.this.treeViewer.setSorter(newSorter);
        }
      });
    }
    return col;
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
    manager.add(this.actionOpenLastBuildDetails);
    manager.add(this.actionOpenLog);
  }

  private void makeActions() {

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    this.actionReloadJobs = new ReloadJobsAction();
    this.actionReloadJobs.setEnabled(false);

    this.actionOpenLastBuildDetails = new OpenBuildAction(true);
    this.actionOpenBuildHistory = new ReloadBuildHistoryAction(false);
    this.actionOpenLog = new OpenLogAction();
    this.actionDeleteJob = new DeleteJobAction(this);

    this.actionOpenJobInBrowser = new Action("Open with Browser...", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
      @Override
      public void run() {
        if (JobsView.this.selectedJob != null && JobsView.this.selectedJob instanceof JobViewGeneric) {
          JenkinsJobsResponse.JobViewGeneric job = (JobViewGeneric) JobsView.this.selectedJob;
          CloudBeesUIPlugin.getDefault().openWithBrowser(job.getUrl());
        }
      }
    };

    this.actionOpenJobInBrowser.setToolTipText("Open with Browser"); //TODO i18n
    this.actionOpenJobInBrowser.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_BROWSER));
    this.actionOpenJobInBrowser.setEnabled(false);

    this.actionInvokeBuild = new InvokeBuildAction();

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }

  @Override
  public void setFocus() {
    this.treeViewer.getControl().setFocus();
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
    CloudBeesUIPlugin.getDefault().removeCBRemoteChangeListener(this.jenkinsChangeListener);
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

  private void initColumns() {
    //TODO i18n
    TreeViewerColumn namecol = createColumn("Job", 250, JobSorter.JOB, new ColumnLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JobViewGeneric vg = (JobViewGeneric) cell.getViewerRow().getElement();
        if (vg instanceof JenkinsJobsResponse.Job) {
          JenkinsJobsResponse.Job job = (Job) vg;
          String val = job.getDisplayName();
          if (job.inQueue != null && job.inQueue) {
            val = val + " (in queue)";
          } else if (job.color != null && job.color.indexOf('_') > 0) {
            val = val + " (running)";
          }
          cell.setText(val);

          String key = job.color;
          if (job.color != null && job.color.contains("_")) {
            key = job.color.substring(0, job.color.indexOf("_"));
          }

          // Assuming for now that these are all folders as thers's no API to tell the difference
          if (job.color == null) {
            key = "folder";
          }

          Image img = JobsView.this.stateIcons.get(key);
          if (img != null) {
            cell.setImage(img);
          } else if (job.color != null) {
            cell.setText(val + "[" + job.color + "]");
          }

        }

        if (vg instanceof View) {
          cell.setImage(CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_VIEWR2));
          cell.setText(vg.getName());
        }

      }
    });

    createColumn("Build stability", 250, JobSorter.BUILD_STABILITY, new ColumnLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        
        Object element = cell.getViewerRow().getElement();
        if ((element instanceof JenkinsJobsResponse.Job && ((JenkinsJobsResponse.Job) element).color == null)
            || (element instanceof JenkinsJobsResponse.View)) {
          cell.setText("");
          cell.setImage(null);
          return;
        }
        
        JenkinsJobsResponse.Job job = (Job) element;

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
                cell.setImage(CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_HEALTH_PREFIX + CBDEVImages.IMG_16 + icon));
              }
            }
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }

      }
    });

    createColumn("Last build", 150, JobSorter.LAST_BUILD, new ColumnLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        Object element = cell.getViewerRow().getElement();
        if ((element instanceof JenkinsJobsResponse.Job && ((JenkinsJobsResponse.Job) element).color == null)
            || (element instanceof JenkinsJobsResponse.View)) {
          cell.setText("");
          cell.setImage(null);
          return;
        }
        
        JenkinsJobsResponse.Job job = (Job) element;

        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastBuild));
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });
    createColumn("Last success", 150, JobSorter.LAST_SUCCESS, new ColumnLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        Object element = cell.getViewerRow().getElement();
        if ((element instanceof JenkinsJobsResponse.Job && ((JenkinsJobsResponse.Job) element).color == null)
            || (element instanceof JenkinsJobsResponse.View)) {
          cell.setText("");
          cell.setImage(null);
          return;
        }
        
        JenkinsJobsResponse.Job job = (Job) element;

        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastSuccessfulBuild));
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });
    createColumn("Last failure", 150, JobSorter.LAST_FAILURE, new ColumnLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        Object element = cell.getViewerRow().getElement();
        if ((element instanceof JenkinsJobsResponse.Job && ((JenkinsJobsResponse.Job) element).color == null)
            || (element instanceof JenkinsJobsResponse.View)) {
          cell.setText("");
          cell.setImage(null);
          return;
        }
        
        JenkinsJobsResponse.Job job = (Job) element;

        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastFailedBuild));
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });

  }

}
