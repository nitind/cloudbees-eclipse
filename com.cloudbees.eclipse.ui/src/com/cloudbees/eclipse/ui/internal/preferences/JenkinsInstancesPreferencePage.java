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

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

/**
 * CloudBees Jenkins instances configuration
 *
 * @author ahtik
 */

public class JenkinsInstancesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
  protected Table table;
  protected Button btnAdd;
  protected Button btnEdit;
  protected Button btnRemove;

  public JenkinsInstancesPreferencePage() {
    setPreferenceStore(CloudBeesUIPlugin.getDefault().getPreferenceStore());
    setDescription("Configure connection information for your instances that are not hosted at CloudBees JaaS.");//TODO i18n

  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(final IWorkbench workbench) {
  }

  @Override
  protected Control createContents(final Composite parent) {
    parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Composite comp = new Composite(parent, SWT.NONE);
    GridLayout gl_comp = new GridLayout(1, false);
    gl_comp.marginWidth = 0;
    gl_comp.verticalSpacing = 0;
    gl_comp.horizontalSpacing = 0;
    comp.setLayout(gl_comp);

    Label lblConfiguredJenkins = new Label(comp, SWT.NONE);
    lblConfiguredJenkins.setText("Available Jenkins instances:"); //TODO i18n

    Composite compositeJenkinsInstances = new Composite(comp, SWT.NONE);
    compositeJenkinsInstances.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    GridLayout gl_compositeJenkinsInstances = new GridLayout(2, false);
    gl_compositeJenkinsInstances.marginWidth = 0;
    compositeJenkinsInstances.setLayout(gl_compositeJenkinsInstances);

    Composite compositeTable = new Composite(compositeJenkinsInstances, SWT.NONE);
    compositeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    GridLayout gl_compositeTable = new GridLayout(1, false);
    gl_compositeTable.marginHeight = 0;
    gl_compositeTable.marginWidth = 0;
    compositeTable.setLayout(gl_compositeTable);

    this.table = new Table(compositeTable, SWT.BORDER | SWT.FULL_SELECTION);
    this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.table.setHeaderVisible(true);
    this.table.setLinesVisible(true);

    this.table.addSelectionListener(new SelectionListener() {

      public void widgetSelected(final SelectionEvent e) {
        enableButtons();
      }

      public void widgetDefaultSelected(final SelectionEvent e) {
        enableButtons();
      }
    });

    TableColumn tblclmnLabel = new TableColumn(this.table, SWT.NONE);
    tblclmnLabel.setWidth(120);
    tblclmnLabel.setText("Label");//TODO i18n

    TableColumn tblclmnUrl = new TableColumn(this.table, SWT.NONE);
    tblclmnUrl.setWidth(300);
    tblclmnUrl.setText("Url");//TODO i18n

    Composite compositeButtons = new Composite(compositeJenkinsInstances, SWT.NONE);
    GridLayout gl_compositeButtons = new GridLayout(1, false);
    gl_compositeButtons.marginHeight = 0;
    gl_compositeButtons.marginWidth = 0;
    compositeButtons.setLayout(gl_compositeButtons);
    compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));

    this.btnAdd = new Button(compositeButtons, SWT.PUSH);
    GridData gd_btnAdd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    gd_btnAdd.widthHint = 70;
    this.btnAdd.setLayoutData(gd_btnAdd);
    this.btnAdd.setText("&Add...");

    this.btnEdit = new Button(compositeButtons, SWT.NONE);
    this.btnEdit.setEnabled(false);
    GridData gd_btnEdit = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    gd_btnEdit.widthHint = 70;
    this.btnEdit.setLayoutData(gd_btnEdit);
    this.btnEdit.setText("E&dit...");

    this.btnRemove = new Button(compositeButtons, SWT.NONE);
    this.btnRemove.setEnabled(false);
    GridData gd_btnRemove = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    gd_btnRemove.widthHint = 70;
    this.btnRemove.setLayoutData(gd_btnRemove);
    this.btnRemove.setText("&Remove");

    SelectionAdapter addListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        Shell parent = CloudBeesUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        WizardDialog dialog = new JenkinsWizardDialog(parent);
        dialog.create();
        dialog.getShell().setSize(Math.max(400, dialog.getShell().getSize().x), 400);
        dialog.open();
        loadTable();
      }
    };
    this.btnAdd.addSelectionListener(addListener);

    SelectionAdapter editListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        TableItem[] items = JenkinsInstancesPreferencePage.this.table.getSelection();
        if (items == null || items.length <= 0) {
          return;
        }
        JenkinsInstance ni = (JenkinsInstance) items[0].getData();

        Shell parent = CloudBeesUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        WizardDialog dialog = new JenkinsWizardDialog(parent, ni);
        dialog.create();
        dialog.getShell().setSize(Math.max(400, dialog.getShell().getSize().x), 400);
        dialog.open();
        loadTable();
      }
    };
    this.btnEdit.addSelectionListener(editListener);

    SelectionAdapter removeListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        TableItem[] items = JenkinsInstancesPreferencePage.this.table.getSelection();
        int[] itemIndices = JenkinsInstancesPreferencePage.this.table.getSelectionIndices();
        if (items == null || items.length <= 0) {
          return;
        }

        for (TableItem item : items) {
          JenkinsInstance ni = (JenkinsInstance) item.getData();
          CloudBeesUIPlugin.getDefault().removeJenkinsInstance(ni);
        }

        JenkinsInstancesPreferencePage.this.table.remove(itemIndices);
      }
    };
    this.btnRemove.addSelectionListener(removeListener);

    loadTable();
    return comp;

  }

  private void loadTable() {
    // TODO preserve selection on reload

    this.table.removeAll();

    List<JenkinsInstance> insts = CloudBeesUIPlugin.getDefault().loadManualJenkinsInstances();
    Collections.sort(insts, new Comparator<JenkinsInstance>() {
      public int compare(final JenkinsInstance o1, final JenkinsInstance o2) {
        return o1.label.compareTo(o2.label);
      }
    });

    for (JenkinsInstance instance : insts) {
      TableItem tableItem = new TableItem(this.table, SWT.NONE);
      tableItem.setText(new String[] { instance.label, instance.url });
      tableItem.setData(instance);
    }

    if (this.table.getItemCount() > 0) {
      this.table.setSelection(0);
    }

    enableButtons();
  }

  private void enableButtons() {
    TableItem[] items = this.table.getSelection();
    JenkinsInstancesPreferencePage.this.btnEdit.setEnabled(items != null && items.length > 0);
    JenkinsInstancesPreferencePage.this.btnRemove.setEnabled(items != null && items.length > 0);
  }
}
