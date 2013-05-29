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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.gc.api.AccountNameRequest;
import com.cloudbees.eclipse.core.gc.api.AccountNameResponse;
import com.cloudbees.eclipse.core.gc.api.AccountNamesRequest;
import com.cloudbees.eclipse.core.gc.api.AccountNamesResponse;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusRequest;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;
import com.cloudbees.eclipse.core.gc.api.KeysUsingAuthRequest;
import com.cloudbees.eclipse.core.gc.api.KeysUsingAuthResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.google.gson.Gson;

/**
 * Central access to CloudBees Grand Central API
 * 
 * @author ahtik
 */
public class GrandCentralService {

  public static final String HOST = System.getProperty("cloudbees.host", "cloudbees.com");

  // private static final String BASE_URL =
  // "https://grandcentral.cloudbees.com/api/";
  private static final String BASE_URL = "https://grandcentral." + HOST + "/api/";

  public static final String GC_BASE_URL = "https://grandcentral." + HOST;

  private final ForgeSyncService forgeSyncService;

  private String email = null;
  private String password = null;

  private String activeAccount = null;

  private String[] accountsCache = null;

  private AuthInfo authInfo = null;

  //private boolean accountSelectionActive = false;

  public GrandCentralService() {
    this.forgeSyncService = new ForgeSyncService();
  }

  public ForgeSyncService getForgeSyncService() {
    return this.forgeSyncService;
  }

  public void setAuthInfo(final String email, final String password) {
    this.email = email;
    this.password = password;
    activeAccount = null;
    accountsCache = null;
  }

  /**
   * Validates user credential against CloudBees SSO authentication server. TODO Refactor to json-based validation
   * 
   * @param email
   * @param password
   * @param monitor
   * @return
   * @throws CloudBeesException
   */
  public boolean validateUser(final IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return false;
    }

    AuthInfo auth = getAuthInfo(monitor);

    return auth.getAuth().api_key.length() > 0 && auth.getAuth().secret_key.length() > 0
        && auth.getAuth().uid.length() > 0;

  }

  public boolean hasAuthInfo() {
    return this.email != null && this.email.trim().length() > 0 && this.password != null
        && this.password.trim().length() > 0;
  }

  public AuthInfo getCachedAuthInfo(final boolean refresh, IProgressMonitor monitor) throws CloudBeesException {
    //authInfo = null;
    if (refresh || authInfo == null) {
      authInfo = getAuthInfo(monitor);
    }
    return authInfo;
  }

  @Deprecated
  private String getCachedPrimaryUser(final boolean refresh) throws CloudBeesException {
    String user = null;
    if (refresh || user == null) {
      user = getPrimaryAccount(null);
    }
    return user;

  }

  private AuthInfo getAuthInfo(final IProgressMonitor monitor) throws CloudBeesException {
    StringBuffer errMsg = new StringBuffer();

    try {
      String url = BASE_URL + "user/keys_using_auth";

      HttpClient httpclient = Utils.getAPIClient(url);

      KeysUsingAuthRequest req = new KeysUsingAuthRequest();
      req.email = this.email;
      req.password = this.password;

      HttpPost post = Utils.jsonRequest(url, req);

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      Gson g = Utils.createGson();

      KeysUsingAuthResponse services = g.fromJson(bodyResponse, KeysUsingAuthResponse.class);

      if (services.message != null && services.message.length() > 0) {
        errMsg.append(services.message);
      }
      Utils.checkResponseCode(resp);

      return new AuthInfo(services);

    } catch (Exception e) {
      throw cbthrow(errMsg.toString(), e);
    }

  }

  private CloudBeesException cbthrow(String errMsg, Exception e) {
    String extra = "";
    if (e.getCause() != null) {
      extra = e.getCause().getMessage();
    }
    return new CloudBeesException("Failed to get account services info."
        + (errMsg.length() > 0 ? "\n" + errMsg + "; " + extra : "\n" + extra), e);

  }

  /*
   * private boolean webValidateUser(String email, String password) throws
   * CloudBeesException { String url =
   * "https://sso.cloudbees.com/sso-gateway/signon/usernamePasswordLogin.do";
   *
   * HttpClient httpclient = getAPIClient();
   *
   * HttpPost post = new HttpPost(url);
   *
   * List<NameValuePair> formparams = new ArrayList<NameValuePair>();
   * formparams.add(new BasicNameValuePair("josso_cmd", "login"));
   * formparams.add(new BasicNameValuePair("josso_username", email));
   * formparams.add(new BasicNameValuePair("josso_password", password));
   *
   * try { UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
   * "UTF-8"); post.setEntity(entity);
   *
   * HttpResponse resp = httpclient.execute(post); int code =
   * resp.getStatusLine().getStatusCode(); if (code == 302 || code == 301) {
   * // TODO Temporarily until json API becomes available, validate // user by
   * assuming redirect means successful login return true; }
   *
   * return false; // wrong response code means invalid user or //
   * unrecognized response page
   *
   * } catch (ClientProtocolException e) { throw new
   * CloudBeesException("Failed to validate user", e); } catch (IOException e)
   * { throw new CloudBeesException("Failed to validate user", e); } }
   */

  /*
   * public String remoteGetDomainId() throws CloudBeesException {
   *
   * try { HttpClient httpclient = Utils.getAPIClient();
   *
   * KeysUsingAuthRequest req = new KeysUsingAuthRequest(); req.email = email;
   * req.password = password;
   *
   * Gson g = Utils.createGson(); String json = g.toJson(req);
   *
   * String url = BASE_URL + "api_key"; HttpPost post = new HttpPost(url);
   * post.getParams().setParameter("json", json); HttpResponse resp =
   * httpclient.execute(post);
   *
   * Utils.checkResponseCode(resp); String bodyResponse =
   * Utils.getResponseBody(resp);
   *
   * KeysUsingAuthResponse domainId = g.fromJson(bodyResponse,
   * KeysUsingAuthResponse.class);
   *
   * return domainId.api_key;
   *
   * } catch (Exception e) { throw new
   * CloudBeesException("Failed to get api_key", e); } }
   */public void start() {
    // Do nothing for now.
  }

  public void stop() {
    // Do nothing for now.
  }

  private AccountServiceStatusResponse loadAccountServices(final String account) throws CloudBeesException {
    StringBuffer errMsg = new StringBuffer();

    try {
      String url = BASE_URL + "account/service_status";

      HttpClient httpclient = Utils.getAPIClient(url);

      AccountServiceStatusRequest req = new AccountServiceStatusRequest();
      req.account = account;
      AuthInfo auth = getAuthInfo(null);
      req.uid = auth.getAuth().uid;

      //System.out.println("URL: " + url);
      
      HttpPost post = Utils.jsonRequest(url, req);

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      //System.out.println("Received: " + bodyResponse);
      Gson g = Utils.createGson();

      AccountServiceStatusResponse services = g.fromJson(bodyResponse, AccountServiceStatusResponse.class);

      if (services.message != null && services.message.length() > 0) {
        errMsg.append(services.message);
      }
      Utils.checkResponseCode(resp);

      return services;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get account services info"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
  }

  public List<JenkinsInstance> loadDevAtCloudInstances(final IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return new ArrayList<JenkinsInstance>();
    }

    StringBuffer errMsg = new StringBuffer();

    try {

      String account = getActiveAccountName();
      if (account == null) {
        return new ArrayList<JenkinsInstance>();
      }

      List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
      String url = "https://" + account + ".ci." + HOST;
      JenkinsInstance inst = new JenkinsInstance(account, url, this.email, this.password, true, true);
      instances.add(inst);

      return instances;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get remote DEV@cloud Jenkins instances"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
  }

  /**
   * Returns currently active account. The contract is that if it returns <code>null</code> then it doesn't exist.
   * 
   * @param monitor
   * @return
   */
  public String getActiveAccountName() {
    return activeAccount;

    /*    if (accountSelectionActive) {
          monitor.beginTask("Waiting for the account selection to finish", 1000);

          long wait = 0;      
          //Wait max for 10sec
          while (!accountSelectionActive && wait <= 100) {
            try {
              Thread.currentThread().wait(100);
            } catch (InterruptedException e) {
              monitor.setCanceled(true);
              break;
            }
            wait++;
            monitor.worked(10);
          }

        }

        if (activeAccount != null) {
          return activeAccount;
        }
    */
    //throw new CloudBeesException("Active account not selected!");

  }

  /**
   * Returns all accounts for the user. IMPORTANT! All user functionality should work only with the active/selected user
   * account. Make sure UX does not depend on the whole list of accounts. To get the active account use
   * #getActiveAccounName()
   * 
   * @param monitor
   * @return
   * @throws CloudBeesException
   */
  public String[] getAccounts(final IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return new String[0];
    }

    KeysUsingAuthResponse auth = getAuthInfo(monitor).getAuth();
    StringBuffer errMsg = new StringBuffer();

    try {

      String url = BASE_URL + "account/names";

      HttpClient httpclient = Utils.getAPIClient(url);

      AccountNamesRequest req = new AccountNamesRequest();
      req.uid = auth.uid;

      HttpPost post = Utils.jsonRequest(url, req);

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      Gson g = Utils.createGson();

      AccountNamesResponse services = g.fromJson(bodyResponse, AccountNamesResponse.class);

      if (services.message != null && services.message.length() > 0) {
        errMsg.append(services.message);
      }
      Utils.checkResponseCode(resp);

      Arrays.sort(services.accounts);

      accountsCache = services.accounts;

      return services.accounts;

    } catch (Exception e) {
      throw cbthrow(errMsg.toString(), e);
    }

  }

  @Deprecated
  private String getPrimaryAccount(final IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return "";
    }

    KeysUsingAuthResponse auth = getAuthInfo(monitor).getAuth();
    StringBuffer errMsg = new StringBuffer();

    try {

      String url = BASE_URL + "account/name";

      HttpClient httpclient = Utils.getAPIClient(url);

      AccountNameRequest req = new AccountNameRequest();
      req.uid = auth.uid;

      HttpPost post = Utils.jsonRequest(url, req);

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      Gson g = Utils.createGson();

      AccountNameResponse account = g.fromJson(bodyResponse, AccountNameResponse.class);

      if (account.message != null && account.message.length() > 0) {
        errMsg.append(account.message);
      }
      Utils.checkResponseCode(resp);

      return account.account;

    } catch (Exception e) {
      throw cbthrow(errMsg.toString(), e);
    }

  }

  public static class AuthInfo {

    private final KeysUsingAuthResponse auth;

    public AuthInfo(final KeysUsingAuthResponse services) {
      this.auth = services;
    }

    public KeysUsingAuthResponse getAuth() {
      return this.auth;
    }

  }

  public List<ForgeInstance> getForgeRepos(final IProgressMonitor monitor) throws CloudBeesException {
    List<ForgeInstance> result = new ArrayList<ForgeInstance>();

    String acc = getActiveAccountName();

    if (acc == null) {
      return result;
    }

    AccountServiceStatusResponse services = loadAccountServices(acc);
    Repo[] repos = services.services.forge.repos;
    for (Repo forge : repos) {
      ForgeInstance.TYPE type = null; //ForgeInstance.TYPE.GIT;
      for (ForgeInstance.TYPE t : ForgeInstance.TYPE.values()) {
        if (t.name().equalsIgnoreCase(forge.type)) {
          type = t;
          break;
        }
      }
      if (type != null) {
        result.add(new ForgeInstance(forge.url, services.username, this.password, type));
      }
    }

    return result;
  }

  
  public String getCurrentUsername(final IProgressMonitor monitor) throws CloudBeesException {
    String acc = getActiveAccountName();

    
    
    if (acc == null) {
      return null;
    }

    AccountServiceStatusResponse services = loadAccountServices(acc);
    return services.username;
  }

  
  public String getEmail() {
    return email;
  }

  public void setActiveAccount(String newname) {
    activeAccount = newname;
  }

  public String[] getCachedAccounts() {
    return accountsCache;
  }

  public AuthInfo getCachedAuthInfo(boolean b) throws CloudBeesException {
    return getCachedAuthInfo(b, new NullProgressMonitor());
  }

  /*  public void setAccountSelectionActive(boolean b) {
      accountSelectionActive = b;
    }
  */

}
