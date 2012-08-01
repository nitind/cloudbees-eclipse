package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.core.ApplicationInfoChangeListener;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.IStatusUpdater;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.popup.actions.ReloadRunAtCloudAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.cloudbees.eclipse.ui.views.CBTreeContributor;
import com.cloudbees.eclipse.ui.views.CBTreeProvider;
import com.cloudbees.eclipse.ui.views.ICBTreeProvider;

/**
 * View for showing all available apps in RUN@cloud
 * 
 * @author ahtik
 */
public class AppListView extends CBTreeProvider implements IPropertyChangeListener, ICBTreeProvider {

  public static final String ID = "com.cloudbees.eclipse.run.ui.views.AppListView";

  private TreeViewer viewer;

  protected ITreeContentProvider contentProvider = new AppContentProvider();
  protected LabelProvider labelProvider = new AppLabelProvider();

  protected CBRemoteChangeListener jenkinsChangeListener;

  protected ApplicationInfoChangeListener applicationChangeListener;

  private IStatusUpdater statusUpdater;

  public void init() {

    this.applicationChangeListener = new ApplicationInfoChangeListener() {

      @Override
      public void applicationInfoChanged() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            refresh(false);
          }
        });
      }
    };
    this.statusUpdater = new IStatusUpdater() {

      @Override
      public void update(ApplicationListResponse response) {
        AppListView.this.contentProvider.inputChanged(AppListView.this.viewer, null, response);
        Display.getDefault().asyncExec(new Runnable() {

          @Override
          public void run() {
            AppListView.this.viewer.refresh(true);
          }
        });
      }

      @Override
      public void update(String id, String status, ApplicationInfo info) {

      }
    };
    AppStatusUpdater.addListener(this.statusUpdater);

    CloudBeesUIPlugin.getDefault().addApplicationInfoChangeListener(this.applicationChangeListener);
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();
  }

  @Override
  public void dispose() {
    AppStatusUpdater.removeListener(this.statusUpdater);
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeCBRemoteChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    this.contentProvider = null;
    this.labelProvider = null;

  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())
        || PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())
        || PreferenceConstants.P_EMAIL.equals(event.getProperty())
        || PreferenceConstants.P_ACTIVE_ACCOUNT.equals(event.getProperty())
        || PreferenceConstants.P_PASSWORD.equals(event.getProperty())) {
      refresh(false);
    }
  }

  @Override
  public CBTreeContributor[] getContributors() {
    return new CBTreeContributor[] { new ReloadRunAtCloudAction() };
  }

  @Override
  public ITreeContentProvider getContentProvider() {
    return this.contentProvider;
  }

  @Override
  public ILabelProvider getLabelProvider() {
    return this.labelProvider;
  }

  @Override
  public boolean open(final Object el) {

    if (el instanceof AppGroup) {
      boolean exp = AppListView.this.viewer.getExpandedState(el);
      if (exp) {
        AppListView.this.viewer.collapseToLevel(el, 1);
      } else {
        AppListView.this.viewer.expandToLevel(el, 1);
      }
      return true;
    }

    if (el instanceof ApplicationInfo) {
      try {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IPageLayout.ID_PROP_SHEET);
        return true;
      } catch (PartInitException e) {
        return false;
      }
    }

    return false;
  }

  @Override
  public void setViewer(final TreeViewer viewer) {
    this.viewer = viewer;
    init();
  }

  private void refresh(boolean userAction) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading RUN@cloud applications list") {

      protected IStatus run(final IProgressMonitor monitor) {
        try {
          ApplicationListResponse list = BeesSDK.getList();
          AppListView.this.contentProvider.inputChanged(AppListView.this.viewer, null, list);
        } catch (Exception e1) {
          AppListView.this.contentProvider.inputChanged(AppListView.this.viewer, null, null);
          CBRunUiActivator.logErrorAndShowDialog(e1);
        }
        return Status.OK_STATUS;
      }
    };

    job.setUser(userAction);
    job.schedule();

  }

  @Override
  public String getId() {
    return ID;
  }

}
