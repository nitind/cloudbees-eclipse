package com.cloudbees.eclipse.run.ui.launchconfiguration;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.util.CBResourceUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

public class CBLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private static final String TAB_NAME = "CloudBees Application";
  private static final String GROUP_TITLE = "Project:";
  private static final String BROWSE_PROJECT_LABEL = "Browse...";
  private static final String SELECTION_DIALOG_MESSAGE = "Select a CloudBees project to constrain your search.";
  private static final String SELECTION_DIALOG_TITLE = "Project Selection";

  private Text projectName;
  private Button projectBtn;

  public void createControl(Composite parent) {

    Composite content = new Composite(parent, SWT.NONE);
    content.setLayout(new GridLayout());
    setControl(content);

    Group group = new Group(content, SWT.NONE);
    group.setText(GROUP_TITLE);
    group.setLayout(new GridLayout(2, false));
    GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    group.setLayoutData(data);

    projectName = new Text(group, SWT.SINGLE | SWT.BORDER);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    projectName.setLayoutData(data);
    projectName.setFont(parent.getFont());
    projectName.addModifyListener(new ModifyListener() {

      public void modifyText(ModifyEvent e) {
        updateErrorMessage();
        updateLaunchConfigurationDialog();
      }

    });

    projectBtn = createPushButton(group, BROWSE_PROJECT_LABEL, null);
    projectBtn.addSelectionListener(new SelectionAdapter() {

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

  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
  }

  public void initializeFrom(ILaunchConfiguration configuration) {
    try {
      projectName.setText(configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, new String()));
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
  }

  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName.getText());
  }

  public String getName() {
    return TAB_NAME;
  }

  @Override
  public Image getImage() {
    return CBRunUiActivator.getDefault().getImageRegistry().get(Images.CLOUDBEES_ICON_16x16);
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
        projectName.setText(project.getName());
      }
    }
    
    updateErrorMessage();
  }
  
  private IStatus validate() {
    String name = projectName.getText();
    if(name == null || name.length() == 0) {
      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Project name is empty.");
    }
    
    boolean foundProjectWithSameName = false;
    for(IProject project : CBResourceUtil.getWorkbenchCloudBeesProjects()) {
      if(project.getName().equals(name)) {
        foundProjectWithSameName = true;
        break;
      }
    }
    
    if(!foundProjectWithSameName) {
      String error = MessageFormat.format("Can''t find CloudBees application with name ''{0}'' from workspace.", name);
      return new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, error);
    }
    
    return new Status(IStatus.OK, CBRunUiActivator.PLUGIN_ID, null);
  }
  
  private void updateErrorMessage() {
    IStatus status = validate();
    if(status.getSeverity() == IStatus.OK) {
      setErrorMessage(null);
      setMessage("Run CloudBees application");
    } else {
      setErrorMessage(status.getMessage());
    }
  }
  
  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    return validate().getSeverity() == IStatus.OK;
  }
  
}
