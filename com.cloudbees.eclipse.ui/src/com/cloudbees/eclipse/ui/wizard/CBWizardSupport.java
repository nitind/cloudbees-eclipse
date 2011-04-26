package com.cloudbees.eclipse.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class CBWizardSupport {

  public static JenkinsInstance[] getJenkinsInstances(IWizardContainer container) throws Exception {
    JenkinsInstance[] result = null;

    final List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
    final Failiure<CloudBeesException> failiure = new Failiure<CloudBeesException>();

    IRunnableWithProgress operation = new IRunnableWithProgress() {

      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          CloudBeesUIPlugin plugin = CloudBeesUIPlugin.getDefault();
          List<JenkinsInstance> manualInstances = plugin.loadManualJenkinsInstances();
          List<JenkinsInstance> cloudInstances = plugin.loadDevAtCloudInstances(monitor);
          instances.addAll(manualInstances);
          instances.addAll(cloudInstances);
        } catch (CloudBeesException e) {
          failiure.cause = e;
        }
      }
    };

    try {
      container.run(true, false, operation);
      result = new JenkinsInstance[instances.size()];
      instances.toArray(result);
    } catch (Exception e) {
      if (failiure.cause != null) {
        throw failiure.cause;
      }
      throw e;
    }

    return result;
  }

  public static Repo[] getRepos(IWizardContainer container) throws Exception {
    return getRepos(container, null);
  }

  public static Repo[] getRepos(IWizardContainer container, final ForgeSync.TYPE type) throws Exception {
    Repo[] result = null;

    final List<Repo> repos = new ArrayList<AccountServiceStatusResponse.AccountServices.ForgeService.Repo>();
    final Failiure<CloudBeesException> failiure = new Failiure<CloudBeesException>();

    IRunnableWithProgress operation = new IRunnableWithProgress() {

      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          GrandCentralService service = CloudBeesCorePlugin.getDefault().getGrandCentralService();
          if (type == null) {
            repos.addAll(service.getForgeRepos(monitor));
          } else {
            for (Repo r : service.getForgeRepos(monitor)) {
              if (ForgeSync.TYPE.valueOf(r.type.toUpperCase()) == type) {
                repos.add(r);
              }
            }
          }
        } catch (CloudBeesException e) {
          failiure.cause = e;
        }
      }
    };

    try {
      container.run(true, false, operation);
      result = new Repo[repos.size()];
      repos.toArray(result);
    } catch (Exception e) {
      if (failiure.cause != null) {
        throw failiure.cause;
      }
      throw e;
    }

    return result;
  }

  public static IStructuredSelection getCurrentSelection() {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      ISelection selection = window.getSelectionService().getSelection();
      if (selection instanceof IStructuredSelection) {
        return (IStructuredSelection) selection;
      }
    }
    return StructuredSelection.EMPTY;
  }

  public static void makeJenkinsJob(final String configXML, final JenkinsService jenkinsService, final String jobName,
      IWizardContainer container) throws Exception {

    final Failiure<CloudBeesException> failiure = new Failiure<CloudBeesException>();

    IRunnableWithProgress operation = new IRunnableWithProgress() {

      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          jenkinsService.createJenkinsJob(jobName, configXML, monitor);
        } catch (CloudBeesException e) {
          failiure.cause = e;
        }
      }

    };

    try {
      container.run(true, false, operation);
    } catch (Exception e) {
      if (failiure.cause != null) {
        throw failiure.cause;
      }
      throw e;
    }

  }

}
