package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

/**
 * View for showing all available apps in RUN@cloud
 * 
 * @author ahtik
 */
public class AppListView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.run.ui.views.AppListView";

  private TreeViewer tree;

  public AppListView() {
    super();
  }

  @Override
  public void createPartControl(final Composite parent) {

    this.tree = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
    this.tree.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        if (element instanceof ApplicationInfo) {
          ApplicationInfo elem = (ApplicationInfo) element;
          return elem.getId() + " (" + elem.getStatus() + ")";
        }
        return super.getText(element);
      }
    });

    this.tree.setContentProvider(new ITreeContentProvider() {

      @Override
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }

      @Override
      public void dispose() {
      }

      @Override
      public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ApplicationListResponse) {
          return ((ApplicationListResponse) inputElement).getApplications().toArray();
        }
        return null;
      }

      @Override
      public Object[] getChildren(Object parentElement) {
        return null;
      }

      @Override
      public Object getParent(Object element) {
        return null;
      }

      @Override
      public boolean hasChildren(Object element) {
        return false;
      }

    });

    Button button = new Button(parent, SWT.NONE);
    button.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          ApplicationListResponse list = BeesSDK.getList();
          AppListView.this.tree.setInput(list);
          AppListView.this.tree.refresh(true);
        } catch (Exception e1) {
          CBRunUiActivator.logError(e1);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {

      }
    });

    //this.table.setInput(getViewSite());
  }

  @Override
  public void setFocus() {
    this.tree.getControl().setFocus();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
  }

}
