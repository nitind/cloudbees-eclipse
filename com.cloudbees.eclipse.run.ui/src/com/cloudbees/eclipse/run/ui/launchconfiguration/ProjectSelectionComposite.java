package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.cloudbees.eclipse.run.core.util.CBResourceUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

public abstract class ProjectSelectionComposite extends Composite {

  private static final String GROUP_TITLE = "Project:";
  private static final String BROWSE_PROJECT_LABEL = "Browse...";
  private static final String SELECTION_DIALOG_MESSAGE = "Select a CloudBees project to constrain your search.";
  private static final String SELECTION_DIALOG_TITLE = "Project Selection";

  private final Text projectName;
  private final Button projectBtn;

  /**
   * Create the composite.
   * 
   * @param parent
   * @param style
   */
  public ProjectSelectionComposite(Composite parent, int style) {
    super(parent, style);
    Composite composite = new Composite(this, style);
    composite.setLayout(new GridLayout());

    Group group = new Group(composite, SWT.NONE);
    group.setText(GROUP_TITLE);
    group.setLayout(new GridLayout(2, false));
    GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    group.setLayoutData(data);

    this.projectName = new Text(group, SWT.SINGLE | SWT.BORDER);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    this.projectName.setLayoutData(data);
    this.projectName.setFont(parent.getFont());

    this.projectBtn = SWTFactory.createPushButton(group, BROWSE_PROJECT_LABEL, null);
    this.projectBtn.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleProjectButtonPressed();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        handleProjectButtonPressed();
      }

    });
  }

  @Override
  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }

  private void handleProjectButtonPressed() {
    JavaElementLabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);

    ElementListSelectionDialog selectionDialog = new ElementListSelectionDialog(getShell(), labelProvider);
    selectionDialog.setTitle(SELECTION_DIALOG_TITLE);
    selectionDialog.setMessage(SELECTION_DIALOG_MESSAGE);

    List<IProject> projects = CBResourceUtil.getWorkbenchCloudBeesProjects();

    IProject[] projectsArray = new IProject[projects.size()];
    projects.toArray(projectsArray);
    selectionDialog.setElements(projectsArray);
    if (projects.size() > 0) {
      selectionDialog.setInitialSelections(new Object[] { projects.get(0) });
    }

    if (selectionDialog.open() == Window.OK) {
      IProject project = (IProject) selectionDialog.getFirstResult();
      if (project != null) {
        this.projectName.setText(project.getName());
      }
    }

    updateErrorMessage();
  }

  IStatus validate() {
    String name = this.projectName.getText();
    if (name == null || name.length() == 0) {
      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Project name is empty.");
    }

    boolean foundProjectWithSameName = false;
    for (IProject project : CBResourceUtil.getWorkbenchCloudBeesProjects()) {
      if (project.getName().equals(name)) {
        foundProjectWithSameName = true;
        break;
      }
    }

    if (!foundProjectWithSameName) {
      String error = MessageFormat.format("Can''t find CloudBees application with name ''{0}'' from workspace.", name);
      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, error);
    }

    return new Status(IStatus.OK, CBRunUiActivator.PLUGIN_ID, null);
  }

  public abstract void updateErrorMessage();

  public void addModifyListener(ModifyListener modifyListener) {
    this.projectName.addModifyListener(modifyListener);
  }

  public void setText(String attribute) {
    this.projectName.setText(attribute);
  }

  public String getText() {
    return this.projectName.getText();
  }
}
