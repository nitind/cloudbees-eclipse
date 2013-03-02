package com.cloudbees.eclipse.dev.scm.egit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.Activator;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jsch.core.IJSchService;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.util.Utils;
import com.jcraft.jsch.JSchException;

public class TestGitClone {

  static {
    //SshSessionFactory.getInstance().
    
    // force ssh setup
    org.eclipse.egit.ui.Activator.getDefault().getLog();
    
    IJSchService ssh = CloudBeesScmEgitPlugin.getDefault().getJSchService();
    
    
    BundleContext bc = Activator.getDefault().getBundle().getBundleContext();
    ServiceReference<IJSchService> ref = bc.getServiceReference(IJSchService.class);
    IJSchService ssh2 = bc.getService(ref);
    
    File f = new File("C:\\Users\\ahtik\\.ssh\\github_rsa");
    InputStream is = null;
    try {
      is = new FileInputStream(f);
      String prvkey = Utils.readString(is);
      ssh.getJSch().addIdentity(f.getAbsolutePath());
      ssh2.getJSch().addIdentity(f.getAbsolutePath());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    } finally {
      if (is!=null) {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
      }
    }
  }

  @Test
  public void testSshConfig() throws CloudBeesException, JSchException {
    ForgeEGitSync s = new ForgeEGitSync();
    assertTrue(s.validateSSHConfig(new NullProgressMonitor()));
  }

  @Test
  public void testClone() throws InterruptedException, InvocationTargetException, URISyntaxException {
    ForgeEGitSync s = new ForgeEGitSync();
    //s.cloneRepo("ssh://git@git.cloudbees.com/ahtikaccount2/scalatest-2.git");
    //String host = "git.cloudbees.com";
    //String reppath = "/ahtikaccount2/scalatest-2.git";    

    //uri ssh://git@git.cloudbees.com/ahtikaccount2/nodetest2.git
    //host git.cloudbees.com
    //reppath /ahtikaccount2/nodetest2.git
    String projname = "scalatest2";
    String url = "ssh://git@git.cloudbees.com/ahtikaccount2/scalatest-2.git";
    try {
      File f = s.cloneRepo(url, new URI("file://c:/testdir"), new NullProgressMonitor());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
