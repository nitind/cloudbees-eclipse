/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.dev.ui.views.forge;

import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CBRemoteChangeAdapter;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.actions.ReloadForgeReposAction;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.cloudbees.eclipse.ui.views.CBTreeAction;
import com.cloudbees.eclipse.ui.views.CBTreeContributor;
import com.cloudbees.eclipse.ui.views.CBTreeProvider;

/**
 * View showing both Jenkins offline installations and JaaS Nectar instances
 * 
 * @author ahtik
 */
public class ForgeTreeView  extends CBTreeProvider implements IPropertyChangeListener {

  public static final String ID = "zcom.cloudbees.eclipse.ui.views.instances.ForgeTreeView";

  protected ITreeContentProvider contentProvider;
  protected ILabelProvider labelProvider;

  private TreeViewer viewer;
  private CBRemoteChangeListener jenkinsChangeListener;

  private CBTreeAction reloadForgeAction = new ReloadForgeReposAction();

  public void init() {


    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    
    contentProvider = new ForgeContentProvider();
    labelProvider = new ForgeLabelProvider();

    this.reloadForgeAction = new ReloadForgeReposAction();
    this.reloadForgeAction.setEnabled(forgeEnabled);

    this.jenkinsChangeListener = new CBRemoteChangeAdapter() {
      @Override
      public void forgeChanged(final List<ForgeInstance> instances) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            ForgeTreeView.this.contentProvider.inputChanged(ForgeTreeView.this.viewer, null, instances);
          }
        });
      }
    };

    CloudBeesUIPlugin.getDefault().addCBRemoteChangeListener(this.jenkinsChangeListener);
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (PreferenceConstants.P_ENABLE_FORGE.equals(event.getProperty())) {
      boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
      this.reloadForgeAction.setEnabled(forgeEnabled);
    }
    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())
        || PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())
        || PreferenceConstants.P_EMAIL.equals(event.getProperty())
        || PreferenceConstants.P_PASSWORD.equals(event.getProperty())) {
      try {
        CloudBeesDevUiPlugin.getDefault().reloadForgeRepos(false);
      } catch (CloudBeesException e) {
        CloudBeesDevUiPlugin.logError(e);
      }
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeCBRemoteChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    this.contentProvider = null;
    this.labelProvider = null;
  }

  @Override
  public CBTreeContributor[] getContributors() {
    return new CBTreeContributor[] {  this.reloadForgeAction };
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
    
    if (el instanceof ForgeGroup) {
      boolean exp = ForgeTreeView.this.viewer.getExpandedState(el);
      if (exp) {
        ForgeTreeView.this.viewer.collapseToLevel(el, 1);
      } else {
        ForgeTreeView.this.viewer.expandToLevel(el, 1);
      }
      return true;
    }

    if (el instanceof ForgeInstance) {
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
  public String getId() {
    return ID;
  }

}
