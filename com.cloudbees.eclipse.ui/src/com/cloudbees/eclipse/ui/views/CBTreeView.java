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
package com.cloudbees.eclipse.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CBRemoteChangeAdapter;
import com.cloudbees.eclipse.core.CBRemoteChangeListener;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.Region;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.ui.AuthStatus;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.internal.ActiveAccountAndRegionContributionItem;
import com.cloudbees.eclipse.ui.internal.ConfigureSshKeysAction;
import com.cloudbees.eclipse.ui.internal.ShowConsoleAction;
import com.cloudbees.eclipse.ui.views.CBTreeSeparator.SeparatorLocation;

public class CBTreeView extends ViewPart {

  public static final String ID = "com.cloudbees.eclipse.ui.views.CBTreeView";

  private TreeViewer viewer;

  private ICBTreeProvider[] providers;
  
  private CBTreeAction configureSshAction = new ConfigureSshKeysAction();
  private CBTreeAction showConsoleAction = new ShowConsoleAction();
  //private CBTreeAction configureAccountAction = new ConfigureCloudBeesAction();

  
  private CBRemoteChangeListener changeListener = new CBRemoteChangeAdapter() {
    public void activeAccountChanged(final String email, final String newAccountName, final Region region) {
      
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          updateCD(email, newAccountName, region);
        }     
      });
      
    }
  };

  private IMenuListener statusUpdateListener = new IMenuListener() {
    
    public void menuAboutToShow(IMenuManager manager) {
      IActionBars bars = getViewSite().getActionBars();
      IMenuManager pullDownMenu = bars.getMenuManager();
      IContributionItem[] items = pullDownMenu.getItems();
      for (int i = 0; i < items.length; i++) {
        IContributionItem it = items[i];
        if (it instanceof ActionContributionItem) {
          ActionContributionItem ita = (ActionContributionItem) it;
          //System.out.println("Action: "+ita.getAction());
          ita.update();          
        }
        //System.out.println("IT: "+it);
      }
      /*pullDownMenu.markDirty();
      pullDownMenu.updateAll(true);*/
      //System.out.println("About to show a menu!");
    }
  };
  
  class NameSorter extends ViewerSorter {

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
      if (e1 instanceof ICBGroup && e2 instanceof ICBGroup) {
        return ((ICBGroup) e1).getOrder() - ((ICBGroup) e2).getOrder();
      }

      if (e1 instanceof ForgeInstance && e2 instanceof ForgeInstance) {
        return ((ForgeInstance)e1).compareTo((ForgeInstance) e2);
      }
      
      return super.compare(viewer, e1, e2);
    }

  }

  public CBTreeView() {
    super();
    
    GrandCentralService gcs;
    try {
      gcs = CloudBeesCorePlugin.getDefault().getGrandCentralService();

      String accountName  = gcs.getActiveAccountName();
      String email = gcs.getEmail();
      Region region = gcs.getActiveRegion();
      updateCD(email, accountName, region);
      
    } catch (CloudBeesException e1) {
      //Ignore for now
    }
        
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CloudBeesUIPlugin.PLUGIN_ID, "cbTreeProvider").getExtensions();

    List<ICBTreeProvider> prs = new ArrayList<ICBTreeProvider>();
    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object provider = element.createExecutableExtension("class");
          if (provider instanceof ICBTreeProvider) {
            prs.add((ICBTreeProvider) provider);
          }
        } catch (Exception e) {
          e.printStackTrace(); // FIXME
        }
      }
    }

    this.providers = prs.toArray(new ICBTreeProvider[prs.size()]);
    Arrays.sort(this.providers, new Comparator<ICBTreeProvider>() {

      public int compare(ICBTreeProvider o1, ICBTreeProvider o2) {
        return o1.getId().compareTo(o2.getId());
      }});
    
    CloudBeesUIPlugin.getDefault().addCBRemoteChangeListener(changeListener);
    
  }

  public ICBTreeProvider[] getProviders() {
    return this.providers;
  }

  @Override
  public void createPartControl(final Composite parent) {
    this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    this.viewer.setContentProvider(new CBContentProvider(this));
    this.viewer.setLabelProvider(new CBLabelProvider(this));
    this.viewer.setSorter(new NameSorter());
    this.viewer.setInput(getViewSite());

    for (ICBTreeProvider provider : this.providers) {
      provider.setViewer(this.viewer);
      getSite().setSelectionProvider(this.viewer);
    }

    this.viewer.addOpenListener(new IOpenListener() {
      public void open(final OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof TreeSelection) {

          Object el = ((TreeSelection) sel).getFirstElement();

          //boolean opened = false;
          for (ICBTreeProvider provider : CBTreeView.this.providers) {
            //opened |= ;
            if (provider.open(el)) {
              break;
            }
          }
/*          if (!opened) {
            boolean exp = CBTreeView.this.viewer.getExpandedState(el);
            if (exp) {
              CBTreeView.this.viewer.collapseToLevel(el, 1);
            } else {
              CBTreeView.this.viewer.expandToLevel(el, 1);
            }
          }
*/        }
      }
    });

    MenuManager popupMenu = new MenuManager();
    popupMenu.setRemoveAllWhenShown(true);
    
    popupMenu.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(final IMenuManager mgr) {
        
      }
    });

    IActionBars bars = getViewSite().getActionBars();
    IMenuManager pullDownMenu = bars.getMenuManager();
    IToolBarManager toolbarMenu = bars.getToolBarManager();

    for (ICBTreeProvider provider : this.providers) {
      for (CBTreeContributor contributor : provider.getContributors()) {
        if (contributor.isPopup()) {
          contributor.contributeTo(popupMenu);
        }
        if (contributor.isPullDown()) {
          contributor.contributeTo(pullDownMenu);
        }
        if (contributor.isToolbar()) {
          contributor.contributeTo(toolbarMenu);
        }
      }
    }
    
    pullDownMenu.add(new CBTreeSeparator(SeparatorLocation.PULL_DOWN));
    pullDownMenu.add(this.showConsoleAction);
    pullDownMenu.add(new CBTreeSeparator(SeparatorLocation.PULL_DOWN));
    pullDownMenu.add(this.configureSshAction);
    pullDownMenu.add(new CBTreeSeparator(SeparatorLocation.PULL_DOWN));
    pullDownMenu.add(new ActiveAccountAndRegionContributionItem(true));
    
    pullDownMenu.addMenuListener(statusUpdateListener);
    //pullDownMenu.add(this.configureAccountAction);
    
    
    

    Menu menu = popupMenu.createContextMenu(this.viewer.getTree());
    this.viewer.getTree().setMenu(menu);
    getSite().registerContextMenu(popupMenu, this.viewer);

    //Not loading here anymore as there is now improved loading workflow
    //CloudBeesUIPlugin.getDefault().reloadAllJenkins(false);
    
  }

  @Override
  public void setFocus() {
    this.viewer.getControl().setFocus();
  }

  @Override
  public void dispose() {
    for (ICBTreeProvider provider : this.providers) {
      provider.dispose();
    }
    this.providers = null;

    CloudBeesUIPlugin.getDefault().removeCBRemoteChangeListener(changeListener);
    
    super.dispose();
  }


  private void updateCD(String email, String newAccountName, Region region) {
    if (email==null) {
      CBTreeView.this.setContentDescription("Account not configured!");
      return;
    }
    if (newAccountName!=null && newAccountName.length()>0) {
      CBTreeView.this.setContentDescription(" "+email+"; "+newAccountName+"; region: "+region.getLabel());
      return;
    }
    String post="";
    if (CloudBeesUIPlugin.getDefault().getAuthStatus()!=AuthStatus.OK) {
      post=" (user not authenticated!)";
    }
    CBTreeView.this.setContentDescription(" "+email+post);
  }
}
