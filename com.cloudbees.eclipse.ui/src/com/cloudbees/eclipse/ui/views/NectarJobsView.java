package com.cloudbees.eclipse.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * View showing jobs for both Nectar offline installations and HaaS Nectar instances
 * 
 * @author ahtik
 */
public class NectarJobsView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.ui.views.NectarJobsView";

  private TreeViewer viewer;

  private Action action1; // Configure Account
  private Action action2; // Attach Nectar
  private Action action3; // Reload Forge repositories

  class TreeObject implements IAdaptable {
    private String name;
    private TreeParent parent;

    public TreeObject(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setParent(TreeParent parent) {
      this.parent = parent;
    }

    public TreeParent getParent() {
      return parent;
    }

    public String toString() {
      return getName();
    }

    public Object getAdapter(Class key) {
      return null;
    }
  }

  class TreeParent extends TreeObject {
    private List<TreeObject> children;

    public TreeParent(String name) {
      super(name);
      children = new ArrayList<TreeObject>();
    }

    public void addChild(TreeObject child) {
      children.add(child);
      child.setParent(this);
    }

    public void removeChild(TreeObject child) {
      children.remove(child);
      child.setParent(null);
    }

    public TreeObject[] getChildren() {
      return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
    }

    public boolean hasChildren() {
      return children.size() > 0;
    }
  }

  class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
    private TreeParent invisibleRoot;

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
      if (parent.equals(getViewSite())) {
        if (invisibleRoot == null)
          initialize();
        return getChildren(invisibleRoot);
      }
      return getChildren(parent);
    }

    public Object getParent(Object child) {
      if (child instanceof TreeObject) {
        return ((TreeObject) child).getParent();
      }
      return null;
    }

    public Object[] getChildren(Object parent) {
      if (parent instanceof TreeParent) {
        return ((TreeParent) parent).getChildren();
      }
      return new Object[0];
    }

    public boolean hasChildren(Object parent) {
      if (parent instanceof TreeParent)
        return ((TreeParent) parent).hasChildren();
      return false;
    }

    private void initialize() {
      TreeObject to1 = new TreeObject("job 1");
      TreeParent p1 = new TreeParent("HaaS");
      p1.addChild(to1);

      TreeObject to4 = new TreeObject("instance 1");
      TreeParent p2 = new TreeParent("Nectar");
      p2.addChild(to4);

      TreeParent root = new TreeParent("Jobs");
      root.addChild(p1);
      root.addChild(p2);

      invisibleRoot = new TreeParent("");
      invisibleRoot.addChild(root);
    }
  }

  class ViewLabelProvider extends LabelProvider {

    public String getText(Object obj) {
      return obj.toString();
    }

    public Image getImage(Object obj) {
      String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
      if (obj instanceof TreeParent)
        imageKey = ISharedImages.IMG_OBJ_FOLDER;
      return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
    }
  }

  class NameSorter extends ViewerSorter {
  }

  public void createPartControl(Composite parent) {
    viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    viewer.setContentProvider(new ViewContentProvider());
    viewer.setLabelProvider(new ViewLabelProvider());
    viewer.setSorter(new NameSorter());
    viewer.setInput(getViewSite());

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
    action2.setText("Attach Nectar instances...");
    action2.setToolTipText("Attach more Nectar instances to monitor");

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
  }

  @Override
  public void dispose() {
    CloudBeesUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    super.dispose();
  }
}
