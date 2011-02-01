package com.cloudbees.eclipse.core;

import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.gc.api.AccountServicesResponse;
import com.cloudbees.eclipse.core.gc.api.KeysUsingAuthRequest;
import com.cloudbees.eclipse.core.gc.api.KeysUsingAuthResponse;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusRequest;
import com.cloudbees.eclipse.core.internal.forge.ForgeSync;
import com.cloudbees.eclipse.core.util.Utils;
import com.google.gson.Gson;

/**
 * Central access to CloudBees Grand Central API
 * 
 * @author ahtik
 */
public class GrandCentralService {

  private static final String HOST = System.getProperty("cloudbees.host", "cloudbees.com");

  //private static final String BASE_URL = "https://grandcentral.cloudbees.com/api/";
  private static final String BASE_URL = "https://grandcentral." + HOST + "/api/";

  private ForgeSyncService forgeSyncService = new ForgeSyncService();

  /**
   * Validates user credential against CloudBees SSO authentication server. FIXME Refactor to json-based validation
   * 
   * @param email
   * @param password
   * @param monitor
   * @return
   * @throws CloudBeesException
   */
  public boolean remoteValidateUser(String email, String password, IProgressMonitor monitor) throws CloudBeesException {

    StringBuffer errMsg = new StringBuffer();

    try {
      HttpClient httpclient = Utils.getAPIClient();

      AccountServiceStatusRequest req = new AccountServiceStatusRequest();
      req.email = email;
      req.password = password;

      String url = BASE_URL + "user/keys_using_auth";

      HttpPost post = Utils.jsonRequest(url, req);

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      Gson g = Utils.createGson();

      AccountServicesResponse services = g.fromJson(bodyResponse, AccountServicesResponse.class);

      if (services.message != null && services.message.length() > 0) {
        errMsg.append(services.message);
      }
      Utils.checkResponseCode(resp);

      return services.account_name.length() > 0;

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
        if (code == 302) {
          // FIXME Temporarily until json API becomes available, validate
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

  public String remoteGetDomainId(String email, String password) throws CloudBeesException {
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


  public void start() {
    // Do nothing for now.
  }

  public void stop() {
    // Do nothing for now.
  }

  private AccountServicesResponse remoteGetAccountServices(String email, String password) {

    //TODO Dummy until real API becomes available
    AccountServicesResponse r = new AccountServicesResponse();
    //r.api_version = "1.0";

    AccountServicesResponse.ForgeService forge1 = new AccountServicesResponse.ForgeService();
    forge1.type = "SVN";
    forge1.url = "http://anonsvn.jboss.org/repos/jbpm/jbpm4/trunk/";

    AccountServicesResponse.ForgeService forge2 = new AccountServicesResponse.ForgeService();
    forge2.type = "GIT";
    forge2.url = "https://github.com/vivek/hudson.git";

    r.forge = new AccountServicesResponse.ForgeService[] { forge1, forge2 };

    r.jaas = new AccountServicesResponse.JaasService[] {};

    return r;
  }

  public void reloadForgeRepos(String email, String password, IProgressMonitor monitor) throws CloudBeesException {
    AccountServicesResponse services = remoteGetAccountServices(email, password);
    for (AccountServicesResponse.ForgeService forgeService : services.forge) {
      ForgeSync.TYPE type = ForgeSync.TYPE.valueOf(forgeService.type);
      if (type == null) {
        throw new CloudBeesException("Unexpected Forge repository type " + forgeService.type + "!");
      }
      Properties props = new Properties();
      props.put("url", forgeService.url);
      forgeSyncService.sync(type, props, monitor);
    }
  }

}
