package com.cloudbees.eclipse.dev.ui.views.instances;

import java.util.List;

import org.eclipse.jface.action.IMenuManager;
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

import com.cloudbees.eclipse.core.CBRemoteChangeAdapter;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.BaseJenkinsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse.View;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.actions.ConfigureJenkinsInstancesAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadJenkinsCloudInstancesAction;
import com.cloudbees.eclipse.dev.ui.actions.ReloadJenkinsLocalInstancesAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.cloudbees.eclipse.ui.views.CBTreeAction;
import com.cloudbees.eclipse.ui.views.CBTreeContributor;
import com.cloudbees.eclipse.ui.views.ICBTreeProvider;

/**
 * View showing both Jenkins offline installations and JaaS Nectar instances
 * 
 * @author ahtik
 */
public class JenkinsTreeView extends ViewPart implements IPropertyChangeListener, ICBTreeProvider {

  public static final String ID = "acom.cloudbees.eclipse.ui.views.instances.JenkinsTreeView";

  protected InstanceContentProvider contentProvider = new InstanceContentProvider();
  protected ILabelProvider labelProvider = new InstanceLabelProvider();

  private TreeViewer viewer;
  private CBRemoteChangeListener jenkinsChangeListener;

  private CBTreeAction attachJenkinsAction = new ConfigureJenkinsInstancesAction();
  //  private CBTreeAction reloadForgeAction = new ReloadForgeReposAction();
  private CBTreeAction reloadJenkinsCloudAction = new ReloadJenkinsCloudInstancesAction();
  private CBTreeAction reloadJenkinsLocalAction = new ReloadJenkinsLocalInstancesAction();

  class NameSorter extends ViewerSorter {

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
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
    //    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
    //        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    //    this.reloadForgeAction.setEnabled(forgeEnabled);

    boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
    this.reloadJenkinsCloudAction.setEnabled(jaasEnabled);

    this.jenkinsChangeListener = new CBRemoteChangeAdapter() {

      public void jenkinsChanged(final List<JenkinsInstanceResponse> instances) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            JenkinsTreeView.this.contentProvider.inputChanged(JenkinsTreeView.this.viewer, null, instances);
          }
        });
        
      }
      
      public void jenkinsStatusUpdate(String viewUrl, boolean online) {
        JenkinsTreeView.this.contentProvider.jenkinsStatusUpdate(JenkinsTreeView.this.viewer, viewUrl, online);
      }

      public void activeAccountChanged(String email, String newAccountName) {
        contentProvider.cloudLoadingFinished();
      }
      
    };

    CloudBeesUIPlugin.getDefault().addCBRemoteChangeListener(this.jenkinsChangeListener);
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    CloudBeesUIPlugin.getDefault().reloadAllCloudJenkins(false);
    CloudBeesUIPlugin.getDefault().reloadAllLocalJenkins(false);
    
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
    manager.add(this.attachJenkinsAction);
    manager.add(this.reloadJenkinsLocalAction);
    manager.add(this.reloadJenkinsCloudAction);
  }

/*  private void makeActions() {
    this.configureAccountAction = new ConfigureCloudBeesAction();
    this.attachJenkinsAction = new ConfigureJenkinsInstancesAction();
    //    this.reloadForgeAction = new ReloadForgeReposAction();
    this.reloadJenkinsAction = new ReloadJenkinsInstancesAction();
    this.configureSshAction = new ConfigureSshKeysAction();

    //    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
    //        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    //    this.reloadForgeAction.setEnabled(forgeEnabled);

    boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
    this.reloadJenkinsAction.setEnabled(jaasEnabled);

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }*/

  @Override
  public void setFocus() {
    this.viewer.getControl().setFocus();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    //    if (PreferenceConstants.P_ENABLE_FORGE.equals(event.getProperty())) {
    //      boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
    //          .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    //      this.reloadForgeAction.setEnabled(forgeEnabled);
    //    }

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())) {
      boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
      this.reloadJenkinsCloudAction.setEnabled(jaasEnabled);
    }

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())
        || PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())
        || PreferenceConstants.P_EMAIL.equals(event.getProperty())
        || PreferenceConstants.P_PASSWORD.equals(event.getProperty())) {
      CloudBeesUIPlugin.getDefault().reloadAllCloudJenkins(false);
    }
    
    if (PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())) {
      CloudBeesUIPlugin.getDefault().reloadAllLocalJenkins(false);
    }
    
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeCBRemoteChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    this.contentProvider = null;
    this.labelProvider = null;

    super.dispose();
  }

  @Override
  public CBTreeContributor[] getContributors() {
    return new CBTreeContributor[] { 
         this.attachJenkinsAction, reloadJenkinsLocalAction, this.reloadJenkinsCloudAction};
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
  public void setViewer(final TreeViewer viewer) {
    this.viewer = viewer;

    init();
  }

  @Override
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

  public String getId() {
    return ID;
  }

}
