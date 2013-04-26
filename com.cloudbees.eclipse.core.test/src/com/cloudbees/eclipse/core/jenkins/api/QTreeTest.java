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
package com.cloudbees.eclipse.core.jenkins.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.cloudbees.eclipse.core.CloudBeesException;

public class QTreeTest {


  @Test
  public void testQTree() throws CloudBeesException {

    String qtree = JenkinsBuildDetailsResponse.QTREE;
    System.out.println("Qtree: " + qtree);

    assertEquals(-1, qtree.indexOf("builtOnchangeSet"));
    assertTrue(qtree.indexOf("builtOn,changeSet") > 0);

  }

}
