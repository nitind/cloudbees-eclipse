package com.cloudbees.eclipse.ui.console;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import org.eclipse.ui.console.IOConsoleOutputStream;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;
import com.cloudbees.eclipse.run.sdk.CBSdkActivator;

/**
 * Wrapper to run Bees command line client commands and access input/output streams
 * 
 * @author ahtik
 */
public class BeesRunner {

  public void run(final BeesConsoleSession sess) {

    Thread inputThread = new Thread(new Runnable() {
      public void run() {
        try {
          Scanner scan = new Scanner(sess.getInputStream());
          while (scan.hasNextLine()) {

            String input = scan.nextLine();
            if (input.length() == 0) {

              IOConsoleOutputStream out = sess.newOutputStream();
              try {
                out.write("bees ");
                out.flush();
                BeesConsole.moveCaret();
              } finally {
                if (out != null) {
                  out.close();
                }
              }

              continue;
            }

            IOConsoleOutputStream out = sess.newOutputStream();
            try {
              runBees(input, out);
            } catch (CloudBeesException e) {
              e.printStackTrace();
            } finally {
              if (out != null) {
                out.close();
              }
            }

          }

        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    });

    inputThread.start();

  }

  private void runBees(String cmd, IOConsoleOutputStream out) throws CloudBeesException, IOException {

    GrandCentralService grandCentralService;
    grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);
    String secretKey = "-Dbees.apiSecret=" + cachedAuthInfo.getAuth().secret_key;//$NON-NLS-1$
    String authKey = "-Dbees.apiKey=" + cachedAuthInfo.getAuth().api_key;//$NON-NLS-1$
    String beesHome = "-Dbees.home=" + CBSdkActivator.getDefault().getBeesHome();
    String beesHomeDir = CBSdkActivator.getDefault().getBeesHome();

    String java = System.getProperty("eclipse.vm");

    final ProcessBuilder pb = new ProcessBuilder(java, "-Xmx256m", beesHome, secretKey, authKey, "-cp", beesHomeDir
        + "lib/cloudbees-boot.jar", "com.cloudbees.sdk.boot.Launcher", cmd);
    pb.environment().put("BEES_HOME", beesHomeDir);
    pb.directory(new File(beesHomeDir));
    pb.redirectErrorStream(true);

    final Process p = pb.start();

    String line;

    InputStream stdin = p.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

    while ((line = reader.readLine()) != null) {
      writer.write(line + "\n");
      writer.flush();
    }

    writer.write("bees ");
    writer.flush();
    BeesConsole.moveCaret();

  }
}
