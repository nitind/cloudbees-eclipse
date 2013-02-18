package com.cloudbees.eclipse.dev.scm.egit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.ui.internal.CompareUtils;
import org.eclipse.egit.ui.internal.clone.GitCloneWizard;
import org.eclipse.egit.ui.internal.clone.GitImportWizard;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jsch.internal.core.JSchCorePlugin;
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
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance.STATUS;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;
import com.cloudbees.eclipse.dev.core.CloudBeesDevCorePlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

/**
 * Forge repo sync provider for EGIT
 *
 * @author ahtik
 */
public class ForgeEGitSync implements ForgeSync {

  @Override
  public void updateStatus(final ForgeInstance instance, final IProgressMonitor monitor) throws CloudBeesException {
    if (!ForgeInstance.TYPE.GIT.equals(instance.type)) {
      return;
    }

    final String url = instance.url;

    if (url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    try {
      monitor.beginTask("Checking EGit repository '" + url + "'", 10);

      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          monitor.subTask("Checking already cloned local repositories");
          monitor.worked(2);

          if (isAlreadyCloned(url)) {
            monitor.worked(8);
            instance.status = ForgeInstance.STATUS.SYNCED;
          } else {
            if (instance.status != STATUS.SKIPPED) { // user might have deleted it and need to sync again
              instance.status = ForgeInstance.STATUS.UNKNOWN;
            }

            //System.out.println("Repo is unknown for EGit: " + instance.url);
          }
        }
      });

    } finally {
      monitor.worked(10);
      monitor.done();
    }
  }

  @Override
  public void sync(final ForgeInstance instance, final IProgressMonitor monitor) throws CloudBeesException {

    if (!ForgeInstance.TYPE.GIT.equals(instance.type)) {
      return;
    }

    final String url = instance.url;

    if (url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    try {
      monitor.beginTask("Syncing EGit repository '" + url + "'", 10);

      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          monitor.subTask("Checking already cloned local repositories");
          monitor.worked(2);

          if (isAlreadyCloned(url)) {
            monitor.worked(8);
            instance.status = ForgeInstance.STATUS.SYNCED;
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
            instance.status = ForgeInstance.STATUS.SYNCED;
          } else {
            instance.status = ForgeInstance.STATUS.SKIPPED;
          }
        }
      });

    } finally {
      monitor.worked(10);
      monitor.done();
    }
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
              //System.out.println("Checking URI: " + uri + " - " + proposal.equals(uri));
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
      if (!ForgeInstance.TYPE.GIT.equals(repo.type)) {
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
              CloudBeesDevCorePlugin.getDefault().getLogger()
                  .info("Checking URI: " + uri + " - " + proposal.equals(uri));
              if (proposal.equals(uri)) {
                repository = fr;
                break all;
              }
            }
          }
        } catch (Exception e) {
          CloudBeesDevCorePlugin.getDefault().getLogger().error(e);
        }
      }

      CloudBeesDevCorePlugin.getDefault().getLogger().info("Repo: " + repository);

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
      CloudBeesDevCorePlugin.getDefault().getLogger().error(e); // TODO handle better?
      return false;
    }
  }

  @Override
  public void addToRepository(final ForgeInstance instance, final IProject project, final IProgressMonitor monitor) {
    if (!ForgeInstance.TYPE.GIT.equals(instance.type)) {
      return;
    }

    // TODO
  }

  @Override
  public boolean isUnderSvnScm(final IProject project) {
    return false;
  }

  @Override
  public ForgeInstance getMainRepo(final IProject project) {
    return null;
  }

  private void generateRSAKeys() {
    try {
      int type = KeyPair.RSA;

      final KeyPair[] _kpair = new KeyPair[1];
      final int __type = type;
      final JSchException[] _e = new JSchException[1];
      //      BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
      //        public void run() {
      //          try {
      _kpair[0] = KeyPair.genKeyPair(JSchCorePlugin.getPlugin().getJSch(), __type);
      //          } catch (JSchException e) {
      //            _e[0] = e;
      //          }
      //        }
      //      });
      if (_e[0] != null) {
        throw _e[0];
      }
      KeyPair kpair = _kpair[0];

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      String kpairComment = "RSA-1024"; //$NON-NLS-1$
      kpair.writePublicKey(out, kpairComment);
      out.close();
      String publicKey = out.toString();

      //      keyFingerPrintText.setText(kpair.getFingerPrint());
      //      keyCommentText.setText(kpairComment);
      //      keyPassphrase1Text.setText(""); //$NON-NLS-1$
      //      keyPassphrase2Text.setText(""); //$NON-NLS-1$

    } catch (IOException ee) {
      //      ok = false;
    } catch (JSchException ee) {
      //      ok = false;
    }
    //    if (!ok) {
    //      MessageDialog.openError(getShell(), Messages.CVSSSH2PreferencePage_error, Messages.CVSSSH2PreferencePage_47);
    //    }

  }
  
  public void cloneRepo(String url) {
    //GitScmUrlImportWizardPage
    //GitImportWizard
    
    // See ProjectReferenceImporter for hints on cloning and importing!
    
  }
  
  public void cloneFromClickStart() {
    
  }
}
