package com.cloudbees.eclipse.ui.views.instances;

import java.util.List;

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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsChangeListener;
import com.cloudbees.eclipse.core.Logger;
import com.cloudbees.eclipse.core.jenkins.api.BaseJenkinsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse.View;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.cloudbees.eclipse.ui.internal.actions.ConfigureCloudBeesAction;

/**
 * View showing both Jenkins offline installations and JaaS Nectar instances
 * 
 * @author ahtik
 */
public class JenkinsTreeView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.ui.views.instances.JenkinsTreeView";

  private TreeViewer viewer;
  private JenkinsChangeListener jenkinsChangeListener;

  private final Logger log = CloudBeesUIPlugin.getDefault().getLogger();

  private ConfigureCloudBeesAction action1; // Configure Account
  private Action action2; // Attach Jenkins
  private Action action3; // Reload Forge repositories
  private Action action4; // Reload Jenkins repositories

  class NameSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
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

  public void createPartControl(Composite parent) {
    viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    viewer.setContentProvider(new InstanceContentProvider());
    viewer.setLabelProvider(new InstanceLabelProvider());
    viewer.setSorter(new NameSorter());
    viewer.setInput(getViewSite());

    viewer.addOpenListener(new IOpenListener() {

      public void open(OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof TreeSelection) {

          Object el = ((TreeSelection) sel).getFirstElement();

          if (el instanceof BaseJenkinsResponse) {

            BaseJenkinsResponse resp = (BaseJenkinsResponse) el;
            try {
              CloudBeesUIPlugin.getDefault().showJobs(resp.serviceUrl, resp.viewUrl);
            } catch (CloudBeesException e) {
              CloudBeesUIPlugin.getDefault().getLogger().error(e);
            }
            return;
          } else if (el instanceof JenkinsInstanceResponse.View) {
            try {
              CloudBeesUIPlugin.getDefault().showJobs(null, ((JenkinsInstanceResponse.View) el).url);
            } catch (CloudBeesException e) {
              CloudBeesUIPlugin.getDefault().getLogger().error(e);
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
        Source [com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse@1d724f31]
        Source [com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse$View@772fbca]
        */

        /*        if (event.getSelection() instanceof JenkinsInstanceResponse[]) {
                  
                }
        */

      }
    });

    //viewer.expandToLevel(2);

    makeActions();
    contributeToActionBars();

    jenkinsChangeListener = new JenkinsChangeListener() {
      public void activeJobViewChanged(JenkinsJobsResponse newView) {
      }

      public void jenkinsChanged(final List<JenkinsInstanceResponse> instances) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          public void run() {
            viewer.getContentProvider().inputChanged(viewer, null, instances);
          }
        });
      }
    };

    CloudBeesUIPlugin.getDefault().addJenkinsChangeListener(jenkinsChangeListener);

    CloudBeesUIPlugin.getDefault().reloadAllJenkins(false);
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
    action1 = new ConfigureCloudBeesAction();
    action2 = new Action() {
      public void run() {
        PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
            "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage", new String[] {
                "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage",
                "com.cloudbees.eclipse.ui.internal.preferences.GeneralPreferencePage" }, null);
        if (pref != null) {
          pref.open();
        }
      }
    };
    action2.setText("Attach Jenkins instances...");
    action2.setToolTipText("Attach more Jenkins instances to monitor");

    /*		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */

    action3 = new Action() {
      public void run() {
        try {
          CloudBeesUIPlugin.getDefault().reloadForgeRepos(true);
        } catch (CloudBeesException e) {
          //TODO i18n
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
        CloudBeesUIPlugin.getDefault().reloadAllJenkins(true);
      }
    };
    action4.setText("Reload Jenkins instances...");
    action4.setToolTipText("Reload Jenkins instances");
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

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())
        || PreferenceConstants.P_JENKINS_INSTANCES.equals(event.getProperty())
        || PreferenceConstants.P_EMAIL.equals(event.getProperty())
        || PreferenceConstants.P_PASSWORD.equals(event.getProperty())) {
      CloudBeesUIPlugin.getDefault().reloadAllJenkins(false);
    }
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(jenkinsChangeListener);
    jenkinsChangeListener = null;

    super.dispose();
  }

}
