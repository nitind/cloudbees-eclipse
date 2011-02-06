package com.cloudbees.eclipse.ui.views.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
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
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.NectarChangeListener;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse.Job;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse.Job.Build;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * View showing jobs for both NectarInfo offline installations and JaaS NectarInfo instances
 * 
 * @author ahtik
 */
public class JobsView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.ui.views.JobsView";

  private TableViewer table;

  private final Logger log = CloudBeesUIPlugin.getDefault().getLogger();

  private Action action1; // Configure Account
  private Action action2; // Attach NectarInfo
  private Action action3; // Reload Forge repositories
  private Action action4; // Reload JaaS instances

  private final Map<String, Image> stateIcons = new HashMap<String, Image>();

  private NectarChangeListener nectarChangeListener;

  private JobsContentProvider contentProvider;

  private String serviceUrl;
  private String viewUrl;

  public JobsView() {
    super();
  }

  protected void setInput(NectarJobsResponse newView) {
    System.out.println("New Jobs view: " + newView);

    if (newView == null || newView.jobs == null) {
      setContentDescription("No jobs available.");
      contentProvider.setJobs(new ArrayList<NectarJobsResponse.Job>());
    } else {
      String label = CloudBeesUIPlugin.getDefault().getNectarServiceForUrl(newView.serviceUrl).getLabel();

      String viewInfo = "";
      if (newView.name != null && newView.name.length() > 0) {
        viewInfo = newView.name + " [";
      }
      setContentDescription(viewInfo + label + (viewInfo.length() > 0 ? "]" : "") + " (" + new Date() + ")");
      contentProvider.setJobs(Arrays.asList(newView.jobs));
    }

    if (newView != null) {
      serviceUrl = newView.serviceUrl;
      viewUrl = newView.viewUrl;
    } else {
      serviceUrl = null;
      viewUrl = null;
    }

    enableJaasButton();

    table.refresh();
  }

  class NameSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {

      if (e1 instanceof NectarJobsResponse.Job && e2 instanceof NectarJobsResponse.Job) {
        NectarJobsResponse.Job j1 = (NectarJobsResponse.Job) e1;
        NectarJobsResponse.Job j2 = (NectarJobsResponse.Job) e2;

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
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();

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
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
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
            NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
            cell.setText("n/a");//TODO
          }
        });

        createColumn("Last Testsuite result", 100, new CellLabelProvider() {
          public void update(ViewerCell cell) {
            NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
            cell.setText("n/a");//TODO
          }
        });
    */
    createColumn("Last build", 150, JobSorter.LAST_BUILD, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastBuild));
        } catch (Throwable t) {
          cell.setText("n/a");
        }
      }
    });
    createColumn("Last success", 150, JobSorter.LAST_SUCCESS, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastSuccessfulBuild));
        } catch (Throwable t) {
          cell.setText("n/a");
        }
      }
    });
    createColumn("Last failure", 105, JobSorter.LAST_FAILURE, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(JobsView.this.formatBuildInfo(job.lastFailedBuild));
        } catch (Throwable t) {
          cell.setText("n/a");
        }
      }
    });

    /*    createColumn("Comment", 100, new CellLabelProvider() {
          public void update(ViewerCell cell) {
            NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
            cell.setText("n/a");//TODO
          }
        });
    */

    contentProvider = new JobsContentProvider(getViewSite());

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
          if (el instanceof NectarJobsResponse.Job) {
            CloudBeesUIPlugin.getDefault().showBuildForJob(((NectarJobsResponse.Job) el));
          }
        }

      }

    });

    table.getTable().setSortColumn(namecol.getColumn());
    table.getTable().setSortDirection(SWT.DOWN);

    makeActions();
    contributeToActionBars();

    nectarChangeListener = new NectarChangeListener() {
      public void activeJobViewChanged(NectarJobsResponse newView) {
        System.out.println("Reloading items");//FIXME remove!
        JobsView.this.setInput(newView);
      }
    };

    CloudBeesUIPlugin.getDefault().addNectarChangeListener(nectarChangeListener);

  }

  private void configureSorting(int state) {
    // TODO Auto-generated method stub

  }

  protected String formatBuildInfo(Build build) {
    String unit = "";
    if (build.duration != null) {
      //TODO Implement proper human-readable duration conversion, consider using the same conversion rules that Jenkins uses
      //System.out.println("DURATION: " + build.timestamp);
      long mins = (System.currentTimeMillis() - build.timestamp.longValue()) / (60L * 1000);
      long hr = mins / 60;
      if (mins < 60) {
        unit = mins + " min";
        if (mins > 1) {
          unit = unit + "s";
        }
      } else if (mins < 60 * 60 * 24) {
        //long newmins = mins - (hr * 60);
        unit = hr + " hr" + (hr > 1 ? "s" : "");/* + " " + newmins + " min" + (newmins > 1 ? "s" : "");*/
      } else {
        long days = hr / 24L;
        unit = days + " day" + (days > 1 ? "s" : "")/* + ", " + hr + " hr" + (hr > 1 ? "s" : "")*/;
      }

    }
    String timeComp = build.duration != null ? ", " + unit + " ago" : "";
    String buildComp = build.number != null ? "#" + build.number : "n/a";
    return buildComp + timeComp;
  }

  private void initImages() {

    String[] icons = { "blue", "red", "yellow" };

    for (int i = 0; i < icons.length; i++) {
      Image img = ImageDescriptor.createFromURL(
          CloudBeesUIPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/" + icons[i] + ".gif"))
          .createImage();
      System.out.println("Created image " + img);
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
    //fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalPullDown(IMenuManager manager) {
    //manager.add(action1);
    //manager.add(action2);
    manager.add(new Separator());
    manager.add(action3);
    manager.add(action4);
  }

  private void makeActions() {
    action1 = new Action() {
      public void run() {
        PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
            "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage", new String[] {
                "com.cloudbees.eclipse.ui.preferences.NectarInstancesPreferencePage",
                "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage" }, null);
        if (pref != null) {
          pref.open();
        }
      }
    };
    action1.setText("Configure CloudBees access...");
    action1.setToolTipText("Configure CloudBees account access");
    /*		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */
    action2 = new Action() {
      public void run() {
        PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
            "com.cloudbees.eclipse.ui.preferences.NectarInstancesPreferencePage", new String[] {
                "com.cloudbees.eclipse.ui.preferences.NectarInstancesPreferencePage",
                "com.cloudbees.eclipse.ui.internal.preferences.GeneralPreferencePage" }, null);
        if (pref != null) {
          pref.open();
        }
      }
    };
    action2.setText("Attach NectarInfo instances...");
    action2.setToolTipText("Attach more NectarInfo instances to monitor");

    /*		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */

    action3 = new Action() {
      public void run() {
        try {
          CloudBeesUIPlugin.getDefault().reloadForgeRepos();
        } catch (CloudBeesException e) {
          //TODO I18n!
          CloudBeesUIPlugin.showError("Failed to reload Forge repositories!", e);
        }
      }
    };

    action3.setText("Reload Forge repositories...");
    action3.setToolTipText("Reload Forge repositories and create local entries");
    /*		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    action3.setEnabled(forgeEnabled);

    action4 = new Action() {
      public void run() {
        try {
          CloudBeesUIPlugin.getDefault().showJobs(serviceUrl, viewUrl);
        } catch (CloudBeesException e) {
          //TODO I18n!
          CloudBeesUIPlugin.showError("Failed to reload Nectar jobs!", e);
        }
      }
    };
    action4.setText("Reload Nectar jobs...");
    action4.setToolTipText("Reload Nectar jobs");
    /*    action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    enableJaasButton();
  }

  protected void enableJaasButton() {
    if (action4 == null) {
      return;
    }

    boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
    action4.setEnabled(jaasEnabled && (serviceUrl != null || viewUrl != null));
  }

  public void setFocus() {
    table.getControl().setFocus();
  }

  public void propertyChange(PropertyChangeEvent event) {
    if (PreferenceConstants.P_ENABLE_FORGE.equals(event.getProperty())) {
      boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
      action3.setEnabled(forgeEnabled);
    }

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())) {
      boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
      if (!jaasEnabled) {
        setInput(null); // all gone
      }

      enableJaasButton();
    }
    
    if (PreferenceConstants.P_NECTAR_INSTANCES.equals(event.getProperty())) {
      try {
        CloudBeesUIPlugin.getDefault().showJobs(serviceUrl, viewUrl);
      } catch (CloudBeesException e) {
        //TODO I18n!
        CloudBeesUIPlugin.showError("Failed to reload Nectar jobs!", e);
      }
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeNectarChangeListener(nectarChangeListener);
    nectarChangeListener = null;

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
