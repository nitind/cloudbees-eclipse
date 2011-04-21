package com.cloudbees.eclipse.dev.scm.subclipse;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;

public class SVNSupport {

  public boolean isSVNFolder(IProject project) {
    boolean isSVNFolder = false;

    try {
      LocalResourceStatus projectStatus = SVNWorkspaceRoot.peekResourceStatusFor(project);
      isSVNFolder = projectStatus != null && projectStatus.hasRemote();
    } catch (SVNException e) {
      e.printStackTrace();
    }

    return isSVNFolder;
  }

  public ISVNRepositoryLocation getSVNRepositoryLocation(Repo repo) throws CloudBeesException {
    try {
      return SVNProviderPlugin.getPlugin().getRepository(repo.url);
    } catch (SVNException e) {
      throw new CloudBeesException("Failed to get SVN repository", e);
    }
  }

  public void share(ISVNRepositoryLocation location, IProject project, String comment, IProgressMonitor monitor)
      throws CloudBeesException {
    try {
      SVNWorkspaceRoot.shareProject(location, project, project.getName(), comment, true, monitor);
    } catch (TeamException e) {
      throw new CloudBeesException("Failed to share project", e);
    }
  }

  public IResource[] getResources(IProject project) throws CloudBeesException {
    try {
      return project.members(IContainer.INCLUDE_HIDDEN | IContainer.INCLUDE_PHANTOMS
          | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
    } catch (CoreException e) {
      throw new CloudBeesException("Failed to get project members", e);
    }
  }
}
