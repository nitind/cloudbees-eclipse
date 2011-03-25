package com.cloudbees.eclipse.run.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.cloudbees.eclipse.run.core.CBRunCoreScrips;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

public class CBSampleWebAppWizard extends Wizard implements INewWizard {

  private static final String WINDOW_TITLE = "Sample Web Application";

  private CBSampleWebAppWizardPage page;

  public CBSampleWebAppWizard() {
    super();
    setNeedsProgressMonitor(true);
    setWindowTitle(WINDOW_TITLE);
    setDefaultPageImageDescriptor(CBRunUiActivator.imageDescriptorFromPlugin(CBRunUiActivator.PLUGIN_ID,
        Images.CLOUDBEES_WIZ_ICON));
  }

  public void addPages() {
    page = new CBSampleWebAppWizardPage();
    addPage(page);
  }

  public void init(IWorkbench workbench, IStructuredSelection selection) {
  }

  @Override
  public boolean performFinish() {
    
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

    String projectName = page.getProjectName();
    String worksapceLocation = workspaceRoot.getLocation().toPortableString();
    
    CBRunCoreScrips.executeCopySampleWebAppScript(worksapceLocation, projectName);
    
    IPath containerPath = workspaceRoot.getProject(projectName).getFullPath();
    File source = workspaceRoot.getLocation().append(projectName).toFile();
    IImportStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;
    IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
      
      public String queryOverwrite(String pathString) {
        return IOverwriteQuery.ALL;
      }

    };
    
    final ImportOperation importOp = new ImportOperation(containerPath, source, structureProvider, overwriteQuery);
    
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          importOp.setContext(getShell());
          importOp.setCreateContainerStructure(false);
          importOp.run(monitor);
        } catch (InterruptedException e) {
          // TODO
          e.printStackTrace();
        } finally {
          monitor.done();
        }
      }
    };

    try {
      getContainer().run(true, false, op);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }

    return true;
  }

}