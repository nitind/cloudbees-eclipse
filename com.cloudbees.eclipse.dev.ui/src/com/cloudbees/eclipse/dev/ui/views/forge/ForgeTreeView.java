package com.cloudbees.eclipse.dev.ui.views.forge;

import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.dev.ui.actions.ReloadForgeReposAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.cloudbees.eclipse.ui.views.CBTreeAction;
import com.cloudbees.eclipse.ui.views.ICBTreeProvider;

/**
 * View showing both Jenkins offline installations and JaaS Nectar instances
 *
 * @author ahtik
 */
public class ForgeTreeView implements IPropertyChangeListener, ICBTreeProvider {

  public static final String ID = "com.cloudbees.eclipse.ui.views.instances.JenkinsTreeView";

  protected ITreeContentProvider contentProvider = new ForgeContentProvider();
  protected ILabelProvider labelProvider = new ForgeLabelProvider();

  private TreeViewer viewer;
  private JenkinsChangeListener jenkinsChangeListener;

  private CBTreeAction reloadForgeAction = new ReloadForgeReposAction();

  public void init() {
    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    this.reloadForgeAction = new ReloadForgeReposAction();
    this.reloadForgeAction.setEnabled(forgeEnabled);

    this.jenkinsChangeListener = new JenkinsChangeListener() {
      @Override
      public void activeJobViewChanged(final JenkinsJobsResponse newView) {
      }

      @Override
      public void activeJobHistoryChanged(final JenkinsJobAndBuildsResponse newView) {
      }

      @Override
      public void forgeChanged(final List<ForgeInstance> instances) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            ForgeTreeView.this.contentProvider.inputChanged(ForgeTreeView.this.viewer, null, instances);
          }
        });
      }

      @Override
      public void jenkinsChanged(final List<JenkinsInstanceResponse> instances) {
      }
    };

    CloudBeesUIPlugin.getDefault().addJenkinsChangeListener(this.jenkinsChangeListener);
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (PreferenceConstants.P_ENABLE_FORGE.equals(event.getProperty())) {
      boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
      this.reloadForgeAction.setEnabled(forgeEnabled);
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    this.contentProvider = null;
    this.labelProvider = null;
  }

  @Override
  public CBTreeAction[] getActions() {
    return new CBTreeAction[] { this.reloadForgeAction };
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
    return false;
  }

}
