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
package com.cloudbees.eclipse.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.ui.internal.AccountSelectionDialog;

class MultiAccountUtils {

  private static boolean selectionInProgress = false;
  
  /**
   * Loads available accounts and checks if user needs to be asked for the account.
   * 
   * @throws CloudBeesException
   */
  static void selectActiveAccount() throws CloudBeesException {

    final GrandCentralService gcs = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    if (selectionInProgress || gcs.getEmail() == null || gcs.getEmail().length() == 0) {
      //Nothing to do for an empty user.
      return;
    }
    //gcs.setAccountSelectionActive(true);
    
    selectionInProgress = true;

    //  new Exception().printStackTrace();
    
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Selecting active CloudBees account") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        GrandCentralService gcs = null;
        try {

          gcs = CloudBeesCorePlugin.getDefault().getGrandCentralService();
          monitor.beginTask("Loading available accounts for " + gcs.getEmail(), 100);         
          
          String origActiveAccount = CloudBeesUIPlugin.getDefault().getPreferenceStore()
              .getString(PreferenceConstants.P_ACTIVE_ACCOUNT);
          String activeAccount = origActiveAccount;

          //Reset current elements in case login fails.
          CloudBeesUIPlugin.getDefault().fireAccountNameChange(gcs.getEmail(), null);
          
          String[] accts = null;
          try {
            accts = CloudBeesCorePlugin.getDefault().getGrandCentralService().getAccounts(monitor);
          } catch (CloudBeesException e) {
            // Either because of server connectivity issues or bad credentials.
            monitor.setTaskName("Failed to retrieve account info: "+e.getMessage());
            //return new Status(IStatus.ERROR, CloudBeesUIPlugin.PLUGIN_ID, 0, "Failed to retrieve account info: "+e.getMessage(), e);
            // return OK as null response is handled properly and at this point error message is avoided.
            CloudBeesUIPlugin.getDefault().setAuthStatus(AuthStatus.FAILED);
            return Status.OK_STATUS;
          }
          
          final String[] accounts = accts;

          monitor.setTaskName("Found " + accounts.length + " accounts available for " + gcs.getEmail());
          monitor.worked(90);

          boolean foundAndActive = false;
          for (String a : accounts) {
            if (a != null && activeAccount != null && activeAccount.equals(a)) {
              foundAndActive = true;
              break;
            }
          }

          if (foundAndActive) {
            // Already using it as active account.
            monitor.setTaskName("Using account " + activeAccount);
            monitor.worked(10);
          } else if (accounts.length == 1) {
            activeAccount = accounts[0];
            monitor.setTaskName("Using account " + activeAccount);
            monitor.worked(10);
          } else if (accounts.length == 0) {
            // problem! no accounts found.
            throw new CloudBeesException("No accounts found for user " + gcs.getEmail());
          } else {           
            // multiple accounts. prompt user!
            final int[] retCode = {0};
            final String[] account = {null};
            
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
              public void run() {
                Shell shell = Display.getDefault().getActiveShell();
                AccountSelectionDialog dialog = new AccountSelectionDialog(shell, accounts);
                dialog.open();
                account[0] = dialog.getSelectedAccountName();
                retCode[0] = dialog.getReturnCode();
              }
            });
            

            if (retCode[0] != IDialogConstants.OK_ID || account[0] == null || account[0].length() == 0) {
              // Nothing selected.
              return Status.OK_STATUS;
            } else {
              activeAccount = account[0];
            }

          }

          CloudBeesUIPlugin.getDefault().setAuthStatus(AuthStatus.OK);

          activateAccountName(activeAccount);

          return Status.OK_STATUS;

        } catch (Exception e) {
          String msg = e.getLocalizedMessage();
          if (e instanceof CloudBeesException) {
            e = (Exception) e.getCause();
          }
          //CloudBeesUIPlugin.getDefault().getLogger().error(msg, e);
          return new Status(IStatus.ERROR, CloudBeesUIPlugin.PLUGIN_ID, 0, msg, e);
        } finally {
          monitor.done();
          selectionInProgress = false;
          //gcs.setAccountSelectionActive(false);
        }
      }

    };

    job.setUser(false);
    job.schedule();
  }

  public final static void activateAccountName(String activeAccount) throws CloudBeesException {
    final GrandCentralService gcs = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    
    String email = gcs.getEmail();
    
    String origActiveAccount = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getString(PreferenceConstants.P_ACTIVE_ACCOUNT);
    
    gcs.setActiveAccount(activeAccount);
    
    if (origActiveAccount == null || !origActiveAccount.equals(activeAccount)) {
      CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .setValue(PreferenceConstants.P_ACTIVE_ACCOUNT, activeAccount);
    }
    
    CloudBeesUIPlugin.getDefault().fireActiveAccountChanged(email, activeAccount);
    
  }
  
}
