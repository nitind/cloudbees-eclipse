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
package com.cloudbees.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ActiveAccountContributionItem extends CompoundContributionItem {

  boolean onlyAccountRelated = false;

  public ActiveAccountContributionItem() {
    super();
  }

  public ActiveAccountContributionItem(boolean onlyAccountRelated) {
    this.onlyAccountRelated = onlyAccountRelated;
  }

  @Override
  protected IContributionItem[] getContributionItems() {
    List<IContributionItem> list = new ArrayList<IContributionItem>();

    if (!onlyAccountRelated) {
      // Add new project wiz only when RUN@cloud is available
      if (CloudBeesCorePlugin.validateRUNatCloudJRE()) {
        CommandContributionItemParameter params = new CommandContributionItemParameter(CloudBeesUIPlugin.getDefault()
            .getWorkbench(), null, "com.cloudbees.eclipse.run.ui.commands.newSampleWebApp",
            CommandContributionItem.STYLE_PUSH);
        params.label = "New CloudBees ClickStart Project...";
        params.mnemonic = "N";
        params.tooltip = "Create a new project from ClickStart templates";
        params.icon = CloudBeesUIPlugin.getImageDescription(CBImages.ICON_16X16_NEW_CB_PROJ_WIZ);
        list.add(new CommandContributionItem(params));
      }

      {
        CommandContributionItemParameter params = new CommandContributionItemParameter(CloudBeesUIPlugin.getDefault()
            .getWorkbench(), null, "com.cloudbees.eclipse.ui.commands.showCloudBeesView",
            CommandContributionItem.STYLE_PUSH);
        params.label = "CloudBees View";
        params.mnemonic = "V";
        params.tooltip = "Open CloudBees View";
        params.icon = CloudBeesUIPlugin.getImageDescription(CBImages.ICON_16X16_CB_PLAIN);
        list.add(new CommandContributionItem(params));
      }

      list.add(new Separator());
    }

    addActiveAccount(list);
    

    {
      CommandContributionItemParameter params = new CommandContributionItemParameter(CloudBeesUIPlugin.getDefault()
          .getWorkbench(), null, "com.cloudbees.eclipse.run.ui.commands.cloudBeesAccount",
          CommandContributionItem.STYLE_PUSH);
      params.label = "Configure...";
      params.mnemonic = "C";
      params.tooltip = "Manage CloudBees Eclipse Toolkit settings";
      //params.icon = CloudBeesUIPlugin.getImageDescription(CBImages.ICON_16X16_CB_PLAIN);
      list.add(new CommandContributionItem(params));
    }

    return list.toArray(new IContributionItem[0]);

  }

  private void addActiveAccount(List<IContributionItem> list) {
    try {
      final String accountName = CloudBeesUIPlugin.getDefault().getActiveAccountName(new NullProgressMonitor());

      String email = CloudBeesCorePlugin.getDefault().getGrandCentralService().getEmail();
      final String accounts[] = CloudBeesCorePlugin.getDefault().getGrandCentralService().getCachedAccounts();

      if (accounts != null && accounts.length >= 1) {
        String post = "";
        if (accountName != null && accountName.length() > 0) {
          post = " (" + accountName + ")";
        }

        MenuManager submenuActiveAccount = new MenuManager(email + post, "com.cloudbees.eclipse.ui.activeAccount");

        submenuActiveAccount.add(new CompoundContributionItem("accountitems") {
          protected IContributionItem[] getContributionItems() {
            // Here's where you would dynamically generate your list
            List<IContributionItem> sublist = new ArrayList<IContributionItem>();

            for (String ac : accounts) {
              ActionContributionItem item = new ActionContributionItem(new SelectAccountAction(ac, ac
                  .equals(accountName)));
              sublist.add(item);
            }

            return sublist.toArray(new IContributionItem[0]);
          }
        });
        list.add(submenuActiveAccount);

      }

    } catch (CloudBeesException e) {
      e.printStackTrace();
    }
  }

}
