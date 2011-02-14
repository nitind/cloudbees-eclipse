package com.cloudbees.eclipse.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.google.gson.Gson;

/**
 * Service to access Jenkins instances
 * 
 * @author ahti
 */
public class JenkinsService {

  /*private String url;
  private String label;*/
  private JenkinsInstance jenkins;

  private static String lastJossoSessionId = null;

  public JenkinsService(JenkinsInstance jenkins) {
    this.jenkins = jenkins;
  }

  /**
   * @param viewUrl
   *          full url for the view. <code>null</code> if all jobs from this instance.
   * @return
   * @throws CloudBeesException
   */
  public JenkinsJobsResponse getJobs(String viewUrl, IProgressMonitor monitor) throws CloudBeesException {
    if (viewUrl != null && !viewUrl.startsWith(jenkins.url)) {
      throw new CloudBeesException("Unexpected view url provided! Service url: " + jenkins.url + "; view url: "
          + viewUrl);
    }

    try {
      monitor.beginTask("Fetching Job list for '" + jenkins.label + "'...", 10);

      DefaultHttpClient httpclient = Utils.getAPIClient();

      Gson g = Utils.createGson();

      String reqUrl = viewUrl; // != null ? viewUrl : jenkins.url;

      if (!reqUrl.endsWith("/")) {
        reqUrl += "/";
      }

      String uri = reqUrl + "api/json?tree=" + JenkinsJobsResponse.QTREE;
      HttpPost post = new HttpPost(uri);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");
      monitor.worked(1);

      String bodyResponse = retrieveWithLogin(httpclient, post, new SubProgressMonitor(monitor, 5), false);

      JenkinsJobsResponse views = null;
      try {
        views = g.fromJson(bodyResponse, JenkinsJobsResponse.class);
      } catch (Exception e) {
        throw new CloudBeesException("Illegal JSON response for '" + uri + "'", e);
      }

      if (views != null) {
        views.viewUrl = viewUrl;
      }

      monitor.worked(4);

      return views;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins jobs for '" + jenkins.url + "'.", e);
    } finally {
      monitor.done();
    }

  }

  public JenkinsInstanceResponse getInstance(IProgressMonitor monitor) throws CloudBeesException {
    StringBuffer errMsg = new StringBuffer();
  
    String reqUrl = jenkins.url;
    if (!reqUrl.endsWith("/")) {
      reqUrl += "/";
    }
    reqUrl += "api/json?tree=" + JenkinsInstanceResponse.QTREE;

    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();
      Gson g = Utils.createGson();

      HttpPost post = new HttpPost(reqUrl);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      String bodyResponse = retrieveWithLogin(httpclient, post, new SubProgressMonitor(monitor, 10), false);

      if (bodyResponse == null) {
        throw new CloudBeesException("Failed to receive response from server");
      }

      JenkinsInstanceResponse response = g.fromJson(bodyResponse, JenkinsInstanceResponse.class);

      if (response != null) {
        response.viewUrl = jenkins.url;
        response.atCloud = jenkins.atCloud;

        if (response.views != null) {
          for (int i = 0; i < response.views.length; i++) {
            response.views[i].response = response;
            response.views[i].isPrimary = response.primaryView.url.equals(response.views[i].url);
          }
        }
      }

      return response;

    } catch (OperationCanceledException e) {
      throw e;
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins views for '" + reqUrl + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
  }

  private String retrieveWithLogin(DefaultHttpClient httpclient, HttpPost post, SubProgressMonitor monitor,
      boolean expectRedirect)
      throws UnsupportedEncodingException,
      IOException, ClientProtocolException, CloudBeesException, Exception {
    String bodyResponse = null;

    //        if (lastJossoCookie != null) {
    //          reqUrl += "&JOSSO_SESSIONID=" + lastJossoCookie;
    //        }

    boolean tryToLogin = true; // false for BasicAuth, true for redirect login
    do {

      if ((jenkins.atCloud || jenkins.authenticate) && jenkins.username != null && jenkins.username.trim().length() > 0
          && jenkins.password != null && jenkins.password.trim().length() > 0) {
        //        post.addHeader("Authorization", "Basic " + Utils.toB64(jenkins.username + ":" + jenkins.password));
      }

      if (jenkins.atCloud && lastJossoSessionId != null) { // basic auth failed
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("JOSSO_SESSIONID", lastJossoSessionId));
        post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8)); // 1

        post.addHeader("Cookie", "JOSSO_SESSIONID=" + lastJossoSessionId); // 2

        httpclient.getCookieStore().addCookie(new BasicClientCookie("JOSSO_SESSIONID", lastJossoSessionId)); // 3
      }

      HttpResponse resp = httpclient.execute(post);
      bodyResponse = Utils.getResponseBody(resp);

      if (jenkins.atCloud && jenkins.username != null && jenkins.username.trim().length() > 0
          && jenkins.password != null && jenkins.password.trim().length() > 0
          && (resp.getStatusLine().getStatusCode() == 302 || resp.getStatusLine().getStatusCode() == 301) && tryToLogin) { // redirect to login
        login(httpclient, post.getURI().toASCIIString(), resp.getFirstHeader("Location").getValue(),
            new SubProgressMonitor(monitor, 5));
        //httpclient.getCookieStore().clear();
        tryToLogin = false;
      } else {
        // check final outcome if we got what we asked for
        Utils.checkResponseCode(resp, expectRedirect);
        break;
      }

    } while (!tryToLogin);

    return bodyResponse;
  }

  private void login(DefaultHttpClient httpClient, final String referer, final String redirect,
      final IProgressMonitor monitor) throws Exception {
    try {
      String name = "Logging in to '" + jenkins.label + "'...";
      monitor.beginTask(name, 5);
      monitor.setTaskName(name);

      //    httpClient.getCookieStore().clear();
      //    List<Cookie> oldCookies = new ArrayList<Cookie>(httpClient.getCookieStore().getCookies());

      String nextUrl = redirect;
      for (int i = 0; i < 20 && nextUrl != null; i++) {
        if (monitor.isCanceled()) {
          throw new OperationCanceledException();
        }
        HttpResponse lastResp = visitSite(httpClient, nextUrl, referer);

        Header redir = lastResp.getFirstHeader("Location");
        String redirUrl = redir == null ? null : redir.getValue();

        // page with login form
        if (redirUrl != null && nextUrl.lastIndexOf("josso_login") >= 0 && redirUrl.indexOf("login.do") >= 0) {
          redirUrl = redirUrl.substring(0, redirUrl.indexOf("login.do")) + "usernamePasswordLogin.do";
        }

        // shortcut to josso_login
        if (redirUrl != null && nextUrl.lastIndexOf("josso_security_check") >= 0 && nextUrl.indexOf("login.do") < 0) {
          redirUrl = nextUrl.substring(0, nextUrl.indexOf("josso_security_check")) + "josso_login/";
        }

        nextUrl = redirUrl;

        Header cookie = lastResp.getFirstHeader("Set-Cookie");
        if (cookie == null) {
          cookie = lastResp.getFirstHeader("SET-COOKIE");
        }
        if (cookie == null) {
          cookie = lastResp.getFirstHeader("Set-Cookie2");
        }
        if (cookie == null) {
          cookie = lastResp.getFirstHeader("SET-COOKIE2");
        }

        monitor.worked(1);

        if (cookie != null) {
          CloudBeesCorePlugin.getDefault().getLogger().info("Cookie: " + cookie);
        }
        if (cookie != null && cookie.getValue().startsWith("JOSSO_SESSIONID=")) {
          break; // logged in ok
        }

      }

      for (Cookie cook : httpClient.getCookieStore().getCookies()) {
        if ("JOSSO_SESSIONID".equals(cook.getName())) {
          lastJossoSessionId = cook.getValue();
          //        break login; // ready
        }
      }

    } finally {
      monitor.done();
    }

    //CloudBeesCorePlugin.getDefault().getLogger().info("JOSSO_SESSIONID=" + lastJossoSessionId);
  }

  private HttpResponse visitSite(DefaultHttpClient httpClient, String url, String refererUrl)
      throws IOException, ClientProtocolException, CloudBeesException {

    CloudBeesCorePlugin.getDefault().getLogger().info("Visiting: " + url);

    HttpPost post = new HttpPost(url);
    post.addHeader("Referer", refererUrl);

    if (url.endsWith("usernamePasswordLogin.do")) {
      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      nvps.add(new BasicNameValuePair("josso_username", jenkins.username));
      nvps.add(new BasicNameValuePair("josso_password", jenkins.password));
      nvps.add(new BasicNameValuePair("josso_cmd", "login"));
      nvps.add(new BasicNameValuePair("josso_rememberme", "on"));
      post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    }

    HttpResponse resp = httpClient.execute(post);
    String body = Utils.getResponseBody(resp);

    Header redir = resp.getFirstHeader("Location");

    CloudBeesCorePlugin.getDefault().getLogger()
        .info("\t" + resp.getStatusLine() + (redir != null ? (" -> " + redir.getValue()) : ""));

    return resp;
  }

  public String getLabel() {
    return jenkins.label;
  }

  public String getUrl() {
    return jenkins.url;
  }

  @Override
  public String toString() {
    if (jenkins != null) {
      return "JenkinsService[jenkinsInstance=" + jenkins + "]";
    }
    return super.toString();
  }

  public JenkinsBuildDetailsResponse getJobDetails(String jobUrl, final IProgressMonitor monitor)
      throws CloudBeesException {
    monitor.setTaskName("Fetching Job details...");

    if (jobUrl != null && !jobUrl.startsWith(jenkins.url)) {
      throw new CloudBeesException("Unexpected view url provided! Service url: " + jenkins.url + "; job url: " + jobUrl);
    }

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "api/json?tree=" + JenkinsBuildDetailsResponse.QTREE;

    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();

      Gson g = Utils.createGson();


      HttpPost post = new HttpPost(reqStr);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      String bodyResponse = retrieveWithLogin(httpclient, post, new SubProgressMonitor(monitor, 10), false);

      JenkinsBuildDetailsResponse details = g.fromJson(bodyResponse, JenkinsBuildDetailsResponse.class);

      if (details.fullDisplayName == null) {
        throw new CloudBeesException("Response does not contain required fields!");
      }

      return details;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins jobs for '" + jobUrl + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr, e);
    }

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((jenkins == null) ? 0 : jenkins.hashCode());
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
    JenkinsService other = (JenkinsService) obj;
    if (jenkins == null) {
      if (other.jenkins != null)
        return false;
    } else if (!jenkins.equals(other.jenkins))
      return false;
    return true;
  }

  public JenkinsJobBuildsResponse getJobBuilds(String jobUrl, final IProgressMonitor monitor) throws CloudBeesException {
    monitor.setTaskName("Fetching Job builds...");

    if (jobUrl != null && !jobUrl.startsWith(jenkins.url)) {
      throw new CloudBeesException("Unexpected job url provided! Service url: " + jenkins.url + "; job url: " + jobUrl);
    }

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "api/json?tree=" + JenkinsJobBuildsResponse.QTREE;

    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();

      Gson g = Utils.createGson();

      HttpPost post = new HttpPost(reqStr);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      String bodyResponse = retrieveWithLogin(httpclient, post, new SubProgressMonitor(monitor, 10), false);

      JenkinsJobBuildsResponse details = g.fromJson(bodyResponse, JenkinsJobBuildsResponse.class);

      if (details.name == null) {
        throw new CloudBeesException("Response does not contain required fields!");
      }

      details.viewUrl = jenkins.url;
      
      return details;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins jobs for '" + jobUrl + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr, e);
    }

  }

  public void invokeBuild(String jobUrl, IProgressMonitor monitor) throws CloudBeesException {
    monitor.setTaskName("Invoking build request...");

    if (jobUrl != null && !jobUrl.startsWith(jenkins.url)) {
      throw new CloudBeesException("Unexpected job url provided! Service url: " + jenkins.url + "; job url: " + jobUrl);
    }

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "build";

    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();

      HttpPost post = new HttpPost(reqStr);

      retrieveWithLogin(httpclient, post, new SubProgressMonitor(monitor, 10), true);
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get invoke Build for '" + jobUrl + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr, e);
    }
  }

}
