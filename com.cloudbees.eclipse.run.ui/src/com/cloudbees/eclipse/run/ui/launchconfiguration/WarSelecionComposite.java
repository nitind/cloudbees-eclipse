package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
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

import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

@SuppressWarnings("restriction")
public abstract class WarSelecionComposite extends Composite {

  private static final String GROUP_TITLE = "Custom War";
  private static final String BUTTON_LABEL = "Choose...";
  private static final String HINT = "Choose war file to deploy...";
  private static final String ERROR_TITLE = "Error";

  private Text warPathText;
  private Button chooseWarButton;
  private List<String> warPaths;
  private IProject project;

  public WarSelecionComposite(final Composite parent) {
    super(parent, SWT.NONE);
    prepare(parent);
    createComponents(parent);
  }

  public void setProject(final IProject project) {
    if (this.project != null && project != null && this.project.getFullPath().equals(project.getFullPath())) {
      return;
    }

    this.project = project;
    if (this.project != null) {
      this.warPaths = findWars();
    } else {
      this.warPaths = new ArrayList<String>();
    }
  }

  private void prepare(final Composite parent) {
    this.warPaths = findWars();
  }

  public List<String> findWars() {
    final List<String> wars = new ArrayList<String>();
    WorkbenchJob job = new WorkbenchJob("Searching for war files") {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {

        if (WarSelecionComposite.this.project == null) {
          return Status.OK_STATUS;
        }

        try {
          WarSelecionComposite.this.project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(final IResource resource) throws CoreException {
              if (resource.getType() == IResource.FILE && "war".equalsIgnoreCase(resource.getFileExtension())) {
                wars.add(resource.getProjectRelativePath().toOSString());
              }
              return true;
            }
          });
        } catch (CoreException e) {
          return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Failed to find wars", e);
        }

        return Status.OK_STATUS;
      }
    };

    IStatus status = job.runInUIThread(new NullProgressMonitor());
    if (!status.isOK()) {
      handleException("Exception while searching for wars", status);
    }
    return wars;
  }

  private void createComponents(final Composite parent) {
    setLayout(new FillLayout());

    Composite content = new Composite(this, SWT.NONE);
    content.setLayout(new GridLayout());

    Group group = new Group(content, SWT.NONE);
    group.setText(GROUP_TITLE);
    group.setLayout(new GridLayout(2, false));
    GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    group.setLayoutData(data);

    this.warPathText = new Text(group, SWT.SINGLE | SWT.BORDER);
    this.warPathText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(final ModifyEvent e) {
        handleUpdate();
      }
    });

    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    this.warPathText.setLayoutData(data);
    this.warPathText.setFont(parent.getFont());
    this.warPathText.setMessage(HINT);
    this.warPathText.setText(getDefaultWarPath());

    this.chooseWarButton = SWTFactory.createPushButton(group, BUTTON_LABEL, null);
    this.chooseWarButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        openWarSelectionDialog();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
        openWarSelectionDialog();
      }

    });
  }

  public abstract void handleUpdate();

  public IStatus validate() {
    String currentText = this.warPathText.getText();

    if (currentText == null || currentText.length() == 0) {
      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Please provide war name.");
    }

    //    if (!this.warNames.contains(currentText)) {
    //      String error = MessageFormat.format("Can''t find war file with name ''{0}''.", currentText);
    //      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, error);
    //    }

    return Status.OK_STATUS;
  }

  public String getWarPath() {
    return this.warPathText.getText();
  }

  private String getDefaultWarPath() {
    if (!this.warPaths.isEmpty()) {
      return this.warPaths.get(0);
    }
    return "";
  }

  private void handleException(final String msg, final Throwable t) {
    Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, msg, t);
    handleException(msg, status);
  }

  private void handleException(final String msg, final IStatus status) {
    if (status.getException() != null) {
      CBRunUiActivator.logError(status.getException());
    }
    ErrorDialog.openError(getShell(), ERROR_TITLE, msg, status);
  }

  private void openWarSelectionDialog() {
    WarSelectionDialog dialog = new WarSelectionDialog(getShell(), this.warPaths.toArray(new String[this.warPaths
        .size()]));
    dialog.open();
    if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
      this.warPathText.setText(dialog.getSelectedWarPath());
    }
  }

  public void setWarPath(final String warPath) {
    this.warPathText.setText(warPath);
  }
}
