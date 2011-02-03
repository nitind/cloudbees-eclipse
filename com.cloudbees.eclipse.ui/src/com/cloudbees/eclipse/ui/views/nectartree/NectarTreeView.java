package com.cloudbees.eclipse.ui.views.nectartree;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.nectar.api.BaseNectarResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse.View;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * View showing jobs for both NectarInfo offline installations and JaaS Nectar instances
 * 
 * @author ahtik
 */
public class NectarTreeView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.ui.views.nectartree.NectarTreeView";

  private TreeViewer viewer;

  private final Logger log = CloudBeesUIPlugin.getDefault().getLogger();

  private Action action1; // Configure Account
  private Action action2; // Attach NectarInfo
  private Action action3; // Reload Forge repositories
  private Action action4; // Reload Jaas repositories

  class NameSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
      if (e1 instanceof NectarInstanceResponse.View && e2 instanceof NectarInstanceResponse.View) {
        NectarInstanceResponse.View v1 = (View) e1;
        NectarInstanceResponse.View v2 = (View) e2;
        if (v1.name != null && v2.name != null) {
          return v1.name.compareTo(v2.name);
        }

      }

      //TODO Add ordering for the instance labels

      return super.compare(viewer, e1, e2);
    }

  }

  public void createPartControl(Composite parent) {
    viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    viewer.setContentProvider(new InstanceContentProvider(getViewSite()));
    viewer.setLabelProvider(new InstanceLabelProvider());
    viewer.setSorter(new NameSorter());
    viewer.setInput(getViewSite());

    viewer.addOpenListener(new IOpenListener() {

      public void open(OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof TreeSelection) {

          Object el = ((TreeSelection) sel).getFirstElement();

          if (el instanceof BaseNectarResponse) {

            BaseNectarResponse resp = (BaseNectarResponse) el;
            try {
              CloudBeesUIPlugin.getDefault().showJobs(resp.serviceUrl, resp.viewUrl);
            } catch (CloudBeesException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            return;
          } else if (el instanceof NectarInstanceResponse.View) {
            try {
              CloudBeesUIPlugin.getDefault().showJobs(null, ((NectarInstanceResponse.View) el).url);
            } catch (CloudBeesException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            return;
          } else if (el instanceof InstanceGroup) {
            boolean exp = viewer.getExpandedState(el);
            if (exp) {
              viewer.collapseToLevel(el, 1);
            } else {
              viewer.expandToLevel(el, 1);
            }
          }
        }

        /*
        Source [com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse@1d724f31]
        Source [com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse$View@772fbca]
        */

        /*        if (event.getSelection() instanceof NectarInstanceResponse[]) {
                  
                }
        */

      }
    });

    viewer.expandToLevel(2);

    makeActions();
    contributeToActionBars();

  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    //fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalPullDown(IMenuManager manager) {
    manager.add(action1);
    manager.add(action2);
    manager.add(new Separator());
    manager.add(action3);
    manager.add(action4);
  }

  private void makeActions() {
    action1 = new Action() {
      public void run() {
        PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
            "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage", new String[] {
                "com.cloudbees.eclipse.ui.preferences.NectarInstancesPreferencePage",
                "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage" }, null);
        if (pref != null) {
          pref.open();
        }
      }
    };
    action1.setText("Configure CloudBees access...");
    action1.setToolTipText("Configure CloudBees account access");
    /*		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */
    action2 = new Action() {
      public void run() {
        PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
            "com.cloudbees.eclipse.ui.preferences.NectarInstancesPreferencePage", new String[] {
                "com.cloudbees.eclipse.ui.preferences.NectarInstancesPreferencePage",
                "com.cloudbees.eclipse.ui.internal.preferences.GeneralPreferencePage" }, null);
        if (pref != null) {
          pref.open();
        }
      }
    };
    action2.setText("Attach NectarInfo instances...");
    action2.setToolTipText("Attach more NectarInfo instances to monitor");

    /*		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */

    action3 = new Action() {
      public void run() {
        try {
          CloudBeesUIPlugin.getDefault().reloadForgeRepos();
        } catch (CloudBeesException e) {
          //TODO I18n!
          CloudBeesUIPlugin.showError("Failed to reload Forge repositories!", e);
        }
      }
    };
    action3.setText("Reload Forge repositories...");
    action3.setToolTipText("Reload Forge repositories and create local entries");
    /*		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    action3.setEnabled(forgeEnabled);

    action4 = new Action() {
      public void run() {
        viewer.getContentProvider().inputChanged(viewer, null, null); // make it refresh itself
        viewer.refresh();
      }
    };
    action4.setText("Reload Nectar instances...");
    action4.setToolTipText("Reload Nectar instances");
    /*    action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
    action4.setEnabled(jaasEnabled);
  }

  public void setFocus() {
    viewer.getControl().setFocus();
  }

  public void propertyChange(PropertyChangeEvent event) {
    if (PreferenceConstants.P_ENABLE_FORGE.equals(event.getProperty())) {
      boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
      action3.setEnabled(forgeEnabled);
    }

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())) {
      boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
      action4.setEnabled(jaasEnabled);
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    super.dispose();
  }
}
