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

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;
import com.cloudbees.eclipse.core.gc.api.ClickStartTemplate;
import com.cloudbees.eclipse.core.gc.api.KeysUsingAuthResponse;

public class ClickStartTest {

  private final static Logger log = Logger.getLogger(ClickStartTest.class);

  public final static String CB_TEST_EMAIL = "cb.test.email";
  public final static String CB_TEST_PASSWORD = "cb.test.password";

  static {
    //System.setProperty("cloudbees.host", "beescloud.com");
    System.setProperty(CB_TEST_EMAIL, "xxx");
    System.setProperty(CB_TEST_PASSWORD, "xxx");
  }

  @Test
  public void testLoadTemplates() throws CloudBeesException, InterruptedException {
    GrandCentralService gcs = new GrandCentralService();

    gcs.setAuthInfo(System.getProperty(CB_TEST_EMAIL), System.getProperty(CB_TEST_PASSWORD));
    assertTrue(gcs.validateUser(null));
    KeysUsingAuthResponse auth = gcs.getCachedAuthInfo(false, new NullProgressMonitor()).getAuth();
    String key = auth.api_key;
    String secret = auth.secret_key;

    assertNotNull(key);
    assertNotNull(secret);

    ClickStartService cs = new ClickStartService();
    cs.setAuth(key, secret);

    //AuthInfo auth = getAuthInfo(new NullProgressMonitor());     

    Collection<ClickStartTemplate> templates = cs.loadTemplates(new NullProgressMonitor());

    log.info("Received templates: " + templates.size());
    Iterator<ClickStartTemplate> it = templates.iterator();
    while (it.hasNext()) {
      ClickStartTemplate inst = (ClickStartTemplate) it.next();
      log.info("Template id:" + inst.id);
    }

    assertTrue("Expected at least 5 templates from the repository! Got " + templates.size(), templates.size() > 5);

    //http://localhost:8080/api/apps/json/template?template=https://raw.github.com/CloudBees-community/lift_template/master/clickstart.json
    ClickStartTemplate template = templates.iterator().next();
    String tid = template.id;

    log.info("Loading details of the first template.");
    cs.loadTemplateDetails(tid);

    log.info("Attempting to create from the first template: " + tid);

    ClickStartCreateResponse resp = cs.create(tid,"grandomstate", "cstest001");
    String reservationId = resp.reservationId;
    log.info("Received creation ID:" + reservationId);

    assertNotNull(reservationId);
    assertNotSame("", resp.reservationId);

    while (cs.getCreateProgress(reservationId)<100) {
      log.info(".. waiting for the creation to complete.");
      Thread.currentThread().sleep(1000);
    }

    log.info(".. created!");

    // System.out.println("curl -i --user "+key+":"+secret+" "+"https://api-staging.cloudbees.com/v2/clickstart/apps/json/templates");

    /*    
        
        String[] accounts = gcs.getAccounts(new NullProgressMonitor());
        System.out.println("Accounts list size: "+accounts.length);
        
        String csUrl = "https://clickstart.cloudbees.com/";
    *///list of templates https://clickstart.cloudbees.com/api/apps/json/templates

    //load template details
    //http://localhost:8080/api/apps/json/template?template=https://raw.github.com/CloudBees-community/lift_template/master/clickstart.json

    //POST template
    //"http://localhost:8080/api/apps/json/launch?account=michaelnealeclickstart2&name=nodecli&template=https://raw.github.com/CloudBees-community/nodejs-clickstart/master/clickstart.json"

    // poll progress
    //http://localhost:8080/api/apps/json/progress?reservation_id=http://nodejspn1.michaelnealeclickstart2.cloudbees.net

  }

}
