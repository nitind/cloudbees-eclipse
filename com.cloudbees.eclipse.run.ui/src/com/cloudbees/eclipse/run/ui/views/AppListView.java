package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.core.ApplicationInfoChangeListener;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.IStatusUpdater;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;
import com.cloudbees.eclipse.ui.views.ICBTreeProvider;

/**
 * View for showing all available apps in RUN@cloud
 * 
 * @author ahtik
 */
public class AppListView extends ViewPart implements IPropertyChangeListener, ICBTreeProvider {

  public static final String ID = "com.cloudbees.eclipse.run.ui.views.AppListView";

  private TreeViewer viewer;

  protected ITreeContentProvider contentProvider = new AppContentProvider();
  protected LabelProvider labelProvider = new AppLabelProvider();

  protected JenkinsChangeListener jenkinsChangeListener;

  protected ApplicationInfoChangeListener applicationChangeListener;

  private IStatusUpdater statusUpdater;

  public void init() {

    this.applicationChangeListener = new ApplicationInfoChangeListener() {

      @Override
      public void applicationInfoChanged() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            try {
              ApplicationListResponse list = BeesSDK.getList();
              AppListView.this.contentProvider.inputChanged(AppListView.this.viewer, null, list);
              AppListView.this.viewer.refresh(true);
            } catch (Exception e1) {
              CBRunUiActivator.logErrorAndShowDialog(e1);
            }
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
    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    this.contentProvider = null;
    this.labelProvider = null;

    super.dispose();
  }

  @Override
  public void createPartControl(final Composite parent) {
    this.viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
    this.viewer.setContentProvider(this.contentProvider);
    this.viewer.setLabelProvider(this.labelProvider);
    getSite().setSelectionProvider(this.viewer);
    init();
  }

  @Override
  public void setFocus() {
    this.viewer.getControl().setFocus();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
  }

  @Override
  public CBTreeAction[] getContributors() {
    return new CBTreeAction[0];
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
  public boolean open(final Object object) {
    return false;
  }

  @Override
  public void setViewer(final TreeViewer viewer) {
    this.viewer = viewer;
    init();
  }

}
