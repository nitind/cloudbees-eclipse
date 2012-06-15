package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.WorkbenchJob;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;
import com.cloudbees.eclipse.ui.UIUtils;

public class AccountSelectionDialog extends TitleAreaDialog {

  private static final String TITLE = "Account Selection";
  private static final String DESCRIPTION = "Please select the account you want to use";
  private static final String ERROR_TITLE = "Error";
  private static final Image ICON = CBRunUiActivator.getImage(Images.CLOUDBEES_WIZ_ICON);

  private final String[] accountNames;
  private String selectedAccountName;

  public AccountSelectionDialog(Shell shell) {
    super(shell);
    this.accountNames = loadAccountNames();
  }

  public AccountSelectionDialog(Shell shell, String[] accountNames) {
    super(shell);
    this.accountNames = accountNames;
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    setTitle(TITLE);
    setMessage(DESCRIPTION);
    setTitleImage(ICON);
    getShell().setSize(400, 400);
    getShell().setMinimumSize(400, 400);
    getShell().setText(TITLE);

    Point shellCenter = UIUtils.getCenterPoint();
    getShell().setLocation(shellCenter.x - 400 / 2, shellCenter.y - 400 / 2);
    
    
    if (this.accountNames == null || this.accountNames.length == 0) {
      getButton(OK).setEnabled(false);
    }

    return contents;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);

    Composite content = new Composite(area, SWT.NONE);
    content.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 10;
    gridLayout.marginHeight = 10;
    content.setLayout(gridLayout);

    Label label = new Label(content, SWT.NONE);
    label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
    label.setText("Select account:");

    final List list = new org.eclipse.swt.widgets.List(content, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
    list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    list.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSelectionChange(list);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        handleSelectionChange(list);
      }
    });
    list.setItems(this.accountNames);

    return area;
  }

  private String[] loadAccountNames() {
    final java.util.List<String> accountNamesList = new ArrayList<String>();

    try {
      final GrandCentralService gcService = CloudBeesCorePlugin.getDefault().getGrandCentralService();

      WorkbenchJob job = new WorkbenchJob("Loading account names") {
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
          try {
            String[] accounts = gcService.getAccounts(monitor);
            for (String accountName : accounts) {
              accountNamesList.add(accountName);
            }
            return Status.OK_STATUS;
          } catch (CloudBeesException e) {
            return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Failed to load accounts", e);
          }
        }
      };

      IStatus status = job.runInUIThread(new NullProgressMonitor());
      if (!status.isOK()) {
        throw status.getException();
      }

    } catch (Throwable t) {
      handleException("Exception while loading accounts", t);
    }

    String[] accountNames = new String[accountNamesList.size()];
    return accountNamesList.toArray(accountNames);
  }

  private void handleException(String msg, Throwable t) {
    Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, msg, t);
    handleException(msg, status);
  }

  private void handleException(String msg, IStatus status) {
    CBRunUiActivator.logError(status.getException());
    ErrorDialog.openError(getShell(), ERROR_TITLE, msg, status);
  }

  @Override
  protected boolean isResizable() {
    return true;
  }

  @Override
  public boolean isHelpAvailable() {
    return false;
  }

  private void handleSelectionChange(List list) {
    if (list.getSelectionCount() == 0) {
      this.selectedAccountName = null;
    }
    this.selectedAccountName = list.getSelection()[0];
  }

  public String getSelectedAccountName() {
    return this.selectedAccountName;
  }

}
