/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.run.ui.launchconfiguration;

import static com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants.DO_NOTHING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.launching.SocketAttachConnector;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBProjectProcessService;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class CBLocalLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate/* LaunchConfigurationDelegate *//*AntLaunchDelegate */{

  public void launch(ILaunchConfiguration conf, String mode, ILaunch launch, IProgressMonitor monitor)
      throws CoreException {
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }

    boolean debug = mode.equals("debug");

    String projectName = conf.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, "");
    String warName = conf.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_WAR_PATH, "");

    IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    IFile file = null;
    if (proj != null && warName != null && warName.length() > 0) {
      file = proj.getFile(warName);
    }

    conf = modifyLaunch(proj, conf, mode);

    if (conf.getAttribute(DO_NOTHING, false)) {
      monitor.setCanceled(true);
      return;
    }

    String port = conf.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PORT, CBRunUtil.getDefaultLocalPort() + "");
    String debugPort = conf.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_DEBUG_PORT,
        CBRunUtil.getDefaultLocalDebugPort() + "");

    final IFile ff = file;

    Process p = internalLaunch(monitor, file, proj, debug, port, debugPort);
    if (p == null) {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        public void run() {
          MessageDialog.openWarning(CloudBeesUIPlugin.getActiveWindow().getShell(), "Deploy stopped!",
              "Process creation stopped for '" + ff.getName() + "'");
        }
      });
      return;
      //throw new CoreException(new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, "Failed to create local process!"));
    }

    Map<String, String> attrs = new HashMap<String, String>();

    if (debug) {
      //addDebugAttrs(attrs, projectName, debugPort);
    }

    String taskName = warName;
    if (taskName == null || taskName.length() == 0) {
      taskName = projectName;
    }
    IProcess runtimeProcess = DebugPlugin.newProcess(launch, p, taskName, attrs); // new RuntimeProcess(launch, p, warName, null);

    launch.addProcess(runtimeProcess);

    if (debug) {
      IVMConnector connector = new SocketAttachConnector();//.getDefaultVMConnector();

      Map args = connector.getDefaultArguments();

      Map argMap = conf.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, (Map) null);

      if (argMap == null) {
        argMap = new HashMap();
      }
      int connectTimeout = JavaRuntime.getPreferences().getInt(JavaRuntime.PREF_CONNECT_TIMEOUT);

      argMap.put("timeout", Integer.toString(connectTimeout)); //$NON-NLS-1$

      //String connectMapAttr = IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP;
      //Map<String, String> connectMap = new HashMap<String, String>();
      argMap.put("hostname", "localhost");
      argMap.put("port", debugPort);
      //argMap.put(connectMapAttr, connectMap);

      setDefaultSourceLocator(launch, conf);

      connector.connect(argMap, monitor, launch);
    }

    CBProjectProcessService.getInstance().addProcess(projectName, launch.getProcesses()[0]);
    //DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new TerminateListener(projectName));

    //if (debug) {
    //CBRunUtil.createTemporaryRemoteLaunchConfiguration(projectName).launch(mode, monitor);
    //}

    // handleExtensions(configuration, projectName);
  }

  /*  private void addDebugAttrs(Map<String, String> attrs, String projectName, String debugPort) {
      List<String> resourcePaths = new ArrayList<String>();
      resourcePaths.add("/" + projectName);
      attrs.put("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS", resourcePaths);

      List<String> resourceTypes = new ArrayList<String>();
      resourceTypes.add("4");
      attrs.put("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES", resourceTypes);

      String connectMapAttr = IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP;
      Map<String, String> connectMap = new HashMap<String, String>();
      connectMap.put("hostname", "localhost");
      connectMap.put("port", debugPort);
      attrs.put(connectMapAttr, connectMap);

      attrs.put(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, true);
      attrs.put(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
      attrs.put(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
          IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
    }
  */
  private ILaunchConfiguration modifyLaunch(IProject proj, ILaunchConfiguration configuration, String mode)
      throws CoreException {
    ILaunchConfigurationWorkingCopy copy = configuration.copy(configuration.getName());

    copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, true);

    List<String> resourcePaths = new ArrayList<String>();
    resourcePaths.add("/" + proj.getName());
    copy.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS", resourcePaths);

    List<String> resourceTypes = new ArrayList<String>();
    resourceTypes.add("4");
    copy.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES", resourceTypes);

    copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, proj.getName());

    copy.setMappedResources(new IResource[] { proj });

    if (mode.equals("run")) {
      copy.removeAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR);
    } else if (mode.equals("debug")) {
      copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
          IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);

      //String vmargs = "-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8002";
      //copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmargs);
    }
    return copy;
  }

  private class TerminateListener implements ILaunchesListener2 {

    private final String projectName;

    public TerminateListener(String projectName) {
      this.projectName = projectName;
    }

    @Override
    public void launchesRemoved(ILaunch[] launches) {
    }

    @Override
    public void launchesAdded(ILaunch[] launches) {
    }

    @Override
    public void launchesChanged(ILaunch[] launches) {
    }

    @Override
    public void launchesTerminated(ILaunch[] launches) {
      //CBProjectProcessService service = CBProjectProcessService.getInstance();
      //service.removeProcess(this.projectName);
    }
  }

  /*  private IExtension[] handleExtensions(ILaunchConfiguration configuration, String projectName) {
      IExtension[] extensions = Platform.getExtensionRegistry()
          .getExtensionPoint(CBRunUiActivator.PLUGIN_ID, "launchDelegateAditions").getExtensions();

      for (IExtension extension : extensions) {
        for (IConfigurationElement element : extension.getConfigurationElements()) {
          try {
            Object executableExtension = element.createExecutableExtension("actions");
            if (executableExtension instanceof ILaunchExtraAction) {
              ((ILaunchExtraAction) executableExtension).action(configuration, projectName, true);
            }
          } catch (CoreException e) {
            CBRunUiActivator.logError(e);
          }
        }
      }
      return extensions;
    }
  */

  public static Process internalLaunch(IProgressMonitor monitor, final IFile file, IProject project, boolean debug,
      String port, String debugPort) {

    try {

      // Strategy for deciding if build is needed: invoke project build always when selection is project
      if (project != null) {
        return wrappedDeployLocal(project, file, debug, port, debugPort, monitor);
      } else if (file != null) {// deploy specified file, without build. If unknown type, confirm first.

        if (!BeesSDK.hasSupportedExtension(file.getName())) {
          final String ext = BeesSDK.getExtension(file.getName());

          final Boolean[] openConfirm = new Boolean[] { Boolean.FALSE };

          PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            public void run() {
              openConfirm[0] = MessageDialog.openConfirm(CloudBeesUIPlugin.getActiveWindow().getShell(),
                  "Deploy to local",
                  ext + " is an unknown app package type.\nAre you sure you want to deploy '" + file.getName()
                      + "' to local?");
            }

          });

          if (!openConfirm[0]) {
            return null;
          }

        }

        return wrappedDeployLocal(file.getProject(), file, debug, port, debugPort, monitor);

      }
    } catch (Exception e) {
      e.printStackTrace();
      final Exception e2 = e;
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        public void run() {
          MessageDialog.openWarning(CloudBeesUIPlugin.getActiveWindow().getShell(), "Deploy failed!",
              "Deployment failed for '" + file.getName() + "': " + e2.getMessage());
        }
      });
    }

    return null;
  }

  private static Process wrappedDeployLocal(final IProject project, final IFile file, final boolean debug, String port,
      String debugPort, final IProgressMonitor monitor) throws CloudBeesException {
    String artifactId = file == null ? project.getName() : file.getRawLocation().toOSString();

    if (file == null) { // project run --> include build 
      return BeesSDK.deployProjectLocal(project, true, debug, port, debugPort, monitor);
    } else {
      return BeesSDK.deployFileLocal(project, file, debug, port, debugPort, monitor);
    }

  }

  @Override
  public boolean isAllowTerminate(ILaunchConfiguration configuration) throws CoreException {
    return super.isAllowTerminate(configuration);
  }

}
