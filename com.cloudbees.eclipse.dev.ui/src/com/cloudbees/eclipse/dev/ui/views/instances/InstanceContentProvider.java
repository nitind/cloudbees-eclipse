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
package com.cloudbees.eclipse.dev.ui.views.instances;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class InstanceContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private InstanceGroup jenkinsLocalGroup = new InstanceGroup.OnPremiseJenkinsInstanceGroup("Local Builds", false);
  private InstanceGroup jenkinsCloudGroup = new InstanceGroup.DevAtCloudJenkinsInstanceGroup("Builds", true);

  //private InstanceGroup favoritesGroup = new FavoritesInstanceGroup("Favorite Jenkins Jobs", false);

  public InstanceContentProvider() {
    this.jenkinsLocalGroup.setLoading(false);
    this.jenkinsCloudGroup.setLoading(false);
    //this.favoritesGroup.setLoading(false);
  }

  @Override
  public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {

    if (newInput == null || !(newInput instanceof List)) {
      if (v != null) {
        if (v instanceof TreeViewer) {
          ((TreeViewer)v).refresh(true);
        } else {
          v.refresh();  
        }
        
      }
      return;
    }

    boolean hasLocal = false;
    boolean hasCloud = false;

    for (Object instance : (List) newInput) {
      if (instance instanceof JenkinsInstanceResponse) {
        JenkinsInstanceResponse nir = (JenkinsInstanceResponse) instance;
        if (nir.atCloud) {
          hasCloud = true;
        } else {
          hasLocal = true;
        }
      }
    }

    if (hasLocal) {
      this.jenkinsLocalGroup.clear();
    }

    if (hasCloud || (newInput instanceof List && ((List)newInput).isEmpty())) {
      this.jenkinsCloudGroup.clear();
    }

    for (Object instance : (List) newInput) {
      if (instance instanceof JenkinsInstanceResponse) {
        JenkinsInstanceResponse nir = (JenkinsInstanceResponse) instance;
        if (nir.atCloud) {
          this.jenkinsCloudGroup.addChild(nir);
        } else {
          this.jenkinsLocalGroup.addChild(nir);
        }
      }
    }

    if (hasLocal) {
      this.jenkinsLocalGroup.setLoading(false);
    }

    if (hasCloud) {
      this.jenkinsCloudGroup.setLoading(false);
    }

    if (v != null) {
      v.refresh();
      ((TreeViewer) v).expandToLevel(1);
    }
  }

  @Override
  public void dispose() {
    this.jenkinsLocalGroup = null;
    this.jenkinsCloudGroup = null;
  }

  @Override
  public Object[] getElements(final Object parent) {
    return getChildren(parent);
  }

  @Override
  public Object getParent(final Object child) {
    if (child instanceof JenkinsInstanceHolder) {
      return ((JenkinsInstanceHolder) child).getParent();
    }
    return null;
  }

  @Override
  public Object[] getChildren(final Object parent) {
    if (parent instanceof IViewSite) {
      return new InstanceGroup[] { this.jenkinsCloudGroup, this.jenkinsLocalGroup };
    } else if (parent instanceof InstanceGroup) {
      InstanceGroup ig = ((InstanceGroup) parent);
      JenkinsInstanceResponse[] ret = ig.getChildren();
      if (ig.isCloudHosted() && ret != null && ret.length == 1) {
        //if hosted at cloud then only one jenkins can be enabled at once
        JenkinsInstanceResponse resp = (JenkinsInstanceResponse) ret[0];
        return resp.views;
      }
      return ig.getChildren();
    } else if (parent instanceof JenkinsInstanceResponse) {
      JenkinsInstanceResponse resp = (JenkinsInstanceResponse) parent;
      return resp.views;
    }
    return new Object[0];
  }

  @Override
  public boolean hasChildren(final Object parent) {
    if (parent instanceof InstanceGroup) {
      return ((InstanceGroup) parent).hasChildren();
    }

    if (parent instanceof JenkinsInstanceResponse) {
      return ((JenkinsInstanceResponse) parent).views != null && ((JenkinsInstanceResponse) parent).views.length > 0;
    }

    return false;
  }

  public void jenkinsStatusUpdate(final TreeViewer viewer, String viewUrl, boolean online) {
    JenkinsService jenkinsService = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(viewUrl);

    boolean dirty = updateJenkinsGroup(jenkinsLocalGroup, jenkinsService, online);
    
    if (updateJenkinsGroup(jenkinsCloudGroup, jenkinsService, online)) {
      dirty = true;
    }
    
    if (dirty) {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          viewer.refresh(true);
        }
      });
      
    }
    
  }

  /**
   * @param group
   * @param jenkinsService
   * @param online
   * @return true if any change was made to the tree structure
   */
  private boolean updateJenkinsGroup(InstanceGroup group, JenkinsService jenkinsService, boolean online) {
    
    boolean dirty = false;
    
    // Let's see if this url is represented in this tree
    for (JenkinsInstanceResponse i : group.getChildren()) {
      if (jenkinsService.getUrl().startsWith(i.viewUrl)) {

        // came online
        if ((i.offline && online)) {
          // fetch new response
          try {
            JenkinsInstanceResponse newResp = jenkinsService.getInstance(new NullProgressMonitor());
            group.removeChild(i);
            group.addChild(newResp);
            dirty = true;
          } catch (CloudBeesException e) {
            e.printStackTrace();
          }
        }

        // went offline
        if ((!i.offline && !online)) {
          // prepare the fake response
          JenkinsInstanceResponse fakeResp = new JenkinsInstanceResponse();
          fakeResp.viewUrl = jenkinsService.getUrl();
          fakeResp.nodeName = jenkinsService.getLabel();
          fakeResp.offline = true;
          fakeResp.atCloud = jenkinsService.isCloud();
          group.removeChild(i);
          group.addChild(fakeResp);
          dirty = true;
        }

      }
      
    }
    
    if (dirty) {
      // status changed. Not loading anymore
      group.setLoading(false);
    }
    
    return dirty;

  }

  public void cloudLoadingFinished() {
    jenkinsCloudGroup.setLoading(false);  
  }

}
