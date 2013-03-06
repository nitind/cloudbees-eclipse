/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.dev.core.junit;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.junit.Test;

public class JUnitReportSupportTest {

  @Test
  public void testImportJenkins() throws Exception {
    InputStream report = JUnitReportSupport.class.getResourceAsStream("testReport.xml");
    TestRunSession sess = JUnitReportSupport.importJenkinsTestRunSession(null, null, report);
    assertEquals(3, sess.getTotalCount());
    assertEquals(0, sess.getFailureCount());
    assertEquals(0, sess.getErrorCount());
  }

  @Test
  public void testImportJenkins2() throws Exception {
    InputStream report = JUnitReportSupport.class.getResourceAsStream("testReport2.xml");
    TestRunSession sess = JUnitReportSupport.importJenkinsTestRunSession(null, null, report);
    assertEquals(7788, sess.getTotalCount());
    assertEquals(0, sess.getFailureCount());
    assertEquals(0, sess.getErrorCount());
  }

  @Test
  public void testImportJenkins3() throws Exception {
    InputStream report = JUnitReportSupport.class.getResourceAsStream("testReport3.xml");
    TestRunSession sess = JUnitReportSupport.importJenkinsTestRunSession(null, null, report);
    assertEquals(43, sess.getTotalCount());
    assertEquals(0, sess.getFailureCount());
    assertEquals(0, sess.getErrorCount());
  }

  @Test
  public void testImportJenkins4() throws Exception {
    InputStream report = JUnitReportSupport.class.getResourceAsStream("testReport4.xml");
    TestRunSession sess = JUnitReportSupport.importJenkinsTestRunSession(null, null, report);
    assertEquals(519, sess.getTotalCount());
    assertEquals(2, sess.getFailureCount());
    assertEquals(0, sess.getErrorCount());
  }

  @Test
  public void testImportJenkins5() throws Exception {
    InputStream report = JUnitReportSupport.class.getResourceAsStream("testReport5.xml");
    TestRunSession sess = JUnitReportSupport.importJenkinsTestRunSession(null, null, report);
    assertEquals(4, sess.getTotalCount());
    assertEquals(2, sess.getFailureCount());
    assertEquals(0, sess.getErrorCount());
  }

}
