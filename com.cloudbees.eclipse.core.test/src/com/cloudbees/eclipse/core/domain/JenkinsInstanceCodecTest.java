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
package com.cloudbees.eclipse.core.domain;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class JenkinsInstanceCodecTest {

  @Test
  public void testCodec() {
    List<JenkinsInstance> list = new ArrayList<JenkinsInstance>();
    JenkinsInstance i1 = new JenkinsInstance("label1", "url1", "username1", "password1", true, false);
    JenkinsInstance i2 = new JenkinsInstance("label2", "url2", null, null, false, false);
    JenkinsInstance i3 = new JenkinsInstance("label3", "url3", "username3", "", true, false);
    list.add(i1);
    list.add(i2);
    list.add(i3);

    String ret = JenkinsInstance.encode(list);
    System.out.println("ENCODED:" + ret);
    List<JenkinsInstance> decoded = JenkinsInstance.decode(ret);
    for (JenkinsInstance inst : decoded) {
      System.out.println(inst);
    }

    //TODO add real validation
  }

}
