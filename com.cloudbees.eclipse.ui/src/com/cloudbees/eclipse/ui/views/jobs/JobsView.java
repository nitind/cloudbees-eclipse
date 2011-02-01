package com.cloudbees.eclipse.ui.views.jobs;

import java.util.Arrays;
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
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

  private final Map<String, Image> stateIcons = new HashMap<String, Image>();

  private NectarChangeListener nectarChangeListener;

  private JobsContentProvider contentProvider;

  public JobsView() {
    super();
  }

  protected void setInput(NectarJobsResponse newView) {
    contentProvider.setJobs(Arrays.asList(newView.jobs));
    table.refresh();
  }

  class NameSorter extends ViewerSorter {

  }

  public void createPartControl(Composite parent) {

    initImages();

    table = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER
    /*SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL*/);
    //Tree tree = viewer.getTree();
    //tree.setHeaderVisible(true);

    table.getTable().setLinesVisible(true);
    table.getTable().setHeaderVisible(true);

    createColumn("S", 10, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        Image img = stateIcons.get(job.color);
        if (img != null) {
          cell.setText("");
          cell.setImage(img);
        } else {
          cell.setImage(null);
          cell.setText(job.color);
        }

      }
    });

    createColumn("Job", 200, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        cell.setText(job.displayName);
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
    createColumn("Last build", 100, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(job.lastBuild.fullDisplayName);
        } catch (Throwable t) {
          cell.setText("n/a");
        }
      }
    });
    createColumn("Last success", 100, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(job.lastSuccessfulBuild.fullDisplayName);
        } catch (Throwable t) {
          cell.setText("n/a");
        }
      }
    });
    createColumn("Last failure", 100, new CellLabelProvider() {
      public void update(ViewerCell cell) {
        NectarJobsResponse.Job job = (Job) cell.getViewerRow().getElement();
        try {
          cell.setText(job.lastFailedBuild.fullDisplayName);
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
    table.setSorter(new NameSorter());
    table.setInput(getViewSite());
    /*
        table.addFilter(new ViewerFilter() {
          @Override
          public boolean select(Viewer viewer, Object parentElement, Object element) {
            return true;
          }
        });*/

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

  private void initImages() {

    String[] icons = { "blue", "red" };

    for (int i = 0; i < icons.length; i++) {
      Image img = ImageDescriptor.createFromURL(
          CloudBeesUIPlugin.getDefault().getBundle().getResource("/jenkins-icons/16x16/" + icons[i] + ".gif"))
          .createImage();
      System.out.println("Created image " + img);
      stateIcons.put(icons[i], img);
    }

    stateIcons.put(
        "disabled",
        ImageDescriptor.createFromURL(
            CloudBeesUIPlugin.getDefault().getBundle().getResource("/jenkins-icons/16x16/grey.gif")).createImage());

  }

  private void createColumn(final String colName, int width, CellLabelProvider cellLabelProvider) {
    final TableViewerColumn treeViewerColumn = new TableViewerColumn(table, SWT.NONE);
    TableColumn trclmnCol = treeViewerColumn.getColumn();

    if (width > 0) {
      trclmnCol.setWidth(width);
    }

    trclmnCol.setText(colName);
    trclmnCol.setMoveable(true);

    treeViewerColumn.setLabelProvider(cellLabelProvider);

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
