package com.cloudbees.eclipse.ui.internal.forge;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.ui.internal.clone.GitCloneWizard;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;

/**
 * Forge repo sync provider for EGIT
 * 
 * @author ahtik
 */
public class ForgeEGitSync implements ForgeSync {

  public ACTION sync(TYPE type, Properties props, final IProgressMonitor monitor) throws CloudBeesException {

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

}
