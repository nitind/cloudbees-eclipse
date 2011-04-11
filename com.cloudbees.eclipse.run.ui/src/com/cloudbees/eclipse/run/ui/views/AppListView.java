package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * View for showing all available apps in RUN@cloud
 * 
 * @author ahtik
 */
public class AppListView extends ViewPart implements IPropertyChangeListener {

  public static final String ID = "com.cloudbees.eclipse.run.ui.views.AppListView";

  private TableViewer table;

  public AppListView() {
    super();
  }

  @Override
  public void createPartControl(final Composite parent) {


    this.table = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);

    this.table.getTable().setHeaderVisible(true);

    //this.table.setInput(getViewSite());
  }


  @Override
  public void setFocus() {
    this.table.getControl().setFocus();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    // TODO Auto-generated method stub

  }

}
