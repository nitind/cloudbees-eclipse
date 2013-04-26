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
package com.cloudbees.eclipse.core;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;

public class AuthJenkinsServiceTest {


  @Test
  public void testSSOAuth() throws CloudBeesException {
    JenkinsInstance ni = new JenkinsInstance("grandsomstate", "https://grandomstate.ci.cloudbees.com",
        "ahti@codehoop.com", "xxx", true, true);

    JenkinsService s = new JenkinsService(ni);

    JenkinsInstanceResponse vs = s.getInstance(null);

    assertTrue(vs.views.length > 0);

  }

}
