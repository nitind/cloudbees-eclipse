package com.cloudbees.eclipse.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.gc.api.AccountNameRequest;
import com.cloudbees.eclipse.core.gc.api.AccountNameResponse;
import com.cloudbees.eclipse.core.gc.api.AccountNamesRequest;
import com.cloudbees.eclipse.core.gc.api.AccountNamesResponse;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusRequest;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse;
import com.cloudbees.eclipse.core.gc.api.KeysUsingAuthRequest;
import com.cloudbees.eclipse.core.gc.api.KeysUsingAuthResponse;
import com.cloudbees.eclipse.core.internal.forge.ForgeSync;
import com.cloudbees.eclipse.core.util.Utils;
import com.google.gson.Gson;

/**
 * Central access to CloudBees Grand Central API
 * 
 * @author ahtik
 */
public class GrandCentralService {

  private static final String PK = "2bf5c815c8334b2";

  private static final String HOST = System.getProperty("cloudbees.host", "cloudbees.com");

  //private static final String BASE_URL = "https://grandcentral.cloudbees.com/api/";
  private static final String BASE_URL = "https://grandcentral." + HOST + "/api/";

  private ForgeSyncService forgeSyncService = new ForgeSyncService();

  private String email;
  private String password;

  public GrandCentralService(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public void setAuthInfo(String email, String password) {
    this.email = email;
    this.password = password;
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
  public boolean validateUser(IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return false;
    }

    AuthInfo auth = getAuthInfo(monitor);

    return auth.getAuth().api_key.length() > 0 && auth.getAuth().secret_key.length() > 0
        && auth.getAuth().uid.length() > 0;

  }

  private boolean hasAuthInfo() {
    return email != null && password != null;
  }

  private AuthInfo getAuthInfo(IProgressMonitor monitor) throws CloudBeesException {
    StringBuffer errMsg = new StringBuffer();

    try {
      HttpClient httpclient = Utils.getAPIClient();

      KeysUsingAuthRequest req = new KeysUsingAuthRequest();
      req.email = email;
      req.password = password;

      String url = BASE_URL + "user/keys_using_auth";

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
      throw new CloudBeesException("Failed to get account services info"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }

  }

  /*  private boolean webValidateUser(String email, String password) throws CloudBeesException {
      String url = "https://sso.cloudbees.com/sso-gateway/signon/usernamePasswordLogin.do";

      HttpClient httpclient = getAPIClient();

      HttpPost post = new HttpPost(url);

      List<NameValuePair> formparams = new ArrayList<NameValuePair>();
      formparams.add(new BasicNameValuePair("josso_cmd", "login"));
      formparams.add(new BasicNameValuePair("josso_username", email));
      formparams.add(new BasicNameValuePair("josso_password", password));

      try {
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(entity);

        HttpResponse resp = httpclient.execute(post);
        int code = resp.getStatusLine().getStatusCode();
        if (code == 302 || code == 301) {
          // TODO Temporarily until json API becomes available, validate
          // user by assuming redirect means successful login
          return true;
        }

        return false; // wrong response code means invalid user or
        // unrecognized response page

      } catch (ClientProtocolException e) {
        throw new CloudBeesException("Failed to validate user", e);
      } catch (IOException e) {
        throw new CloudBeesException("Failed to validate user", e);
      }
    }*/

  /*  public String remoteGetDomainId() throws CloudBeesException {

      try {
        HttpClient httpclient = Utils.getAPIClient();

        KeysUsingAuthRequest req = new KeysUsingAuthRequest();
        req.email = email;
        req.password = password;

        Gson g = Utils.createGson();
        String json = g.toJson(req);

        String url = BASE_URL + "api_key";
        HttpPost post = new HttpPost(url);
        post.getParams().setParameter("json", json);
        HttpResponse resp = httpclient.execute(post);

        Utils.checkResponseCode(resp);
        String bodyResponse = Utils.getResponseBody(resp);

        KeysUsingAuthResponse domainId = g.fromJson(bodyResponse, KeysUsingAuthResponse.class);

        return domainId.api_key;

      } catch (Exception e) {
        throw new CloudBeesException("Failed to get api_key", e);
      }
    }

  */public void start() {
    // Do nothing for now.
  }

  public void stop() {
    // Do nothing for now.
  }

  private AccountServiceStatusResponse loadAccountServices(String account) throws CloudBeesException {
    StringBuffer errMsg = new StringBuffer();

    try {
      HttpClient httpclient = Utils.getAPIClient();

      AccountServiceStatusRequest req = new AccountServiceStatusRequest();
      req.account = account;
      AuthInfo auth = getAuthInfo(null);
      req.uid = auth.getAuth().uid;
      req.partner_key = PK;

      String url = BASE_URL + "account/service_status";

      HttpPost post = Utils.jsonRequest(url, req);

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

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

  /*AccountServiceStatusResponse r = new AccountServiceStatusResponse();
  //r.api_version = "1.0";

      AccountServiceStatusResponse.ForgeService forge1 = new AccountServiceStatusResponse.ForgeService();
      forge1.type = "SVN";
      forge1.url = "http://anonsvn.jboss.org/repos/jbpm/jbpm4/trunk/";

      AccountServiceStatusResponse.ForgeService forge2 = new AccountServiceStatusResponse.ForgeService();
      forge2.type = "GIT";
      forge2.url = "https://github.com/vivek/hudson.git";

      r.forge = new AccountServiceStatusResponse.ForgeService[] { forge1, forge2 };

      r.jaas = new AccountServiceStatusResponse.JaasService[] {};
  
  return r;
  */

  public void reloadForgeRepos(IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return;
    }

    String primaryAccount = getPrimaryAccount(monitor);

    AccountServiceStatusResponse services = loadAccountServices(primaryAccount);

    for (AccountServiceStatusResponse.AccountServices.ForgeService.Repo forge : services.services.forge.repos) {
      ForgeSync.TYPE type = ForgeSync.TYPE.valueOf(forge.type.toUpperCase());
      if (type == null) {
        throw new CloudBeesException("Unexpected Forge repository type " + type + "!");
      }
      Properties props = new Properties();
      props.put("url", forge.url);

      try {
        monitor.beginTask("Syncing repository: " + forge.url, 10);
        forgeSyncService.sync(type, props, monitor);
      } finally {
        monitor.done();
      }

    }
  }

  public List<JenkinsInstance> loadDevAtCloudInstances(IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return new ArrayList<JenkinsInstance>();
    }

    StringBuffer errMsg = new StringBuffer();

    try {

      String[] accounts = getAccounts(monitor);

      List<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();

      for (String account : accounts) {

        String url = "https://" + account + ".ci." + HOST;

        JenkinsInstance inst = new JenkinsInstance(account, url, email, password, true, true);

        instances.add(inst);
      }

      return instances;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get remote DEV@cloud Jenkins instances"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
  }

  public String[] getAccounts(IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return new String[0];
    }

    KeysUsingAuthResponse auth = getAuthInfo(monitor).getAuth();
    StringBuffer errMsg = new StringBuffer();

    try {

      HttpClient httpclient = Utils.getAPIClient();

      AccountNamesRequest req = new AccountNamesRequest();
      req.uid = auth.uid;
      req.partner_key = PK;

      String url = BASE_URL + "account/names";

      HttpPost post = Utils.jsonRequest(url, req);

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      Gson g = Utils.createGson();

      AccountNamesResponse services = g.fromJson(bodyResponse, AccountNamesResponse.class);

      if (services.message != null && services.message.length() > 0) {
        errMsg.append(services.message);
      }
      Utils.checkResponseCode(resp);

      return services.accounts;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get account services info"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }

  }

  public String getPrimaryAccount(IProgressMonitor monitor) throws CloudBeesException {

    if (!hasAuthInfo()) {
      return "";
    }

    KeysUsingAuthResponse auth = getAuthInfo(monitor).getAuth();
    StringBuffer errMsg = new StringBuffer();

    try {

      HttpClient httpclient = Utils.getAPIClient();

      AccountNameRequest req = new AccountNameRequest();
      req.uid = auth.uid;
      req.partner_key = PK;

      String url = BASE_URL + "account/name";

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
      throw new CloudBeesException("Failed to get account services info"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }

  }

  private static class AuthInfo {

    private KeysUsingAuthResponse auth;

    public AuthInfo(KeysUsingAuthResponse services) {
      this.auth = services;
    }

    public KeysUsingAuthResponse getAuth() {
      return auth;
    }

  }
}
