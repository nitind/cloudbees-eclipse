package com.cloudbees.eclipse.dev.ui.views.forge;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance.STATUS;

public class ForgeSyncConfirmation extends Dialog {

  List<ForgeInstance> repos;
  List<ForgeInstance> selectedRepos;

  private static class Sorter extends ViewerSorter {
    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
      if (e1 instanceof ForgeInstance && e2 instanceof ForgeInstance) {
        return ((ForgeInstance) e1).url.compareToIgnoreCase(((ForgeInstance) e2).url);
      }
      return 0;
    }
  }

  private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
    @Override
    public Image getColumnImage(final Object element, final int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(final Object element, final int columnIndex) {
      return ((ForgeInstance) element).url;
    }
  }

  private static class ContentProvider implements IStructuredContentProvider {
    @Override
    public Object[] getElements(final Object inputElement) {
      return (ForgeInstance[]) inputElement;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    }
  }

  private Table table;
  CheckboxTableViewer checkboxTableViewer;

  /**
   * Create the dialog.
   * 
   * @param parentShell
   */
  public ForgeSyncConfirmation(final Shell parentShell, final List<ForgeInstance> repos) {
    super(parentShell);
    setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    this.repos = repos;
  }

  /**
   * Create contents of the dialog.
   * 
   * @param parent
   */
  @Override
  protected Control createDialogArea(final Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);

    Label lblSelectForgeRepositories = new Label(container, SWT.NONE);
    lblSelectForgeRepositories.setText("Configure Forge repositories for this Eclipse workspace:");

    this.checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
        | SWT.BORDER | SWT.FULL_SELECTION);
    this.checkboxTableViewer.setSorter(new Sorter());
    this.table = this.checkboxTableViewer.getTable();
    this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    TableColumn tblclmnForgeInstanceUrl = new TableColumn(this.table, SWT.LEFT);
    tblclmnForgeInstanceUrl.setWidth(200);
    tblclmnForgeInstanceUrl.setText("Forge Instance Url");
    this.checkboxTableViewer.setLabelProvider(new TableLabelProvider());
    this.checkboxTableViewer.setContentProvider(new ContentProvider());

    this.checkboxTableViewer.setInput(this.repos.toArray(new ForgeInstance[this.repos.size()]));
    tblclmnForgeInstanceUrl.pack();

    Composite composite = new Composite(container, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    composite.setLayout(new GridLayout(2, false));

    Button btnSelectAll = new Button(composite, SWT.NONE);
    btnSelectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        ForgeSyncConfirmation.this.checkboxTableViewer.setAllChecked(true);
      }
    });
    btnSelectAll.setText("Select &All");

    Button btnDeselectAll = new Button(composite, SWT.NONE);
    btnDeselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        ForgeSyncConfirmation.this.checkboxTableViewer.setAllChecked(false);
      }
    });
    btnDeselectAll.setText("&Deselect All");

    for (ForgeInstance repo : this.repos) {
      if (repo.status == STATUS.UNKNOWN) {
        this.checkboxTableViewer.setChecked(repo, true);
      }
    }

    parent.layout(true);

    return container;
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText("Access Forge repositories");
  }

  /**
   * Create contents of the button bar.
   * 
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  /**
   * Return the initial size of the dialog.
   */
  @Override
  protected Point getInitialSize() {
    return new Point(450, 300);
  }

  @Override
  protected void okPressed() {
    Object[] checkedElements = this.checkboxTableViewer.getCheckedElements();
    if (checkedElements != null) {
      this.selectedRepos = new ArrayList<ForgeInstance>(checkedElements.length);
      for (Object o : checkedElements) {
        this.selectedRepos.add((ForgeInstance) o);
      }
    }
    super.okPressed();
  }

  public List<ForgeInstance> getSelectedRepos() {
    return this.selectedRepos;
  }
}
