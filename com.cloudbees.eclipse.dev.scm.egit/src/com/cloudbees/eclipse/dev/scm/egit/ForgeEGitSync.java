/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.dev.scm.egit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.securestorage.UserPasswordCredentials;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.egit.ui.internal.CompareUtils;
import org.eclipse.egit.ui.internal.SecureStoreUtils;
import org.eclipse.egit.ui.internal.clone.GitImportWizard;
import org.eclipse.egit.ui.internal.provisional.wizards.GitRepositoryInfo;
import org.eclipse.egit.ui.internal.provisional.wizards.IRepositorySearchResult;
import org.eclipse.egit.ui.internal.provisional.wizards.NoRepositoryInfoException;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.jsch.internal.core.JSchCorePlugin;
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
import com.cloudbees.eclipse.ui.GitConnectionType;
import com.cloudbees.eclipse.ui.PreferenceConstants;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;

/**
 * Forge repo sync provider for EGIT
 * 
 * @author ahtik
 */
public class ForgeEGitSync implements ForgeSync {

  static private String prssh = "ssh://git@";
  static private String prhttps = "https://";

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
    internalSync(instance, monitor);
  }

  public static boolean internalSync(final ForgeInstance instance, final IProgressMonitor monitor) {

    if (!ForgeInstance.TYPE.GIT.equals(instance.type)) {
      return false;
    }

    String u = instance.url;

    if (u == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    final String url = reformatGitUrlToCurrent(u);

    try {
      monitor.beginTask("Cloning EGit repository '" + url + "'", 10);

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

          //Clipboard clippy = new Clipboard(Display.getCurrent());

          String clipurl = url;

          GitConnectionType type = CloudBeesUIPlugin.getDefault().getGitConnectionType();

          final GitRepositoryInfo repoInfo = new GitRepositoryInfo(clipurl);
          IRepositorySearchResult repoSearch = new IRepositorySearchResult() {
            public GitRepositoryInfo getGitRepositoryInfo() throws NoRepositoryInfoException {
              return repoInfo;
            }
          };

          if (type.equals(GitConnectionType.HTTPS)) {

            try {

              String username = CloudBeesUIPlugin.getDefault().getPreferenceStore()
                  .getString(PreferenceConstants.P_EMAIL);
              String password = CloudBeesUIPlugin.getDefault().readP();

              CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
              UserPasswordCredentials credentials = new UserPasswordCredentials(username, password);

              repoInfo.setShouldSaveCredentialsInSecureStore(true);
              repoInfo.setCredentials(username, password);

            } catch (StorageException e) {
              e.printStackTrace();
            }

          }

          GitImportWizard cloneWizard = new GitImportWizard(repoSearch);

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

    if (instance != null && instance.status.equals(ForgeInstance.STATUS.SYNCED)) {
      return true;
    }
    return false;

  }

  protected static boolean isAlreadyCloned(final String url) {
    try {

      if (url == null) {
        return false;
      }

      String bareUrl = stripProtocol(url);
      if (bareUrl == null) {
        return false;
      }

      URIish proposalHTTPS = new URIish(prhttps + bareUrl);
      URIish proposalSSH = new URIish(prssh + bareUrl);

      List<String> reps = Activator.getDefault().getRepositoryUtil().getConfiguredRepositories();
      for (String repo : reps) {
        try {

          FileRepository fr = new FileRepository(new File(repo));
          List<RemoteConfig> allRemotes = RemoteConfig.getAllRemoteConfigs(fr.getConfig());
          for (RemoteConfig remo : allRemotes) {
            List<URIish> uris = remo.getURIs();
            for (URIish uri : uris) {
              //System.out.println("Checking URI: " + uri + " - " + proposal.equals(uri));
              if (proposalHTTPS.equals(uri) || proposalSSH.equals(uri)) {
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

  private static String stripProtocol(String url) {
    if (url.toLowerCase().startsWith(prhttps)) {
      return url.substring(prhttps.length());
    } else if (url.toLowerCase().startsWith(prssh)) {
      return url.substring(prssh.length());
    }
    return null;
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

  public static File cloneRepo(String url, URI locationURI, IProgressMonitor monitor) throws InterruptedException,
      InvocationTargetException, URISyntaxException {
    //GitScmUrlImportWizardPage
    //GitImportWizard

    // See ProjectReferenceImporter for hints on cloning and importing!
    /*
        CloneOperation clone = Utils.createInstance(CloneOperation.class, new Class[] { URIish.class, Boolean.TYPE,
                    Collection.class, File.class, String.class, String.class, Integer.TYPE }, new Object[] { new URIish(url),
                    true, Collections.EMPTY_LIST, new File(""), null, "origin", 5000 });
                if (clone == null) {
                  // old constructor didn't have timeout at the end
                clone = Utils.createInstance(CloneOperation.class, new Class[] { URIish.class, Boolean.TYPE, Collection.class,
                      File.class, String.class, String.class }, new Object[] { new URIish(url), true, Collections.EMPTY_LIST,
                      new File(""), null, "origin" });
        
      }*/
    if (monitor.isCanceled()) {
      return null;
    }

    try {
      int timeout = 60;

      // force plugin activation
      Activator.getDefault().getLog();

      Platform.getPreferencesService().getInt("org.eclipse.egit.core", UIPreferences.REMOTE_CONNECTION_TIMEOUT, 60,
          null);

      String branch = "master";

      url = reformatGitUrlToCurrent(url);

      URIish gitUrl = new URIish(url);
      File workDir = new File(locationURI);
      //final File repositoryPath = workDir.append(Constants.DOT_GIT_EXT).toFile();

      String refName = Constants.R_HEADS + branch;

      GitConnectionType type = CloudBeesUIPlugin.getDefault().getGitConnectionType();

      final CloneOperation cloneOperation = new CloneOperation(gitUrl, true, null, workDir, refName,
          Constants.DEFAULT_REMOTE_NAME, timeout);

      // https password
      if (type.equals(GitConnectionType.HTTPS)) {

        try {

          String username = CloudBeesUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_EMAIL);
          String password = CloudBeesUIPlugin.getDefault().readP();

          CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);

          // Store to secure storage to ensure the connection remains available without reentering the password

          UserPasswordCredentials credentials = new UserPasswordCredentials(username, password);

          URIish uri = new URIish(url);
          SecureStoreUtils.storeCredentials(credentials, uri);

          cloneOperation.setCredentialsProvider(credentialsProvider);

        } catch (StorageException e) {
          throw new InvocationTargetException(new Exception("Failed to read credentials!", e));
        }
      }

      cloneOperation.run(monitor);

      return workDir;
    } catch (final InvocationTargetException e1) {
      throw e1;
    } catch (InterruptedException e2) {
      throw e2;
    }

  }

  private static String reformatGitUrlToCurrent(String url) {
    String burl = stripProtocol(url);
    if (burl == null) {
      return burl;
    }

    GitConnectionType type = CloudBeesUIPlugin.getDefault().getGitConnectionType();
    if (type.equals(GitConnectionType.HTTPS)) {
      return prhttps + burl;
    }
    return prssh + burl;
  }

  public static boolean validateSSHConfig(IProgressMonitor monitor) throws CloudBeesException, JSchException {
    IJSchService ssh = CloudBeesScmEgitPlugin.getDefault().getJSchService();
    if (ssh == null) {
      throw new CloudBeesException("SSH not available!");
    }

    Session sess = ssh.createSession("git.cloudbees.com", -1, "git");

    ssh.connect(sess, 60000, monitor);
    boolean ret = sess.isConnected();

    if (ret) {
/*      try {
        sess.write(new Packet(new Buffer("echo".getBytes("ISO-8859-1"))));
      } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
*/
      ChannelExec channelExec = (ChannelExec) sess.openChannel("exec");

      try {
        
        InputStream inerr = channelExec.getErrStream();
        
        channelExec.setCommand("echo");
        channelExec.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inerr));
        String line;
        
        
        while ((line = reader.readLine()) != null) {
          String mstr = "CloudBees Forge: You have successfully authenticated as ";
          if (line.contains(mstr)) {
            String username = line.substring(mstr.length());
            int spaceidx = username.indexOf(" ");
            if (spaceidx>0) {
              username = username.substring(0, spaceidx);
              
              String currentUser = CloudBeesCorePlugin.getDefault().getGrandCentralService().getCurrentUsername(monitor);
              // System.out.println("Matched username: '"+username+"'. Validating for: '"+currentUser+"'");
              if (currentUser!=null) {
                ret = currentUser.equalsIgnoreCase(username);
                if (!ret) {
                  throw new CloudBeesException("Eclipse SSH key is authenticating as '"+username+"' but current CloudBees user is '"+currentUser+"'");
                }
              }
            }
          }
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      channelExec.disconnect();
      sess.disconnect();

    }

    sess.disconnect();

    // Not checking the repo read access for the given key as that would require at least one git repo to exist for this user and depending on this does not make sense at this point
    /*
        if (ret) {
          // attempt git clone operation to validate the git key
          try {
            //Caused by: org.eclipse.jgit.errors.NoRemoteRepositoryException: ssh://git@git.cloudbees.com/grandomstate/nonexistigthingiea234: CloudBees Forge: HTTP Error 404: Not Found
            //Caused by: org.eclipse.jgit.errors.NoRemoteRepositoryException: ssh://git@git.cloudbees.com/ahtitestacc/ahtitestrepo01.git: CloudBees Forge: Repository read access denied to 'ahtik'
            URI location = new File(System.getProperty("java.io.tmpdir")+"/rnd99491").toURI();
            cloneRepo("ssh://git@git.cloudbees.com/grandomstate", location, new NullProgressMonitor());
          } catch (Exception e) {
            e.printStackTrace();
            //throw e;
          }
          
        }*/

    return ret;

  }

}
