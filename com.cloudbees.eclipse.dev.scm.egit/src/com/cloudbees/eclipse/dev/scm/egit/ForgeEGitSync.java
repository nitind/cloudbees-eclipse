package com.cloudbees.eclipse.dev.scm.egit;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.ui.internal.CompareUtils;
import org.eclipse.egit.ui.internal.clone.GitCloneWizard;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

/**
 * Forge repo sync provider for EGIT
 * 
 * @author ahtik
 */
public class ForgeEGitSync implements ForgeSync {

  @Override
  public ACTION sync(final TYPE type, final Properties props, final IProgressMonitor monitor) throws CloudBeesException {

    if (!ForgeSync.TYPE.GIT.equals(type)) {
      return ACTION.SKIPPED;
    }

    final String url = props.getProperty("url");

    if (url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    final ACTION[] result = new ACTION[] { ACTION.SKIPPED };

    try {
      monitor.beginTask("Syncing EGit repository '" + url + "'", 10);

      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          monitor.subTask("Checking already cloned local repositories");
          monitor.worked(2);

          if (isAlreadyCloned(url)) {
            monitor.worked(8);
            result[0] = ACTION.CHECKED;
            return;
          }

          monitor.subTask("Cloning remote repository");
          monitor.worked(1);

          Clipboard clippy = new Clipboard(Display.getCurrent());
          clippy.setContents(new Object[] { url }, new Transfer[] { TextTransfer.getInstance() });
          GitCloneWizard cloneWizard = new GitCloneWizard();
          //      cloneWizard.setCallerRunsCloneOperation(true);
          WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
              cloneWizard);
          dlg.setHelpAvailable(true);
          int res = dlg.open();
          if (res == Window.OK) {
            //         cloneWizard.runCloneOperation(getContainer());
          }

          //          int timeout = Activator.getDefault().getPreferenceStore().getInt(
          //              UIPreferences.REMOTE_CONNECTION_TIMEOUT);
          //      CloneOperation clone = Utils.createInstance(CloneOperation.class, new Class[] { URIish.class, Boolean.TYPE,
          //          Collection.class, File.class, String.class, String.class, Integer.TYPE }, new Object[] { new URIish(url),
          //          true, Collections.EMPTY_LIST, new File(""), null, "origin", 5000 });
          //      if (clone == null) {
          //        // old constructor didn't have timeout at the end
          //        clone = Utils.createInstance(CloneOperation.class, new Class[] { URIish.class, Boolean.TYPE, Collection.class,
          //            File.class, String.class, String.class }, new Object[] { new URIish(url), true, Collections.EMPTY_LIST,
          //            new File(""), null, "origin" });
          //      }

          monitor.worked(2);

          //      clone.run(monitor);
          //
          //      if (clone == null) {
          //        throw new CloudBeesException("Failed to create EGit clone operation");
          //      }

          monitor.worked(5);

          //    } catch (URISyntaxException e) {
          //      throw new CloudBeesException(e);
          //    } catch (InvocationTargetException e) {
          //      throw new CloudBeesException(e);
          //    } catch (InterruptedException e) {
          //      throw new CloudBeesException(e);

          if (res == Window.OK) {
            result[0] = ACTION.CLONED;
          } else {
            result[0] = ACTION.CANCELLED;
          }
        }
      });

    } finally {
      monitor.worked(10);
      monitor.done();
    }

    return result[0];
  }

  protected boolean isAlreadyCloned(final String url) {
    try {
      URIish proposal = new URIish(url);

      List<String> reps = Activator.getDefault().getRepositoryUtil().getConfiguredRepositories();
      for (String repo : reps) {
        try {

          FileRepository fr = new FileRepository(new File(repo));
          List<RemoteConfig> allRemotes = RemoteConfig.getAllRemoteConfigs(fr.getConfig());
          for (RemoteConfig remo : allRemotes) {
            List<URIish> uris = remo.getURIs();
            for (URIish uri : uris) {
              System.out.println("Checking URI: " + uri + " - " + proposal.equals(uri));
              if (proposal.equals(uri)) {
                return true;
              }
            }
          }

        } catch (Exception e) {
          CloudBeesCorePlugin.getDefault().getLogger().error(e);
        }
      }
    } catch (Exception e) {
      CloudBeesCorePlugin.getDefault().getLogger().error(e);
    }

    return false;
  }

  @Override
  public boolean openRemoteFile(final JenkinsScmConfig scmConfig, final ChangeSetPathItem item,
      final IProgressMonitor monitor) {
    for (JenkinsScmConfig.Repository repo : scmConfig.repos) {
      if (!ForgeSync.TYPE.GIT.equals(repo.type)) {
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
      // TODO extract repo search into separate method
      RepositoryCache repositoryCache = org.eclipse.egit.core.Activator.getDefault().getRepositoryCache();
      Repository repository = null;
      URIish proposal = new URIish(repo);
      List<String> reps = Activator.getDefault().getRepositoryUtil().getConfiguredRepositories();
      all: for (String rep : reps) {
        try {

          Repository fr = repositoryCache.lookupRepository(new File(rep));
          List<RemoteConfig> allRemotes = RemoteConfig.getAllRemoteConfigs(fr.getConfig());
          for (RemoteConfig remo : allRemotes) {
            List<URIish> uris = remo.getURIs();
            for (URIish uri : uris) {
              System.out.println("Checking URI: " + uri + " - " + proposal.equals(uri));
              if (proposal.equals(uri)) {
                repository = fr;
                break all;
              }
            }
          }
        } catch (Exception e) {
          System.out.println(e); // TODO
          //          CloudBeesCorePlugin.getDefault().getLogger().error(e);
        }
      }

      System.out.println("Repo: " + repository);

      if (repository == null) {
        throw new CloudBeesException("Failed to find mapped repository for " + repo);
      }

      ObjectId commitId = ObjectId.fromString(item.parent.id);
      RevWalk rw = new RevWalk(repository);
      RevCommit rc = rw.parseCommit(commitId);
      final IFileRevision rev = CompareUtils.getFileRevision(item.path, rc, repository, null);

      final IEditorPart[] editor = new IEditorPart[1];

      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          IWorkbenchPage activePage = CloudBeesUIPlugin.getActiveWindow().getActivePage();
          try {
            editor[0] = Utils.openEditor(activePage, rev, monitor);
          } catch (CoreException e) {
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
  public void addToRepository(TYPE type, Repo repo, IProject project, IProgressMonitor monitor) {
    if (type != TYPE.GIT) {
      return;
    }
    // TODO
  }

  @Override
  public boolean isUnderSvnScm(IProject project) {
    return false;
  }

  @Override
  public Repo getSvnRepo(IProject project) {
    return null;
  }
}
