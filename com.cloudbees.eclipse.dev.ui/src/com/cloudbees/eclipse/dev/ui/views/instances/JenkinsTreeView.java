package com.cloudbees.eclipse.dev.ui.views.instances;

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
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.dev.ui.utils.FavouritesUtils;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.cloudbees.eclipse.ui.internal.action.ConfigureCloudBeesAction;

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
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
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

  @Override
  public void createPartControl(final Composite parent) {
    this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    this.viewer.setContentProvider(new InstanceContentProvider());
    this.viewer.setLabelProvider(new InstanceLabelProvider());
    this.viewer.setSorter(new NameSorter());
    this.viewer.setInput(getViewSite());

    this.viewer.addOpenListener(new IOpenListener() {

      @Override
      public void open(final OpenEvent event) {
        ISelection sel = event.getSelection();

        if (sel instanceof TreeSelection) {

          Object el = ((TreeSelection) sel).getFirstElement();

          if (el instanceof BaseJenkinsResponse) {

            BaseJenkinsResponse resp = (BaseJenkinsResponse) el;
            try {
              CloudBeesDevUiPlugin.getDefault().showJobs(resp.viewUrl, true);
            } catch (CloudBeesException e) {
              CloudBeesUIPlugin.getDefault().getLogger().error(e);
            }
            return;
          } else if (el instanceof JenkinsInstanceResponse.View) {
            try {
              CloudBeesDevUiPlugin.getDefault().showJobs(((JenkinsInstanceResponse.View) el).url, true);
            } catch (CloudBeesException e) {
              CloudBeesUIPlugin.getDefault().getLogger().error(e);
            }
            return;
          } else if (el instanceof FavouritesInstanceGroup) {

            try {
              CloudBeesDevUiPlugin.getDefault().showJobs(FavouritesUtils.FAVOURITES, true);
            } catch (CloudBeesException e) {
              CloudBeesUIPlugin.getDefault().getLogger().error(e);
            }

          } else if (el instanceof InstanceGroup) {
            boolean exp = JenkinsTreeView.this.viewer.getExpandedState(el);
            if (exp) {
              JenkinsTreeView.this.viewer.collapseToLevel(el, 1);
            } else {
              JenkinsTreeView.this.viewer.expandToLevel(el, 1);
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

    this.jenkinsChangeListener = new JenkinsChangeListener() {
      @Override
      public void activeJobViewChanged(final JenkinsJobsResponse newView) {
      }

      public void activeJobHistoryChanged(final JenkinsJobAndBuildsResponse newView) {
      }

      @Override
      public void jenkinsChanged(final List<JenkinsInstanceResponse> instances) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            JenkinsTreeView.this.viewer.getContentProvider().inputChanged(JenkinsTreeView.this.viewer, null, instances);
          }
        });
      }
    };

    CloudBeesUIPlugin.getDefault().addJenkinsChangeListener(this.jenkinsChangeListener);

    CloudBeesUIPlugin.getDefault().reloadAllJenkins(false);
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    //fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalPullDown(final IMenuManager manager) {
    manager.add(this.action1);
    manager.add(this.action2);
    manager.add(new Separator());
    manager.add(this.action3);
    manager.add(this.action4);
  }

  private void makeActions() {
    this.action1 = new ConfigureCloudBeesAction();
    this.action2 = new Action() {
      @Override
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
    this.action2.setText("Attach Jenkins instances...");
    this.action2.setToolTipText("Attach more Jenkins instances to monitor");

    /*		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */

    this.action3 = new Action() {
      @Override
      public void run() {
        try {
          CloudBeesDevUiPlugin.getDefault().reloadForgeRepos(true);
        } catch (CloudBeesException e) {
          //TODO i18n
          CloudBeesUIPlugin.showError("Failed to reload Forge repositories!", e);
        }
      }
    };
    this.action3.setText("Reload Forge repositories");
    this.action3.setToolTipText("Reload Forge repositories and create local entries");
    /*		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
    this.action3.setEnabled(forgeEnabled);

    this.action4 = new Action() {
      @Override
      public void run() {
        CloudBeesUIPlugin.getDefault().reloadAllJenkins(true);
      }
    };
    this.action4.setText("Reload Jenkins instances");
    this.action4.setToolTipText("Reload Jenkins instances");
    /*    action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */
    CloudBeesUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
        .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
    this.action4.setEnabled(jaasEnabled);
  }

  @Override
  public void setFocus() {
    this.viewer.getControl().setFocus();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (PreferenceConstants.P_ENABLE_FORGE.equals(event.getProperty())) {
      boolean forgeEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_FORGE);
      this.action3.setEnabled(forgeEnabled);
    }

    if (PreferenceConstants.P_ENABLE_JAAS.equals(event.getProperty())) {
      boolean jaasEnabled = CloudBeesUIPlugin.getDefault().getPreferenceStore()
          .getBoolean(PreferenceConstants.P_ENABLE_JAAS);
      this.action4.setEnabled(jaasEnabled);
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
    CloudBeesUIPlugin.getDefault().removeJenkinsChangeListener(this.jenkinsChangeListener);
    this.jenkinsChangeListener = null;

    super.dispose();
  }

}
