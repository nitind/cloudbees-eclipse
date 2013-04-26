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
package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;

public class JenkinsFinishPage extends AbstractJenkinsPage {

  private static final String DESCR = "The configured Jenkins location will appear in CloudBees Eclipse views,\n\n"
      + "where you can browse Jenkins views, jobs, job details, console logs,\n\n"
      + "JUnit test reports, run new builds and perform other actions!"; // TODO i18n
  private String error;
  private Label labelContentText;

  /**
   * Create the wizard.
   */
  public JenkinsFinishPage(final JenkinsInstance ni) {
    super("finish");
    setJenkinsInstance(ni);
  }

  public void initText(final Exception e) {
    this.error = null;
    if (e != null) {
      // TODO format error nicely, so user can react properly
      this.error = e.getLocalizedMessage();
      Throwable cause = e.getCause();
      while (cause != null) {
        this.error += "\n" + cause.getLocalizedMessage();
        cause = cause.getCause();
      }
    }

    if (this.error == null) {
      setTitle("Congratulations! Jenkins is configured properly");
      setMessage("Specified Jenkins location is working well!");
      //setDescription("Wizard Page description");

    } else {
      setTitle("Failure! Jenkins is not configured properly");
      setMessage("Specified Jenkins location is not working, but you can add it nonetheless!");
      //setDescription("Wizard Page description");
    }

    updateContent();
  }

  /**
   * Create contents of the wizard.
   * @param parent
   */
  public void createControl(final Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);

    setControl(container);
    GridLayout gl_container = new GridLayout(1, false);
    gl_container.marginWidth = 20;
    gl_container.marginHeight = 0;
    container.setLayout(gl_container);

    this.labelContentText = new Label(container, SWT.WRAP);
    this.labelContentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
    updateContent();
  }

  public void updateContent() {
    if (this.labelContentText == null) {
      return;
    }

    String mess = "";

    if (this.error != null && this.error.trim().length() > 0) {
      mess += "Error: \n" + this.error + "\n\n";
    } else {
      mess += DESCR;
    }

    this.labelContentText.setText(mess);
    this.labelContentText.getParent().layout();
  }
}
