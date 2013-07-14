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
package com.cloudbees.eclipse.ui.console;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import org.eclipse.ui.console.IOConsoleOutputStream;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.BeesSDK;

/**
 * Wrapper to run Bees command line client commands and access input/output streams
 * 
 * @author ahtik
 */
public class BeesRunner {

  private final String linesep = System.getProperty("line.separator");
  
  //private boolean streamProcessing = false;
  private OutputStream currentOutputStream = null;

  public void run(final BeesConsoleSession sess) {

    Thread inputThread = new Thread(new Runnable() {
      public void run() {
        try {
          Scanner scan = new Scanner(sess.getInputStream());
          while (scan.hasNextLine()) {

            final String input = scan.nextLine();
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

            final IOConsoleOutputStream out = sess.newOutputStream();
            // we need to wrap this into a separate thread in order to catch follow-up readlines even when the runbees hangs (waits for more input)
            Thread t = new Thread(new Runnable() {
              public void run() {
                try {
                  runBees(input, out);
                } catch (CloudBeesException e) {
                  e.printStackTrace();
                } catch (IOException e) {
                  e.printStackTrace();
                } finally {
                  if (out != null) {
                    try {
                      out.close();
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }
                }
              }
            },"CloudBees Console runner");
            t.start();

          }

        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    });

    inputThread.start();

  }

  private void runBees(String cmd, IOConsoleOutputStream out) throws CloudBeesException, IOException {

    if (currentOutputStream != null) { // current stream in progress, potentially waiting for the user input. let's send it to the current stream.
      currentOutputStream.write((cmd+linesep).getBytes("UTF-8"));
      currentOutputStream.flush();
      return;
    }

    OutputStreamWriter osw = new OutputStreamWriter(out);
    BufferedWriter writer = new BufferedWriter(osw);

    try {
      final ProcessBuilder pb = BeesSDK.createBeesProcess(true, cmd);

      Process p = null;
      try {
        p = pb.start();
      } catch (Exception e) {
        writer.write("Error while running CloudBees SDK: " + e.getMessage() + "\n");
        e.printStackTrace(new PrintWriter(writer));
        return;
      }

      //    String line;

      InputStream stdin = p.getInputStream();

      byte[] b = new byte[4096 * 10];

      currentOutputStream = p.getOutputStream();
      try {
        for (int n; (n = stdin.read(b)) != -1;) {
          writer.write(new String(b, 0, n, "UTF-8"));
          writer.flush();
          BeesConsole.moveCaret();
        }
      } finally {
        currentOutputStream = null;
      }

    } catch (CloudBeesException e) {
      writer.write("Error while creating CloudBees SDK process: " + e.getMessage() + "\n");
      e.printStackTrace(new PrintWriter(writer));
    }

    writer.write("bees ");
    writer.flush();
    BeesConsole.moveCaret();

  }

}
