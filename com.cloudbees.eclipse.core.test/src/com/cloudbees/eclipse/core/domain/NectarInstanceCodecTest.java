package com.cloudbees.eclipse.core.domain;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class NectarInstanceCodecTest {

  @Test
  public void testCodec() {
    List<NectarInstance> list = new ArrayList<NectarInstance>();
    NectarInstance i1 = new NectarInstance("label1", "url1", "username1", "password1", true);
    NectarInstance i2 = new NectarInstance("label2", "url2", null, null, false);
    NectarInstance i3 = new NectarInstance("label3", "url3", "username3", "", true);
    list.add(i1);
    list.add(i2);
    list.add(i3);

    String ret = NectarInstance.encode(list);
    System.out.println("ENCODED:" + ret);
    List<NectarInstance> decoded = NectarInstance.decode(ret);
    for (NectarInstance inst : decoded) {
      System.out.println(inst);
    }

    //TODO add real validation
  }

}
