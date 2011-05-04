package com.cloudbees.eclipse.dev.scm.subversive;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.local.management.ShareProjectOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;

public class ForgeSubversiveSync implements ForgeSync {

  @Override
  public void sync(final ForgeInstance instance, final String passwd, final IProgressMonitor monitor)
      throws CloudBeesException {

    if (!ForgeInstance.TYPE.SVN.equals(instance.type)) {
      return;
    }

    String url = instance.url;

    if (url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    try {
      monitor.beginTask("Validating SVN repository connection...", 10);
      monitor.worked(1);

      IRepositoryLocation loc = SVNRemoteStorage.instance().newRepositoryLocation();
      loc.setUrl(url);
      loc.setUsername(instance.user);
      loc.setPassword(passwd);
      monitor.worked(1);

      Exception ex = SVNUtility.validateRepositoryLocation(loc);
      if (ex != null) {
        monitor.worked(8);
        throw new CloudBeesException("Failed to validate SVN connection to " + url, ex);
      }
      monitor.worked(1);

      monitor.setTaskName("Adding repository...");
      SVNRemoteStorage.instance().addRepositoryLocation(loc);
      monitor.worked(7);

      instance.status = ForgeInstance.STATUS.SYNCED;

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
      IRepositoryLocation loc;
      loc = SVNRemoteStorage.instance().getRepositoryLocation(repo);
      if (loc == null) {
        IRepositoryLocation[] locs = SVNRemoteStorage.instance().getRepositoryLocations();
        if (locs != null) {
          for (IRepositoryLocation cur : locs) {
            if (repo.equals(cur.getUrl())) {
              loc = cur;
              break;
            }
          }
        }
      }
      if (loc == null) {
        loc = SVNRemoteStorage.instance().newRepositoryLocation();
        loc.setUrl(repo);
        Exception ex = SVNUtility.validateRepositoryLocation(loc);
        if (ex != null) {
          SVNRemoteStorage.instance().addRepositoryLocation(loc);
        }
      }
      String rootUrl = loc.getRepositoryRootUrl();
      System.out.println("Root: " + rootUrl);
      String filePath = rootUrl + item.path;
      System.out.println("Filepath: " + filePath);
      IRepositoryFile file = loc.asRepositoryFile(filePath, false);
      String dateS = item.parent.date; //2011-03-31T15:08:58.859428Z
      System.out.println("commit date: " + dateS);
      dateS = dateS.replace("T", " ");
      if (dateS.endsWith("Z")) {
        dateS = dateS.substring(0, dateS.length() - 4);
      }
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      System.out.println("Date1: " + format.parse(dateS));
      format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
      Date ts = format.parse(dateS);
      System.out.println("Date2: " + ts);
      long date = ts.getTime();
      System.out.println("date: " + date);
      switch (item.type) {
      case ADDED:
        ++date;
        break;
      case DELETED:
        --date;
        break;
      }
      file.setSelectedRevision(SVNRevision.fromDate(date));
      file.setPegRevision(SVNRevision.fromDate(date));
      Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+0"));
      cal.setTimeInMillis(date);
      System.out.println("Cal: " + cal.getTime());

      new OpenRemoteFileOperation(new IRepositoryFile[] { file }, OpenRemoteFileOperation.OPEN_DEFAULT).run(monitor);

      return true;
    } catch (Exception e) {
      e.printStackTrace(); // TODO: handle exception
      return false;
    }
  }

  @Override
  public void addToRepository(final ForgeInstance instance, final IProject project, final IProgressMonitor monitor)
      throws CloudBeesException {

    if (!ForgeInstance.TYPE.SVN.equals(instance.type)) {
      return;
    }

    boolean endsWithSlash = instance.url.endsWith("/");
    IRepositoryLocation location = null;

    for (IRepositoryLocation loc : SVNRemoteStorage.instance().getRepositoryLocations()) {
      String url = loc.getUrl();

      if (endsWithSlash && !url.endsWith("/")) {
        url += "/";
      }

      if (url.equalsIgnoreCase(instance.url)) {
        location = loc;
        break;
      }
    }

    if (location == null) {
      throw new CloudBeesException("Could not find CloudBees SVN repository");
    }

    ShareProjectOperation.IFolderNameMapper mapper = new ShareProjectOperation.IFolderNameMapper() {

      @Override
      public String getRepositoryFolderName(final IProject project) {
        return project.getName();
      }
    };

    ShareProjectOperation operation = new ShareProjectOperation(new IProject[] { project }, location, mapper, null, 1,
        false, "Create new repository folder");

    operation.run(monitor);
  }

  @Override
  public boolean isUnderSvnScm(final IProject project) {
    return FileUtility.alreadyOnSVN(project);
  }

  @Override
  public ForgeInstance getMainRepo(final IProject project) {
    SVNChangeStatus status = SVNUtility.getSVNInfoForNotConnected(project);

    if (status != null) {
      return new ForgeInstance(status.url, null, ForgeInstance.TYPE.SVN);
    }

    return null;
  }

}
