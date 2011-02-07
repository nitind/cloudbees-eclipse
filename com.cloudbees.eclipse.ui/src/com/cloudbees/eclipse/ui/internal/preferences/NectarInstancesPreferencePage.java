package com.cloudbees.eclipse.ui.internal.preferences;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.cloudbees.eclipse.core.domain.NectarInstance;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

/**
 * CloudBees NectarInfo instances configuration
 * 
 * @author ahtik
 */

public class NectarInstancesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
  protected Table table;
  protected Button btnAdd;
  protected Button btnEdit;
  protected Button btnRemove;

  public NectarInstancesPreferencePage() {
    setPreferenceStore(CloudBeesUIPlugin.getDefault().getPreferenceStore());
    setDescription("Configure connection information for your instances that are not hosted at CloudBees JaaS.");//TODO i18n

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }

  @Override
  protected Control createContents(Composite parent) {
    parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Composite comp = new Composite(parent, SWT.NONE);
    GridLayout gl_comp = new GridLayout(1, false);
    gl_comp.marginWidth = 0;
    gl_comp.verticalSpacing = 0;
    gl_comp.horizontalSpacing = 0;
    comp.setLayout(gl_comp);

    Label lblConfiguredNectars = new Label(comp, SWT.NONE);
    lblConfiguredNectars.setText("Available Jenkins instances:"); //TODO i18n

    Composite compositeNectarInstances = new Composite(comp, SWT.NONE);
    compositeNectarInstances.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    GridLayout gl_compositeNectarInstances = new GridLayout(2, false);
    gl_compositeNectarInstances.marginWidth = 0;
    compositeNectarInstances.setLayout(gl_compositeNectarInstances);

    Composite compositeTable = new Composite(compositeNectarInstances, SWT.NONE);
    compositeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    GridLayout gl_compositeTable = new GridLayout(1, false);
    gl_compositeTable.marginHeight = 0;
    gl_compositeTable.marginWidth = 0;
    compositeTable.setLayout(gl_compositeTable);

    table = new Table(compositeTable, SWT.BORDER | SWT.FULL_SELECTION);
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    
    table.addSelectionListener(new SelectionListener() {
      
      public void widgetSelected(SelectionEvent e) {
        enableButtons();
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        enableButtons();
      }
    });

    TableColumn tblclmnLabel = new TableColumn(table, SWT.NONE);
    tblclmnLabel.setWidth(217);
    tblclmnLabel.setText("Label");//TODO i18n

    TableColumn tblclmnUrl = new TableColumn(table, SWT.NONE);
    tblclmnUrl.setWidth(53);
    tblclmnUrl.setText("Url");//TODO i18n

    Composite compositeButtons = new Composite(compositeNectarInstances, SWT.NONE);
    GridLayout gl_compositeButtons = new GridLayout(1, false);
    gl_compositeButtons.marginHeight = 0;
    gl_compositeButtons.marginWidth = 0;
    compositeButtons.setLayout(gl_compositeButtons);
    compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));

    btnAdd = new Button(compositeButtons, SWT.PUSH);
    GridData gd_btnAdd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    gd_btnAdd.widthHint = 70;
    btnAdd.setLayoutData(gd_btnAdd);
    btnAdd.setText("&Add...");

    btnEdit = new Button(compositeButtons, SWT.NONE);
    btnEdit.setEnabled(false);
    GridData gd_btnEdit = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    gd_btnEdit.widthHint = 70;
    btnEdit.setLayoutData(gd_btnEdit);
    btnEdit.setText("E&dit...");

    btnRemove = new Button(compositeButtons, SWT.NONE);
    btnRemove.setEnabled(false);
    GridData gd_btnRemove = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    gd_btnRemove.widthHint = 70;
    btnRemove.setLayoutData(gd_btnRemove);
    btnRemove.setText("&Remove");
    
    SelectionAdapter addListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        Shell parent = CloudBeesUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        WizardDialog dialog = new NectarWizardDialog(parent);
        dialog.create();
        dialog.getShell().setSize(Math.max(400, dialog.getShell().getSize().x), 400);
        dialog.open();
        loadTable();
      }
    };
    btnAdd.addSelectionListener(addListener);

    SelectionAdapter editListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        TableItem[] items = NectarInstancesPreferencePage.this.table.getSelection();
        if (items == null || items.length <= 0) {
          return;
        }
        NectarInstance ni = (NectarInstance) items[0].getData();
        
        Shell parent = CloudBeesUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        WizardDialog dialog = new NectarWizardDialog(parent, ni);
        dialog.create();
        dialog.getShell().setSize(Math.max(400, dialog.getShell().getSize().x), 400);
        dialog.open();
        loadTable();
      }
    };
    btnEdit.addSelectionListener(editListener);

    SelectionAdapter removeListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        TableItem[] items = NectarInstancesPreferencePage.this.table.getSelection();
        int[] itemIndices = NectarInstancesPreferencePage.this.table.getSelectionIndices();
        if (items == null || items.length <= 0) {
          return;
        }

        for (TableItem item : items) {
          NectarInstance ni = (NectarInstance) item.getData();
          CloudBeesUIPlugin.getDefault().removeNectarInstance(ni);
        }

        NectarInstancesPreferencePage.this.table.remove(itemIndices);
      }
    };
    btnRemove.addSelectionListener(removeListener);

    loadTable();
    return comp;

  }

  private void loadTable() {
    // FIXME preserve selection on reload

    table.removeAll();

    List<NectarInstance> insts = CloudBeesUIPlugin.getDefault().loadManualNectarInstances();
    Collections.sort(insts, new Comparator<NectarInstance>() {
      public int compare(NectarInstance o1, NectarInstance o2) {
        return o1.label.compareTo(o2.label);
      }
    });

    for (NectarInstance instance : insts) {
      TableItem tableItem = new TableItem(table, SWT.NONE);
      tableItem.setText(new String[] { instance.label, instance.url });
      tableItem.setData(instance);
    }

    if (table.getItemCount() > 0) {
      table.setSelection(0);
    }

    enableButtons();
  }

  private void enableButtons() {
    TableItem[] items = table.getSelection();
    NectarInstancesPreferencePage.this.btnEdit.setEnabled(items != null && items.length > 0);
    NectarInstancesPreferencePage.this.btnRemove.setEnabled(items != null && items.length > 0);
  }
}
