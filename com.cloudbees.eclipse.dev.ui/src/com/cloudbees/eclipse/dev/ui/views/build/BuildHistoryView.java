/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.dev.ui.views.build;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

import com.cloudbees.eclipse.core.CBRemoteChangeAdapter;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.actions.InvokeBuildAction;
import com.cloudbees.eclipse.dev.ui.actions.OpenBuildAction;
import com.cloudbees.eclipse.dev.ui.actions.OpenLogAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadBuildHistoryAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

public class BuildHistoryView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.dev.ui.views.build.BuildHistoryView";

  private TableViewer table;

  protected OpenBuildAction actionOpenBuild;
  protected OpenLogAction actionOpenLog;
  protected ReloadBuildHistoryAction actionReloadJobs;
  private InvokeBuildAction actionInvokeBuild;
  private Action actionOpenBuildInBrowser;

  private final Map<String, Image> stateIcons = new HashMap<String, Image>();

  private CBRemoteChangeListener jenkinsChangeListener;

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

    TableViewerColumn statusCol = createColumn("S", 22, BuildSorter.STATE, new CellLabelProvider() {
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
    statusCol.getColumn().setToolTipText("Status");

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

        if (build.timestamp != null) {
          try {
            cell.setText(Utils.humanReadableTime(System.currentTimeMillis() - build.timestamp) + " ago");
          } catch (Exception e) {
            cell.setText("");
          }
        } else {
          cell.setText("");
        }
        cell.setImage(null);
      }
    });

    createColumn("Build Duration", 100, BuildSorter.DURATION, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild build = (JenkinsBuild) cell.getViewerRow().getElement();
        if (build.duration != null) {
          try {
            cell.setText(Utils.humanReadableTime(build.duration));
          } catch (Throwable t) {
            cell.setText("");
          }
        } else {
          cell.setText("");
        }
      }
    });

    createColumn("Tests", 200, BuildSorter.TESTS, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild build = (JenkinsBuild) cell.getViewerRow().getElement();
        try {
          long total = 0;
          long failed = 0;
          long skipped = 0;
          for (com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild.Action action : build.actions) {
            if ("testReport".equalsIgnoreCase(action.urlName)) {
              total += action.totalCount;
              failed += action.failCount;
              skipped += action.skipCount;
            }
          }

          if (total > 0 || failed > 0 || skipped > 0) {
            String val = "Passed: " + (total - failed - skipped);
            if (failed > 0) {
              val += ", failed: " + failed;
            }
            if (skipped > 0) {
              val += ", skipped: " + skipped;
            }
            cell.setText(val);
          } else {
            cell.setText("");
          }
        } catch (Throwable t) {
          cell.setText("");
        }
      }
    });
    createColumn("Cause", 250, BuildSorter.CAUSE, new CellLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        JenkinsBuild build = (JenkinsBuild) cell.getViewerRow().getElement();
        String val = null;
        try {
          for (com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild.Action action : build.actions) {
            if (action.causes != null && action.causes.length > 0) {
              val = action.causes[0].shortDescription;
              break;
            }
          }

          if (val == null) {
            val = "";
          }
          cell.setText(val);
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

    popupMenu.add(this.actionOpenBuild);
    popupMenu.add(this.actionOpenLog);
    popupMenu.add(new Separator("cloudActions"));
    popupMenu.add(this.actionOpenBuildInBrowser);
    popupMenu.add(this.actionInvokeBuild);
    popupMenu.add(new Separator("reloadActions"));
    popupMenu.add(this.actionReloadJobs);

    Menu menu = popupMenu.createContextMenu(this.table.getTable());
    this.table.getTable().setMenu(menu);

    this.table.addPostSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(final SelectionChangedEvent event) {
        StructuredSelection sel = (StructuredSelection) event.getSelection();
        BuildHistoryView.this.selectedBuild = sel.getFirstElement();
        boolean enable = BuildHistoryView.this.selectedBuild != null;
        BuildHistoryView.this.actionInvokeBuild.setJob(BuildHistoryView.this.selectedBuild);
        BuildHistoryView.this.actionOpenBuildInBrowser.setEnabled(enable);
        BuildHistoryView.this.actionOpenBuild.setBuild(BuildHistoryView.this.selectedBuild);
        BuildHistoryView.this.actionOpenLog.setBuild(BuildHistoryView.this.selectedBuild);

        getViewSite().getActionBars().getToolBarManager().update(true);
      }
    });

    this.jenkinsChangeListener = new CBRemoteChangeAdapter() {

      public void activeJobHistoryChanged(final JenkinsJobAndBuildsResponse newView) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            BuildHistoryView.this.setInput(newView);
          }
        });
      }

    };

    CloudBeesUIPlugin.getDefault().addCBRemoteChangeListener(this.jenkinsChangeListener);
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
    this.stateIcons.put(
        "UNSTABLE",
        ImageDescriptor.createFromURL(
            CloudBeesDevUiPlugin.getDefault().getBundle().getResource("/icons/jenkins-icons/16x16/yellow.gif"))
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
    //    manager.add(this.actionOpenBuild);
    //    manager.add(this.actionOpenLog);
    manager.add(this.actionOpenBuildInBrowser);
    manager.add(this.actionInvokeBuild);
    manager.add(new Separator("reloadJob"));
    manager.add(this.actionReloadJobs);
  }

  private void fillLocalPullDown(final IMenuManager manager) {
    manager.add(this.actionOpenBuild);
    manager.add(this.actionOpenLog);
    //    manager.add(this.actionInvokeBuild);
    //    manager.add(new Separator());
    //    manager.add(this.actionReloadJobs);
  }

  private void makeActions() {

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    this.actionOpenBuild = new OpenBuildAction(false);
    this.actionOpenLog = new OpenLogAction();

    this.actionReloadJobs = new ReloadBuildHistoryAction(true);

    //    this.actionOpenLastBuildDetails = new OpenBuildAction(this);
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
    this.actionOpenBuildInBrowser.setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_BROWSER));
    this.actionOpenBuildInBrowser.setEnabled(false);

    this.actionInvokeBuild = new InvokeBuildAction();

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
    CloudBeesUIPlugin.getDefault().removeCBRemoteChangeListener(this.jenkinsChangeListener);
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
