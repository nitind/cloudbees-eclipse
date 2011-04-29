package com.cloudbees.eclipse.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class CBTreeView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.ui.views";

  private TreeViewer viewer;
  //private JenkinsChangeListener jenkinsChangeListener;

  private ICBTreeProvider[] providers;

  class NameSorter extends ViewerSorter {

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
      // small hack
      if (e1.getClass().getSimpleName().startsWith("Favor")) {
        return -1;
      }
      if (e2.getClass().getSimpleName().startsWith("Favor")) {
        return +1;
      }

      return super.compare(viewer, e1, e2);
    }

  }

  public CBTreeView() {

    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CloudBeesUIPlugin.PLUGIN_ID, "cbTreeProvider")
        .getExtensions();

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

    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
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
    }

    this.viewer.addOpenListener(new IOpenListener() {
      public void open(final OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof TreeSelection) {

          Object el = ((TreeSelection) sel).getFirstElement();

          boolean opened = false;
          for (ICBTreeProvider provider : CBTreeView.this.providers) {
            opened |= provider.open(el);
            if (opened) {
              break;
            }
          }

          if (!opened) {
            boolean exp = CBTreeView.this.viewer.getExpandedState(el);
            if (exp) {
              CBTreeView.this.viewer.collapseToLevel(el, 1);
            } else {
              CBTreeView.this.viewer.expandToLevel(el, 1);
            }
          }
        }
      }
    });

    MenuManager popupMenu = new MenuManager();
    IActionBars bars = getViewSite().getActionBars();
    IMenuManager pullDownMenu = bars.getMenuManager();
    IToolBarManager toolbarMenu = bars.getToolBarManager();

    for (ICBTreeProvider provider : this.providers) {
      for (CBTreeAction action : provider.getActions()) {
        if (action.isPopup()) {
          popupMenu.add(action);
        }
        if (action.isPullDown()) {
          pullDownMenu.add(action);
        }
        if (action.isToolbar()) {
          toolbarMenu.add(action);
        }
      }
    }

    Menu menu = popupMenu.createContextMenu(this.viewer.getTree());
    this.viewer.getTree().setMenu(menu);

//    this.jenkinsChangeListener = new JenkinsChangeListener() {
    //      @Override
    //      public void activeJobViewChanged(final JenkinsJobsResponse newView) {
    //      }
    //
    //      @Override
    //      public void activeJobHistoryChanged(final JenkinsJobAndBuildsResponse newView) {
    //      }
    //
    //      @Override
    //      public void jenkinsChanged(final List<JenkinsInstanceResponse> instances) {
    //        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
    //          @Override
    //          public void run() {
    //            JenkinsTreeView.this.viewer.getContentProvider().inputChanged(JenkinsTreeView.this.viewer, null, instances);
    //          }
    //        });
    //      }
    //    };
    //
    //    CloudBeesUIPlugin.getDefault().addJenkinsChangeListener(this.jenkinsChangeListener);

    CloudBeesUIPlugin.getDefault().reloadAllJenkins(false);
  }

  @Override
  public void setFocus() {
    this.viewer.getControl().setFocus();
  }

  public void propertyChange(final PropertyChangeEvent event) {
    //    if (PreferenceConstants.P_ENABLE_FORGE.equals(event.getProperty())) {
    //      boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
    //          .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    //      this.action3.setEnabled(forgeEnabled);
    //    }
    //
    //    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())) {
    //      boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
    //          .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
    //      this.action4.setEnabled(jaasEnabled);
    //    }
    //
    //    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())
    //        || PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())
    //        || PreferenceConstants.P_EMAIL.equals(event.getProperty())
    //        || PreferenceConstants.P_PASSWORD.equals(event.getProperty())) {
    //      CloudBeesUIPlugin.getDefault().reloadAllJenkins(false);
    //    }
  }

  @Override
  public void dispose() {
    for (ICBTreeProvider provider : this.providers) {
      provider.dispose();
    }
    this.providers = null;

    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    //    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(this.jenkinsChangeListener);
    //    this.jenkinsChangeListener = null;

    super.dispose();
  }

}
