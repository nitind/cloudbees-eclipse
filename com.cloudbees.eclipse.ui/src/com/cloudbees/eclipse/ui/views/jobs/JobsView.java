package com.cloudbees.eclipse.ui.views.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job.Build;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.cloudbees.eclipse.ui.internal.actions.ReloadJobsAction;

/**
 * View showing jobs for both Jenkins offline installations and JaaS Nectar instances
 * 
 * @author ahtik
 */
public class JobsView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.ui.views.JobsView";

  private TableViewer table;

  private final Logger log = CloudBeesUIPlugin.getDefault().getLogger();

  private ReloadJobsAction actionReloadJobs; // Reload JaaS instances
  private Action actionInvokeBuild; // Invoke Build
  //private Action actionOpenLogs; // Open Logs
  private Action actionOpenJobInBrowser; // Open Open Job in Browser

  private final Map<String, Image> stateIcons = new HashMap<String, Image>();

  private JenkinsChangeListener jenkinsChangeListener;

  private JobsContentProvider contentProvider;

  private String serviceUrl;
  private String viewUrl;

  protected Object selectedJob;

  public JobsView() {
    super();
  }

  protected void setInput(JenkinsJobsResponse newView) {

    if (newView == null || newView.jobs == null) {
      setContentDescription("No jobs available.");
      contentProvider.inputChanged(table, null, new ArrayList<JenkinsJobsResponse.Job>());
    } else {
      String label = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(newView.serviceUrl).getLabel();

      String viewInfo = "";
      if (newView.name != null && newView.name.length() > 0) {
        viewInfo = newView.name + " [";
      }
      setContentDescription(viewInfo + label + (viewInfo.length() > 0 ? "]" : "") + " (" + new Date() + ")");
      contentProvider.inputChanged(table, null, Arrays.asList(newView.jobs));
    }

    if (newView != null) {
      serviceUrl = newView.serviceUrl;
      viewUrl = newView.viewUrl;
    } else {
      serviceUrl = null;
      viewUrl = null;
    }

    actionReloadJobs.serviceUrl = serviceUrl;
    actionReloadJobs.viewUrl = viewUrl;

    table.refresh();
  }

  class NameSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {

      if (e1 instanceof JenkinsJobsResponse.Job && e2 instanceof JenkinsJobsResponse.Job) {
        JenkinsJobsResponse.Job j1 = (JenkinsJobsResponse.Job) e1;
        JenkinsJobsResponse.Job j2 = (JenkinsJobsResponse.Job) e2;

        if (j1.displayName != null && j2.displayName != null) {
          return j1.displayName.toLowerCase().compareTo(j2.displayName.toLowerCase());
        }

      }

      return super.compare(viewer, e1, e2);
    }

  }

  public void createPartControl(Composite parent) {

    initImages();

    table = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION
    /*SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL*/);
    //Tree tree = viewer.getTree();
    //tree.setHeaderVisible(true);

    //table.getTable().setLinesVisible(true);
    table.getTable().setHeaderVisible(true);

    createColumn("S", 20, JobSorter.STATE, new CellLabelProvider() {
      public void update(ViewerCell cell) {
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

        Image img = stateIcons.get(key);

        if (img != null) {
          cell.setText("");
          cell.setImage(img);
        } else {
          cell.setImage(null);
          cell.setText(job.color);
        }

      }


    });

    //TODO i18n
    TableViewerColumn namecol = createColumn("Job", 250, JobSorter.JOB, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        String val = job.displayName;
        if (job.inQueue) {
          val = val + " (in queue)";
        } else if (job.color != null && job.color.indexOf('_') > 0) {
          val = val + " (running)";
        }
        cell.setText(val);

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
      public void update(ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastBuild));
        } catch (Throwable t) {
          cell.setText("n/a");
        }
      }
    });
    createColumn("Last success", 150, JobSorter.LAST_SUCCESS, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastSuccessfulBuild));
        } catch (Throwable t) {
          cell.setText("n/a");
        }
      }
    });
    createColumn("Last failure", 105, JobSorter.LAST_FAILURE, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        JenkinsJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastFailedBuild));
        } catch (Throwable t) {
          cell.setText("n/a");
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

    contentProvider = new JobsContentProvider();

    table.setContentProvider(contentProvider);
    //viewer.setLabelProvider(new InstanceLabelProvider());

    table.setSorter(new JobSorter(JobSorter.JOB));

    //table.setComparator(new ViewerComparator());

    table.setInput(getViewSite());
    /*
        table.addFilter(new ViewerFilter() {
          @Override
          public boolean select(Viewer viewer, Object parentElement, Object element) {
            return true;
          }
        });*/

    table.addOpenListener(new IOpenListener() {

      public void open(OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof IStructuredSelection) {
          Object el = ((IStructuredSelection) sel).getFirstElement();
          if (el instanceof JenkinsJobsResponse.Job) {
            CloudBeesUIPlugin.getDefault().showBuildForJob(((JenkinsJobsResponse.Job) el));
          }
        }

      }

    });

    table.getTable().setSortColumn(namecol.getColumn());
    table.getTable().setSortDirection(SWT.DOWN);

    makeActions();
    contributeToActionBars();

    table.addPostSelectionChangedListener(new ISelectionChangedListener() {
      
      public void selectionChanged(SelectionChangedEvent event) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        JobsView.this.selectedJob = sel.getFirstElement();
        boolean enable = sel.getFirstElement()!=null;
        actionInvokeBuild.setEnabled(enable);
        actionOpenJobInBrowser.setEnabled(enable);
      }       
    });
    

    jenkinsChangeListener = new JenkinsChangeListener() {
      public void activeJobViewChanged(final JenkinsJobsResponse newView) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            JobsView.this.setInput(newView);
          }
        });
      }

      public void jenkinsChanged(List<JenkinsInstanceResponse> instances) {
      }
    };

    CloudBeesUIPlugin.getDefault().addJenkinsChangeListener(jenkinsChangeListener);
  }

  protected String formatBuildInfo(Build build) {
    String unit = "";
    if (build.duration != null) {
      //TODO Implement proper human-readable duration conversion, consider using the same conversion rules that Jenkins uses
      //CloudBeesUIPlugin.getDefault().getLogger().info("DURATION: " + build.timestamp);      
      unit = Utils.humanReadableTime((System.currentTimeMillis() - build.timestamp.longValue()));
    }
    String timeComp = build.duration != null ? ", " + unit + " ago" : "";
    String buildComp = build.number != null ? "#" + build.number : "n/a";
    return buildComp + timeComp;
  }

  private void initImages() {

    String[] icons = { "blue", "red", "yellow" };

    for (int i = 0; i < icons.length; i++) {
      //TODO Refactor to use CBImages!
      Image img = ImageDescriptor.createFromURL(
          CloudBeesUIPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/" + icons[i] + ".gif"))
          .createImage();
      stateIcons.put(icons[i], img);
    }

    stateIcons.put(
        "disabled",
        ImageDescriptor.createFromURL(
            CloudBeesUIPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/grey.gif"))
            .createImage());

  }

  private TableViewerColumn createColumn(final String colName, int width, CellLabelProvider cellLabelProvider) {
    return createColumn(colName, width, -1, cellLabelProvider);
  }

  private TableViewerColumn createColumn(final String colName, int width, final int sortCol,
      CellLabelProvider cellLabelProvider) {
    final TableViewerColumn treeViewerColumn = new TableViewerColumn(table, SWT.NONE);
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
        public void widgetSelected(SelectionEvent e) {

          int newOrder = SWT.DOWN;

          if (table.getTable().getSortColumn().equals(treeViewerColumn.getColumn())
              && table.getTable().getSortDirection() == SWT.DOWN) {
            newOrder = SWT.UP;
          }

          table.getTable().setSortColumn(treeViewerColumn.getColumn());
          table.getTable().setSortDirection(newOrder);
          JobSorter newSorter = new JobSorter(sortCol);
          newSorter.setDirection(newOrder);
          table.setSorter(newSorter);
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

  private void fillLocalToolBar(IToolBarManager manager) {
    manager.add(actionReloadJobs);
    manager.add(new Separator());
    manager.add(actionInvokeBuild);
    manager.add(actionOpenJobInBrowser);
  }

  private void fillLocalPullDown(IMenuManager manager) {
    manager.add(actionReloadJobs);
    manager.add(new Separator());
    manager.add(actionInvokeBuild);
    manager.add(actionOpenJobInBrowser);
  }

  private void makeActions() {

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    actionReloadJobs = new ReloadJobsAction();

    actionOpenJobInBrowser = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
        public void run() {
        if (JobsView.this.selectedJob != null) {
          JenkinsJobsResponse.Job job = (Job) JobsView.this.selectedJob;
          CloudBeesUIPlugin.getDefault().openWithBrowser(job.url);
        }
        }
      };

    actionOpenJobInBrowser.setToolTipText("Open with Browser"); //TODO i18n
    actionOpenJobInBrowser.setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.IMG_BROWSER));
    actionOpenJobInBrowser.setEnabled(false);


    actionInvokeBuild = new Action("", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS) { //$NON-NLS-1$
        public void run() {

        //TODO Add monitor
          try {
          JenkinsJobsResponse.Job job = (Job) JobsView.this.selectedJob;
          JenkinsService ns = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(job.url);
          ns.invokeBuild(job.url, new NullProgressMonitor());
          } catch (CloudBeesException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
          }
        }
      };
    actionInvokeBuild.setToolTipText("Run a new build for this job"); //TODO i18n
    actionInvokeBuild.setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.IMG_RUN));
    actionInvokeBuild.setEnabled(false);


    /*    action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    
  }


  public void setFocus() {
    table.getControl().setFocus();
  }

  public void propertyChange(PropertyChangeEvent event) {

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
        CloudBeesUIPlugin.getDefault().showJobs(serviceUrl, viewUrl);
      } catch (CloudBeesException e) {
        //TODO i18n
        CloudBeesUIPlugin.showError("Failed to reload Jenkins jobs!", e);
      }
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(jenkinsChangeListener);
    jenkinsChangeListener = null;

    disposeImages();

    super.dispose();
  }

  private void disposeImages() {
    Iterator<Image> it = stateIcons.values().iterator();
    while (it.hasNext()) {
      Image img = (Image) it.next();
      img.dispose();
    }
    stateIcons.clear();
  }


}
