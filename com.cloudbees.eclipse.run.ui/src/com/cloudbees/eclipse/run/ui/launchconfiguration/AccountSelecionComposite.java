package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.WorkbenchJob;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

@SuppressWarnings("restriction")
public abstract class AccountSelecionComposite extends Composite {

  private static final String GROUP_TITLE = "Account";
  private static final String BUTTON_LABEL = "Choose...";
  private static final String HINT = "Choose account name to use...";
  private static final String ERROR_TITLE = "Error";

  private Text accountNameText;
  private Button chooseAccountButton;
  private GrandCentralService gcService;
  private List<String> accountNames;

  public AccountSelecionComposite(Composite parent) {
    super(parent, SWT.NONE);
    prepare(parent);
    createComponents(parent);
  }

  private void prepare(Composite parent) {
    try {
      this.gcService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
      this.accountNames = new ArrayList<String>();

      WorkbenchJob job = new WorkbenchJob("Loading account names") {
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
          try {
            String[] accounts = AccountSelecionComposite.this.gcService.getAccounts(monitor);
            for (String accountName : accounts) {
              AccountSelecionComposite.this.accountNames.add(accountName);
            }
            return Status.OK_STATUS;
          } catch (CloudBeesException e) {
            return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Failed to load accounts", e);
          }
        }
      };

      IStatus status = job.runInUIThread(new NullProgressMonitor());
      if (!status.isOK()) {
        handleException("Exception while fetching account names", status);
      }

    } catch (CloudBeesException e) {
      handleException("Exception while preparing account data", e);
    }
  }

  private void createComponents(Composite parent) {
    setLayout(new FillLayout());

    Composite content = new Composite(this, SWT.NONE);
    content.setLayout(new GridLayout());

    Group group = new Group(content, SWT.NONE);
    group.setText(GROUP_TITLE);
    group.setLayout(new GridLayout(2, false));
    GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    group.setLayoutData(data);

    this.accountNameText = new Text(group, SWT.SINGLE | SWT.BORDER);
    this.accountNameText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        handleUpdate();
      }
    });

    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    this.accountNameText.setLayoutData(data);
    this.accountNameText.setFont(parent.getFont());
    this.accountNameText.setMessage(HINT);
    this.accountNameText.setText(getDefaultAccountName());

    this.chooseAccountButton = SWTFactory.createPushButton(group, BUTTON_LABEL, null);
    this.chooseAccountButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        openAccountSelectionDialog();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        openAccountSelectionDialog();
      }

    });
  }

  public abstract void handleUpdate();

  public IStatus validate() {
    String currentText = this.accountNameText.getText();

    if (!this.accountNames.contains(currentText)) {
      String error = MessageFormat.format("Can''t find CloudBees account with name ''{0}''.", currentText);
      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, error);
    }

    return Status.OK_STATUS;
  }

  public String getAccountName() {
    return this.accountNameText.getText();
  }

  private String getDefaultAccountName() {
    if (!this.accountNames.isEmpty()) {
      return this.accountNames.get(0);
    }
    return null;
  }

  private void handleException(String msg, Throwable t) {
    Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, msg, t);
    handleException(msg, status);
  }

  private void handleException(String msg, IStatus status) {
    CBRunUiActivator.logError(status.getException());
    ErrorDialog.openError(getShell(), ERROR_TITLE, msg, status);
  }

  private void openAccountSelectionDialog() {
    String[] accountNamesArray = new String[this.accountNames.size()];
    this.accountNames.toArray(accountNamesArray);
    AccountSelectionDialog dialog = new AccountSelectionDialog(getShell(), accountNamesArray);
    dialog.open();
    if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
      this.accountNameText.setText(dialog.getSelectedAccountName());
    }
  }
}
