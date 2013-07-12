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
package com.cloudbees.eclipse.dtp.internal.treeview;

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

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.api.DatabaseListResponse;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.DatabaseInfoChangeListener;
import com.cloudbees.eclipse.dtp.internal.DatabaseStatusUpdate;
import com.cloudbees.eclipse.dtp.internal.ReloadDatabaseAction;
import com.cloudbees.eclipse.run.core.BeesSDK;
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
public class DBListView extends CBTreeProvider implements IPropertyChangeListener, ICBTreeProvider {

  public static final String ID = "com.cloudbees.eclipse.dtp.DBListView";

  private TreeViewer viewer;

  protected ITreeContentProvider contentProvider;
  protected LabelProvider labelProvider;

  protected CBRemoteChangeListener jenkinsChangeListener;

  protected DatabaseInfoChangeListener databaseChangeListener;

  private DatabaseStatusUpdate statusUpdater;

  protected boolean loadfinished = true;

  public void init() {

    contentProvider = new DBContentProvider();
    labelProvider = new DBLabelProvider();

    this.databaseChangeListener = new DatabaseInfoChangeListener() {

      @Override
      public void databaseInfoChanged() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            refresh(false);
          }
        });
      }
    };
    this.statusUpdater = new DatabaseStatusUpdate() {

      public void update(DatabaseListResponse response) {
        DBListView.this.contentProvider.inputChanged(DBListView.this.viewer, null, response);
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            DBListView.this.viewer.refresh(true);
          }
        });
      }

      public void update(String id, String status, DatabaseInfo info) {

      }
    };
    DatabaseStatusHandler.addListener(this.statusUpdater);

    CloudBeesUIPlugin.getDefault().addDatabaseInfoChangeListener(this.databaseChangeListener);
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().fireDatabaseInfoChanged();
  }

  @Override
  public void dispose() {
    DatabaseStatusHandler.removeListener(this.statusUpdater);
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeCBRemoteChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;
    this.contentProvider = null;
    this.labelProvider = null;

  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())
        || PreferenceConstants.P_EMAIL.equals(event.getProperty())
        || PreferenceConstants.P_ACTIVE_ACCOUNT.equals(event.getProperty())
        || PreferenceConstants.P_ACTIVE_REGION.equals(event.getProperty())
        || PreferenceConstants.P_PASSWORD.equals(event.getProperty())) {
      refresh(false);
    }
  }

  @Override
  public CBTreeContributor[] getContributors() {
    return new CBTreeContributor[] { new ReloadDatabaseAction() };
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

    if (el instanceof DBGroup) {
      boolean exp = DBListView.this.viewer.getExpandedState(el);
      if (exp) {
        DBListView.this.viewer.collapseToLevel(el, 1);
      } else {
        DBListView.this.viewer.expandToLevel(el, 1);
      }
      return true;
    }

    if (el instanceof DatabaseInfo) {
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
    if (loadfinished == false) {
      return;
    }

    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Database info") {

      protected IStatus run(final IProgressMonitor monitor) {
        try {
          if (contentProvider != null) {
            String account = CloudBeesUIPlugin.getDefault().getActiveAccountName(monitor);
            DatabaseListResponse list = BeesSDK.getDatabaseList(account);
            DBListView.this.contentProvider.inputChanged(DBListView.this.viewer, null, list);
          }
        } catch (Exception e1) {
          DBListView.this.contentProvider.inputChanged(DBListView.this.viewer, null, null);
          //CloudBeesDataToolsPlugin.logErrorAndShowDialog(e1);
        } finally {
          
          DBListView.this.loadfinished = true;
          
          PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
              DBListView.this.viewer.refresh(true);
            }
          });
          
        }
        return Status.OK_STATUS;
      }
    };

    job.setUser(userAction);
    DBListView.this.loadfinished = false;
    job.schedule();

  }

  @Override
  public String getId() {
    return ID;
  }

}
