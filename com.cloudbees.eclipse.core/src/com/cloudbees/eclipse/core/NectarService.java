package com.cloudbees.eclipse.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.cloudbees.eclipse.core.domain.NectarInstance;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.google.gson.Gson;

/**
 * Service to access Nectar instances
 * 
 * @author ahti
 */
public class NectarService {

  /*private String url;
  private String label;*/
  private NectarInstance nectar;

  public NectarService(NectarInstance nectar) {
    this.nectar = nectar;
  }

  /**
   * @param viewUrl
   *          full url for the view. <code>null</code> if all jobs from this instance.
   * @return
   * @throws CloudBeesException
   */
  public NectarJobsResponse getJobs(String viewUrl) throws CloudBeesException {

    if (viewUrl != null && !viewUrl.startsWith(nectar.url)) {
      throw new CloudBeesException("Unexpected view url provided! Service url: " + nectar.url + "; view url: "
          + viewUrl);
    }

    StringBuffer errMsg = new StringBuffer();

    try {
      HttpClient httpclient = Utils.getAPIClient();

      Gson g = Utils.createGson();

      String reqUrl = viewUrl != null ? viewUrl : nectar.url;

      if (!reqUrl.endsWith("/")) {
        reqUrl = reqUrl + "/";
      }

      HttpPost post = new HttpPost(reqUrl + "api/json?tree=" + NectarJobsResponse.getTreeQuery());
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      NectarJobsResponse views = g.fromJson(bodyResponse, NectarJobsResponse.class);

      if (views != null) {
        views.serviceUrl = nectar.url;
      }

      Utils.checkResponseCode(resp);

      return views;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Nectar jobs for '" + nectar.url + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }

  }

  public NectarInstanceResponse getInstance() throws CloudBeesException {
  
    StringBuffer errMsg = new StringBuffer();
  
    System.out.println("Nectar: " + nectar);
    String reqUrl = nectar.url;
    if (!reqUrl.endsWith("/")) {
      reqUrl += "/";
    }
    reqUrl += "api/json?tree=" + NectarInstanceResponse.getTreeQuery();
  
    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();
      Gson g = Utils.createGson();
  
      HttpResponse resp = null;
      String bodyResponse = null;
      boolean tryToLogin = false; // after first 302 let's try to login with our credentials
  
      do {
        if (nectar.jossoCookie != null) {
          reqUrl += "&JOSSO_SESSIONID=" + nectar.jossoCookie;
        }

        HttpPost post = new HttpPost(reqUrl);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
  
        if (nectar.jossoCookie != null) {
          List<NameValuePair> nvps = new ArrayList<NameValuePair>();
          nvps.add(new BasicNameValuePair("JOSSO_SESSIONID", nectar.jossoCookie));
          post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        }
  
        resp = httpclient.execute(post);
        bodyResponse = Utils.getResponseBody(resp);
  
        if (resp.getStatusLine().getStatusCode() == 302 && !tryToLogin) { // redirect to login
          login(httpclient, resp);
          tryToLogin = true;
        } else {
          tryToLogin = false;
        }
  
      } while (tryToLogin);
  
      if (bodyResponse == null) {
        throw new CloudBeesException("Failed to receive response from server");
      }
  
      NectarInstanceResponse instance = g.fromJson(bodyResponse, NectarInstanceResponse.class);
  
      if (instance != null) {
        instance.serviceUrl = nectar.url;
  
        if (instance.views != null) {
          for (int i = 0; i < instance.views.length; i++) {
            instance.views[i].response = instance;
            instance.views[i].isPrimary = instance.primaryView.url.equals(instance.views[i].url);
          }
        }
      }
  
      Utils.checkResponseCode(resp);
  
      return instance;
  
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Nectar views for '" + reqUrl + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
  }

  private void login(DefaultHttpClient httpclient, HttpResponse resp) throws Exception {
    List<Cookie> oldCookies = new ArrayList<Cookie>(httpclient.getCookieStore().getCookies());
  
    String loginUrl = resp.getFirstHeader("Location").getValue();
  
    int pos = loginUrl.indexOf("/sso-gateway/");
    if (pos < 0) {
      return;
    }
    loginUrl = loginUrl.substring(0, pos) + "/sso-gateway/signon/usernamePasswordLogin.do";
  
    HttpPost post = new HttpPost(loginUrl);
    //    post.setHeader("Accept", "application/json");
    //    post.setHeader("Content-type", "application/json");
  
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    nvps.add(new BasicNameValuePair("josso_username", nectar.username));
    nvps.add(new BasicNameValuePair("josso_password", nectar.password));
    nvps.add(new BasicNameValuePair("josso_cmd", "login"));
    nvps.add(new BasicNameValuePair("josso_rememberme", "on"));
    nvps.add(new BasicNameValuePair("submit", "Login"));
    post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
  
    //    HttpParams params = post.getParams();
    //    params.setParameter("josso_cmd", "login");
    //    params.setParameter("josso_username", nectar.username);
    //    params.setParameter("josso_password", nectar.password);
    //    params.setParameter("josso_rememberme", "on");
    //    params.setParameter("submit", "Login");
  
    HttpResponse loginResp = httpclient.execute(post);
    String bodyResponse = Utils.getResponseBody(loginResp);
  
    Header[] heds = loginResp.getAllHeaders();
  
    List<Cookie> newCookies = new ArrayList<Cookie>(httpclient.getCookieStore().getCookies());
    for (Cookie cook : newCookies) {
      if ("JOSSO_SESSIONID_josso".equals(cook.getName())) {
        nectar.jossoCookie = cook.getValue();
      }
    }
  
    System.out.println("Login: " + loginResp.getStatusLine());
  }

  public String getLabel() {
    return nectar.label;
  }

  public String getUrl() {
    return nectar.url;
  }

  @Override
  public String toString() {
    if (nectar != null) {
      return "NectarService[nectarInstance=" + nectar + "]";
    }
    return super.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((nectar == null) ? 0 : nectar.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NectarService other = (NectarService) obj;
    if (nectar == null) {
      if (other.nectar != null)
        return false;
    } else if (!nectar.equals(other.nectar))
      return false;
    return true;
  }

}
