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
