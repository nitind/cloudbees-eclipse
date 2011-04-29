package com.cloudbees.eclipse.dev.ui.views.instances;

import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.jenkins.api.BaseJenkinsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse.View;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.actions.ConfigureJenkinsInstancesAction;
import com.cloudbees.eclipse.dev.ui.actions.ConfigureSshKeysAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadForgeReposAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadJenkinsInstancesAction;
import com.cloudbees.eclipse.dev.ui.utils.FavoritesUtils;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.cloudbees.eclipse.ui.internal.action.ConfigureCloudBeesAction;
import com.cloudbees.eclipse.ui.views.CBTreeAction;
import com.cloudbees.eclipse.ui.views.ICBTreeProvider;

/**
 * View showing both Jenkins offline installations and JaaS Nectar instances
 *
 * @author ahtik
 */
public class JenkinsTreeView extends ViewPart implements IPropertyChangeListener, ICBTreeProvider {

  public static final String ID = "com.cloudbees.eclipse.ui.views.instances.JenkinsTreeView";

  protected ITreeContentProvider contentProvider = new InstanceContentProvider();
  protected ILabelProvider labelProvider = new InstanceLabelProvider();

  private TreeViewer viewer;
  private JenkinsChangeListener jenkinsChangeListener;

  private CBTreeAction configureAccountAction = new ConfigureCloudBeesAction();
  private CBTreeAction attachJenkinsAction = new ConfigureJenkinsInstancesAction();
  private CBTreeAction reloadForgeAction = new ReloadForgeReposAction();
  private CBTreeAction reloadJenkinsAction = new ReloadJenkinsInstancesAction();
  private CBTreeAction configureSshAction = new ConfigureSshKeysAction();

  class NameSorter extends ViewerSorter {

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
      if (e1 instanceof FavoritesInstanceGroup) {
        return -1;
      }
      if (e2 instanceof FavoritesInstanceGroup) {
        return +1;
      }

      if (e1 instanceof JenkinsInstanceResponse.View && e2 instanceof JenkinsInstanceResponse.View) {
        JenkinsInstanceResponse.View v1 = (View) e1;
        JenkinsInstanceResponse.View v2 = (View) e2;
        if (v1.name != null && v2.name != null) {
          return v1.name.compareTo(v2.name);
        }

      }

      return super.compare(viewer, e1, e2);
    }
  }

  public void init() {
    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    this.reloadForgeAction.setEnabled(forgeEnabled);

    boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
    this.reloadJenkinsAction.setEnabled(jaasEnabled);

    this.jenkinsChangeListener = new JenkinsChangeListener() {
      @Override
      public void activeJobViewChanged(final JenkinsJobsResponse newView) {
      }

      @Override
      public void activeJobHistoryChanged(final JenkinsJobAndBuildsResponse newView) {
      }

      @Override
      public void jenkinsChanged(final List<JenkinsInstanceResponse> instances) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            JenkinsTreeView.this.contentProvider.inputChanged(JenkinsTreeView.this.viewer, null, instances);
          }
        });
      }
    };

    CloudBeesUIPlugin.getDefault().addJenkinsChangeListener(this.jenkinsChangeListener);
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    CloudBeesUIPlugin.getDefault().reloadAllJenkins(false);
  }

  @Override
  public void createPartControl(final Composite parent) {
    this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    this.viewer.setContentProvider(this.contentProvider);
    this.viewer.setLabelProvider(this.labelProvider);
    this.viewer.setSorter(new NameSorter());
    this.viewer.setInput(getViewSite());

    this.viewer.addOpenListener(new IOpenListener() {

      @Override
      public void open(final OpenEvent event) {
        ISelection sel = event.getSelection();
        if (sel instanceof TreeSelection) {
          Object el = ((TreeSelection) sel).getFirstElement();
          JenkinsTreeView.this.open(el);
        }
      }
    });

    contributeToActionBars();

    init();
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    //fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalPullDown(final IMenuManager manager) {
    manager.add(this.configureAccountAction);
    manager.add(this.attachJenkinsAction);
    manager.add(this.configureSshAction);
    manager.add(new Separator());
    manager.add(this.reloadForgeAction);
    manager.add(this.reloadJenkinsAction);
  }

  private void makeActions() {
    this.configureAccountAction = new ConfigureCloudBeesAction();
    this.attachJenkinsAction = new ConfigureJenkinsInstancesAction();
    this.reloadForgeAction = new ReloadForgeReposAction();
    this.reloadJenkinsAction = new ReloadJenkinsInstancesAction();
    this.configureSshAction = new ConfigureSshKeysAction();

    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    this.reloadForgeAction.setEnabled(forgeEnabled);

    boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
    this.reloadJenkinsAction.setEnabled(jaasEnabled);

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }

  @Override
  public void setFocus() {
    this.viewer.getControl().setFocus();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (PreferenceConstants.P_ENABLE_FORGE.equals(event.getProperty())) {
      boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
      this.reloadForgeAction.setEnabled(forgeEnabled);
    }

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())) {
      boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
      this.reloadJenkinsAction.setEnabled(jaasEnabled);
    }

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())
        || PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())
        || PreferenceConstants.P_EMAIL.equals(event.getProperty())
        || PreferenceConstants.P_PASSWORD.equals(event.getProperty())) {
      CloudBeesUIPlugin.getDefault().reloadAllJenkins(false);
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    this.contentProvider = null;
    this.labelProvider = null;

    super.dispose();
  }

  @Override
  public CBTreeAction[] getActions() {
    return new CBTreeAction[] { this.configureAccountAction, this.attachJenkinsAction, this.reloadForgeAction, this.reloadJenkinsAction,
        this.configureSshAction };
  }

  public ITreeContentProvider getContentProvider() {
    return this.contentProvider;
  }

  public ILabelProvider getLabelProvider() {
    return this.labelProvider;
  }

  public void setViewer(final TreeViewer viewer) {
    this.viewer = viewer;

    init();
  }

  public boolean open(final Object el) {
    if (el instanceof BaseJenkinsResponse) {
      BaseJenkinsResponse resp = (BaseJenkinsResponse) el;
      try {
        CloudBeesDevUiPlugin.getDefault().showJobs(resp.viewUrl, true);
      } catch (CloudBeesException e) {
        CloudBeesUIPlugin.getDefault().getLogger().error(e);
      }
      return true;
    } else if (el instanceof JenkinsInstanceResponse.View) {
      try {
        CloudBeesDevUiPlugin.getDefault().showJobs(((JenkinsInstanceResponse.View) el).url, true);
      } catch (CloudBeesException e) {
        CloudBeesUIPlugin.getDefault().getLogger().error(e);
      }
      return true;
    } else if (el instanceof FavoritesInstanceGroup) {
      try {
        CloudBeesDevUiPlugin.getDefault().showJobs(FavoritesUtils.FAVORITES, true);
      } catch (CloudBeesException e) {
        CloudBeesUIPlugin.getDefault().getLogger().error(e);
      }
      return true;
    } else if (el instanceof InstanceGroup) {
      boolean exp = JenkinsTreeView.this.viewer.getExpandedState(el);
      if (exp) {
        JenkinsTreeView.this.viewer.collapseToLevel(el, 1);
      } else {
        JenkinsTreeView.this.viewer.expandToLevel(el, 1);
      }
      return true;
    }

    return false;
  }

}
