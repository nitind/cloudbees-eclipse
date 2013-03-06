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
package com.cloudbees.eclipse.core.jenkins.api;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class JenkinsConfigParserTest {

  @Test
  public final void testParseGitConfig() throws Exception {
    InputStream in = this.getClass().getResourceAsStream("CB_config_git.xml");
    in = new BufferedInputStream(in);

    JenkinsScmConfig config = JenkinsConfigParser.parse(in);

    Set<String> scm = new HashSet<String>();
    scm.add(config.repos[0].url);
    scm.add(config.repos[1].url);
    Set<String> expected = new HashSet<String>(
        Arrays.asList(new String[] { "ssh://git@git.cloudbees.com/grandomstate/testgit.git",
            "ssh://git@git.cloudbees.com/grandomstate/testgit2.git" }));
    assertEquals(scm.toString(), expected, scm);
  }

  @Test
  public final void testParseSvnConfig() throws Exception {
    InputStream in = this.getClass().getResourceAsStream("CB_config_svn.xml");
    in = new BufferedInputStream(in);

    JenkinsScmConfig config = JenkinsConfigParser.parse(in);

    String url = config.repos[0].url;
    assertEquals(url, "https://svn-grandomstate.forge.cloudbees.com/testsvn", url);
  }

  @Test
  public final void testParseCvsConfig() throws Exception {
    InputStream in = this.getClass().getResourceAsStream("Eclipse_config_cvs.xml");
    in = new BufferedInputStream(in);

    JenkinsScmConfig config = JenkinsConfigParser.parse(in);

    String url = config.repos[0].url;
    assertEquals(url, ":pserver:anonymous@dev.eclipse.org:/cvsroot/mylyn", url);
  }

}
