package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;

@SuppressWarnings("restriction")
public abstract class ProjectSelectionComposite extends Composite {

  private static final String PROJECT_MSG = "Choose project...";
  private static final String GROUP_TITLE = "Project";
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
  public ProjectSelectionComposite(final Composite parent, final int style) {
    super(parent, style);
    setLayout(new FillLayout());
    Composite composite = new Composite(this, style);
    composite.setLayout(new GridLayout());

    Group group = new Group(composite, SWT.NONE);
    group.setText(GROUP_TITLE);
    group.setLayout(new GridLayout(2, false));
    GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    group.setLayoutData(data);

    this.projectName = new Text(group, SWT.SINGLE | SWT.BORDER);
    this.projectName.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(final ModifyEvent e) {
        handleUpdate();
      }
    });

    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    this.projectName.setLayoutData(data);
    this.projectName.setFont(parent.getFont());
    this.projectName.setMessage(PROJECT_MSG);

    this.projectBtn = SWTFactory.createPushButton(group, BROWSE_PROJECT_LABEL, null);
    this.projectBtn.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        handleProjectNameChange();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
        handleProjectNameChange();
      }

    });

  }

  @Override
  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }

  private void handleProjectNameChange() {
    JavaElementLabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);

    ElementListSelectionDialog selectionDialog = new ElementListSelectionDialog(getShell(), labelProvider);
    selectionDialog.setTitle(SELECTION_DIALOG_TITLE);
    selectionDialog.setMessage(SELECTION_DIALOG_MESSAGE);

    List<IProject> projects = CBRunUtil.getWorkbenchCloudBeesProjects();

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

    handleUpdate();
  }

  public IStatus validate() {
    String name = this.projectName.getText();
    if (name == null || name.length() == 0) {
      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Project name is empty.");
    }

    boolean foundProjectWithSameName = false;
    for (IProject project : CBRunUtil.getWorkbenchCloudBeesProjects()) {
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

  public abstract void handleUpdate();

  public void addModifyListener(final ModifyListener modifyListener) {
    this.projectName.addModifyListener(modifyListener);
  }

  public void setText(final String attribute) {
    this.projectName.setText(attribute);
  }

  public String getText() {
    return this.projectName.getText();
  }

  public String getDefaultSelection() {
    IJavaElement element = getSelectedJavaElement();
    if (element == null) {
      return new String();
    }

    IProject project = element.getJavaProject().getProject();

    if (CloudBeesNature.isEnabledFor(project) == false) {
      return new String();
    }

    return project.getName();
  }

  private IJavaElement getSelectedJavaElement() {
    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (activeWorkbenchWindow == null) {
      return null;
    }

    IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
    if (activePage == null) {
      return null;
    }

    ISelection selection = activePage.getSelection();
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;

      if (!structuredSelection.isEmpty()) {
        Object element = structuredSelection.getFirstElement();

        if (element instanceof IJavaElement) {
          return (IJavaElement) element;
        }

        if (element instanceof IResource) {
          IJavaElement javaElement = JavaCore.create((IResource) element);

          if (javaElement == null) {
            IProject project = ((IResource) element).getProject();
            javaElement = JavaCore.create(project);
          }

          if (javaElement != null) {
            return javaElement;
          }
        }
      }
    }

    IEditorPart part = activePage.getActiveEditor();
    if (part != null) {
      IEditorInput input = part.getEditorInput();
      return (IJavaElement) input.getAdapter(IJavaElement.class);
    }

    return null;
  }

}
