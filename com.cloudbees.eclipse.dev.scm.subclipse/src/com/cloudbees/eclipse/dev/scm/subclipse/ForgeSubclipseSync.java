package com.cloudbees.eclipse.dev.scm.subclipse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.repo.SVNRepositories;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.RemoteFile;
import org.tigris.subversion.subclipse.core.resources.RemoteResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.editor.RemoteFileEditorInput;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance.STATUS;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

/**
 * Forge repo sync provider for Subclipse
 *
 * @author ahtik
 */
public class ForgeSubclipseSync implements ForgeSync {

  @Override
  public void updateStatus(final ForgeInstance instance, final IProgressMonitor monitor) throws CloudBeesException {

    if (!ForgeInstance.TYPE.SVN.equals(instance.type)) {
      return;
    }

    if (instance.url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    try {
      monitor.beginTask("Validating SVN repository connection '" + instance.url + "'...", 10);
      monitor.worked(1);

      SVNRepositories repos = SVNProviderPlugin.getPlugin().getRepositories();
      boolean exists = repos.isKnownRepository(instance.url, false);
      if (exists) {
        monitor.worked(9);
        instance.status = ForgeInstance.STATUS.SYNCED;
      } else {
        if (instance.status != STATUS.SKIPPED) { // user might have deleted it and need to sync again
          instance.status = ForgeInstance.STATUS.UNKNOWN;
        }

        //System.out.println("Repo is unknown for Subclipse: " + instance.url);
      }
    } finally {
      monitor.worked(10);
      monitor.done();
    }
  }

  @Override
  public void sync(final ForgeInstance instance, final IProgressMonitor monitor) throws CloudBeesException {

    if (!ForgeInstance.TYPE.SVN.equals(instance.type)) {
      return;
    }

    if (instance.url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    try {
      monitor.beginTask("Validating SVN repository connection '" + instance.url + "'...", 10);
      monitor.worked(1);

      SVNRepositories repos = SVNProviderPlugin.getPlugin().getRepositories();
      boolean exists = repos.isKnownRepository(instance.url, false);
      if (exists) {
        instance.status = ForgeInstance.STATUS.SYNCED;
        return;
      }

      Properties props = new Properties();
      props.setProperty("url", instance.url);
      props.setProperty("user", instance.user);
      props.setProperty("password", instance.password);
      ISVNRepositoryLocation rep = SVNProviderPlugin.getPlugin().getRepositories().createRepository(props);
      monitor.worked(5);

      //rep.validateConnection(monitor);
      SVNProviderPlugin.getPlugin().getRepositories().addOrUpdateRepository(rep);
      monitor.worked(4);

      instance.status = ForgeInstance.STATUS.SYNCED;

    } catch (SVNException e) {
      throw new CloudBeesException("Failed to create missing repository!", e);
    } finally {
      monitor.worked(10);
      monitor.done();
    }
  }

  @Override
  public boolean openRemoteFile(final JenkinsScmConfig scmConfig, final ChangeSetPathItem item,
      final IProgressMonitor monitor) {
    for (JenkinsScmConfig.Repository repo : scmConfig.repos) {
      if (!ForgeInstance.TYPE.SVN.equals(repo.type)) {
        continue;
      }

      boolean opened = openRemoteFile_(repo.url, item, monitor);
      if (opened) {
        return true;
      }
    }

    return false;
  }

  private boolean openRemoteFile_(final String repo, final ChangeSetPathItem item, final IProgressMonitor monitor) {
    try {
      SVNRepositories repos = SVNProviderPlugin.getPlugin().getRepositories();
      boolean exists = repos.isKnownRepository(repo, false);
      //      if (exists) {
      //      }
      //      ISVNRepositoryLocation rep = SVNProviderPlugin.getPlugin().getRepositories().createRepository(props);
      ISVNRepositoryLocation repository2 = repos.getRepository(repo);
      //      SVNProviderPlugin.getPlugin().getRepositories().addOrUpdateRepository(rep);
      SVNUrl svnurl = repository2.getRepositoryRoot().appendPath(item.path);
      String dateS = item.parent.date; //2011-03-31T15:08:58.859428Z
      //System.out.println("commit date: " + dateS);
      dateS = dateS.replace("T", " ");
      if (dateS.endsWith("Z")) {
        dateS = dateS.substring(0, dateS.length() - 4);
      }
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      //System.out.println("Date1: " + format.parse(dateS));
      format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
      Date ts = format.parse(dateS);
      //System.out.println("Date2: " + ts);
      Calendar cal = Calendar.getInstance();
      cal.setTime(ts);
      switch (item.type) {
      case ADDED:
        cal.add(Calendar.SECOND, +1);
        break;
      case DELETED:
        cal.add(Calendar.SECOND, -1);
        break;
      }
      ts = cal.getTime();
      SVNRevision revision = new SVNRevision.DateSpec(ts);
      ISVNRemoteResource remoteResource = new RemoteFile(repository2, svnurl, revision);
      //      if (!(remoteResource instanceof ISVNRemoteFile)) {
      //        continue;
      //      }
      final ISVNRemoteFile file = (ISVNRemoteFile) remoteResource;
      if (file instanceof RemoteResource) {
        ((RemoteResource) file).setPegRevision(revision /*SVNRevision.HEAD*/);
      }
      final String filename = remoteResource.getName();

      final IEditorPart[] editor = new IEditorPart[1];

      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          try {
            IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
            IWorkbenchPage page = CloudBeesUIPlugin.getActiveWindow().getActivePage();
            IEditorDescriptor descriptor = registry.getDefaultEditor(filename);
            String id;
            if (descriptor == null) {
              id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
            } else {
              id = descriptor.getId();
            }

            try {
              editor[0] = page.openEditor(new RemoteFileEditorInput(file, monitor), id);
            } catch (PartInitException e) {
              if (id.equals("org.eclipse.ui.DefaultTextEditor")) { //$NON-NLS-1$
                throw e;
              }
              editor[0] = page.openEditor(new RemoteFileEditorInput(file, monitor), "org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
            }
          } catch (Exception e) {
            e.printStackTrace(); // TODO
          }
        }
      });
      return editor[0] != null;
    } catch (Exception e) {
      e.printStackTrace(); // TODO: handle exception
      return false;
    }
  }

  @Override
  public void addToRepository(final ForgeInstance instance, final IProject project, final IProgressMonitor monitor)
      throws CloudBeesException {

    monitor.beginTask("Adding project to SVN repository", 0);

    if (!ForgeInstance.TYPE.SVN.equals(instance.type)) {
      return;
    }

    monitor.subTask("checking if project is already under SCM");
    if (isUnderSvnScm(project)) {
      throw new CloudBeesException("This project is already under source control management.");
    }

    try {
      monitor.subTask("loading repository location");
      ISVNRepositoryLocation location = SVNProviderPlugin.getPlugin().getRepository(instance.url);
      SVNWorkspaceRoot
          .shareProject(location, project, project.getName(), "Create new repository folder", true, monitor);
      monitor.subTask("finished sharing project");
    } catch (Exception e) {
      throw new CloudBeesException("Failed to share project", e);
    }
  }

  @Override
  public boolean isUnderSvnScm(final IProject project) {
    boolean isSVNFolder = false;

    try {
      LocalResourceStatus projectStatus = SVNWorkspaceRoot.peekResourceStatusFor(project);
      isSVNFolder = projectStatus != null && projectStatus.hasRemote();
    } catch (SVNException e) {
      e.printStackTrace();
    }

    return isSVNFolder;
  }

  @Override
  public ForgeInstance getMainRepo(final IProject project) {
    try {
      LocalResourceStatus projectStatus = SVNWorkspaceRoot.peekResourceStatusFor(project);
      ForgeInstance repo = new ForgeInstance(projectStatus.getUrlString(), null, null, ForgeInstance.TYPE.SVN);
      return repo;
    } catch (SVNException e) {
      e.printStackTrace(); // TODO log or report
      return null;
    }
  }

}
