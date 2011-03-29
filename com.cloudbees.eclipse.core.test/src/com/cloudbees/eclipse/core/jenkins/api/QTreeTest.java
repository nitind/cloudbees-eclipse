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
