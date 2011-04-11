package com.cloudbees.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsConfigParser;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;
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
  private final JenkinsInstance jenkins;

  private final Map<String, JenkinsScmConfig> scms = new HashMap<String, JenkinsScmConfig>();

  private static String lastJossoSessionId = null;

  public JenkinsService(final JenkinsInstance jenkins) {
    this.jenkins = jenkins;
  }

  /**
   * @param viewUrl
   *          full url for the view. <code>null</code> if all jobs from this instance.
   * @return
   * @throws CloudBeesException
   */
  public JenkinsJobsResponse getJobs(final String viewUrl, final IProgressMonitor monitor) throws CloudBeesException {
    if (viewUrl != null && !viewUrl.startsWith(this.jenkins.url)) {
      throw new CloudBeesException("Unexpected view url provided! Service url: " + this.jenkins.url + "; view url: "
          + viewUrl);
    }

    try {
      monitor.beginTask("Fetching Job list for '" + this.jenkins.label + "'...", 10);

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

      String bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 5));

      JenkinsJobsResponse views = null;
      try {
        views = g.fromJson(bodyResponse, JenkinsJobsResponse.class);
      } catch (Exception e) {
        throw new CloudBeesException("Illegal JSON response for '" + uri + "': " + bodyResponse, e);
      }

      if (views != null) {
        views.viewUrl = viewUrl;
      }

      monitor.worked(4);

      return views;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins jobs for '" + this.jenkins.url + "'.", e);
    } finally {
      monitor.done();
    }

  }

  public JenkinsInstanceResponse getInstance(final IProgressMonitor monitor) throws CloudBeesException {
    StringBuffer errMsg = new StringBuffer();

    String reqUrl = this.jenkins.url;
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

      String bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

      if (bodyResponse == null) {
        throw new CloudBeesException("Failed to receive response from server");
      }

      JenkinsInstanceResponse response = g.fromJson(bodyResponse, JenkinsInstanceResponse.class);

      if (response != null) {
        response.viewUrl = this.jenkins.url;
        response.atCloud = this.jenkins.atCloud;

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

  synchronized private String retrieveWithLogin(final DefaultHttpClient httpclient, final HttpRequestBase post,
      final List<NameValuePair> params, final boolean expectRedirect, final SubProgressMonitor monitor)
  throws UnsupportedEncodingException, IOException, ClientProtocolException, CloudBeesException, Exception {
    String bodyResponse = null;

    boolean tryToLogin = true; // false for BasicAuth, true for redirect login
    do {

      if ((this.jenkins.atCloud || this.jenkins.authenticate) && this.jenkins.username != null
          && this.jenkins.username.trim().length() > 0 && this.jenkins.password != null
          && this.jenkins.password.trim().length() > 0) {
        //post.addHeader("Authorization", "Basic " + Utils.toB64(this.jenkins.username + ":" + this.jenkins.password));
      }

      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      if (params != null) {
        nvps.addAll(params);
      }

      if (this.jenkins.atCloud && lastJossoSessionId != null) { // basic auth failed
        nvps.add(new BasicNameValuePair("JOSSO_SESSIONID", lastJossoSessionId)); // 1
        post.addHeader("Cookie", "JOSSO_SESSIONID=" + lastJossoSessionId); // 2
        httpclient.getCookieStore().addCookie(new BasicClientCookie("JOSSO_SESSIONID", lastJossoSessionId)); // 3
      }
      if (post instanceof HttpEntityEnclosingRequest) {
        if (((HttpEntityEnclosingRequest) post).getEntity() == null) {
          ((HttpEntityEnclosingRequest) post).setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        }
      }

      CloudBeesCorePlugin.getDefault().getLogger().info("Retrieve: " + post.getURI());

      HttpResponse resp = httpclient.execute(post);
      bodyResponse = Utils.getResponseBody(resp);

      if (this.jenkins.atCloud && this.jenkins.username != null && this.jenkins.username.trim().length() > 0
          && this.jenkins.password != null && this.jenkins.password.trim().length() > 0
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

  private void login(final DefaultHttpClient httpClient, final String referer, final String redirect,
      final IProgressMonitor monitor) throws Exception {
    try {
      String name = "Logging in to '" + this.jenkins.label + "'...";
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

  private HttpResponse visitSite(final DefaultHttpClient httpClient, final String url, final String refererUrl)
  throws IOException, ClientProtocolException, CloudBeesException {

    CloudBeesCorePlugin.getDefault().getLogger().info("Visiting: " + url);

    HttpPost post = new HttpPost(url);
    post.addHeader("Referer", refererUrl);

    if (url.endsWith("usernamePasswordLogin.do")) {
      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      nvps.add(new BasicNameValuePair("josso_username", this.jenkins.username));
      nvps.add(new BasicNameValuePair("josso_password", this.jenkins.password));
      nvps.add(new BasicNameValuePair("josso_cmd", "login"));
      nvps.add(new BasicNameValuePair("josso_rememberme", "on"));
      post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    }

    HttpResponse resp = httpClient.execute(post);

    Utils.getResponseBody(resp);

    Header redir = resp.getFirstHeader("Location");

    CloudBeesCorePlugin.getDefault().getLogger()
    .info("\t" + resp.getStatusLine() + (redir != null ? " -> " + redir.getValue() : ""));

    return resp;
  }

  public String getLabel() {
    return this.jenkins.label;
  }

  public String getUrl() {
    return this.jenkins.url;
  }

  @Override
  public String toString() {
    if (this.jenkins != null) {
      return "JenkinsService[jenkinsInstance=" + this.jenkins + "]";
    }
    return super.toString();
  }

  public JenkinsBuildDetailsResponse getJobDetails(final String jobUrl, final IProgressMonitor monitor)
  throws CloudBeesException {
    monitor.setTaskName("Fetching Job details...");

    if (jobUrl != null && !jobUrl.startsWith(this.jenkins.url)) {
      throw new CloudBeesException("Unexpected view url provided! Service url: " + this.jenkins.url + "; job url: "
          + jobUrl);
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

      String bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

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
    result = prime * result + (this.jenkins == null ? 0 : this.jenkins.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    JenkinsService other = (JenkinsService) obj;
    if (this.jenkins == null) {
      if (other.jenkins != null) {
        return false;
      }
    } else if (!this.jenkins.equals(other.jenkins)) {
      return false;
    }
    return true;
  }

  public JenkinsJobAndBuildsResponse getJobBuilds(final String jobUrl, final IProgressMonitor monitor)
  throws CloudBeesException {
    monitor.setTaskName("Fetching Job builds...");

    if (jobUrl != null && !jobUrl.startsWith(this.jenkins.url)) {
      throw new CloudBeesException("Unexpected job url provided! Service url: " + this.jenkins.url + "; job url: "
          + jobUrl);
    }

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "api/json?tree=" + JenkinsJobAndBuildsResponse.QTREE;

    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();

      Gson g = Utils.createGson();

      HttpPost post = new HttpPost(reqStr);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      String bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

      JenkinsJobAndBuildsResponse details = g.fromJson(bodyResponse, JenkinsJobAndBuildsResponse.class);

      if (details.name == null) {
        throw new CloudBeesException("Response does not contain required fields!");
      }

      details.viewUrl = this.jenkins.url;

      return details;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins jobs for '" + jobUrl + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr, e);
    }
  }

  private JenkinsScmConfig getJobScmConfig(final String jobUrl, final IProgressMonitor monitor)
  throws CloudBeesException {
    monitor.setTaskName("Fetching Job SCM config...");

    if (jobUrl != null && !jobUrl.startsWith(this.jenkins.url)) {
      throw new CloudBeesException("Unexpected job url provided! Service url: " + this.jenkins.url + "; job url: "
          + jobUrl);
    }

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "config.xml";
    String bodyResponse = null;
    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();

      HttpGet post = new HttpGet(reqStr);
      post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml");

      bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

      JenkinsScmConfig config = JenkinsConfigParser.parse(bodyResponse);

      return config;
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins SCM config for '" + jobUrl + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr + " - Response: "
          + bodyResponse, e);
    }
  }

  public void invokeBuild(final String jobUrl, final Map<String, String> props, final IProgressMonitor monitor)
  throws CloudBeesException {
    monitor.setTaskName("Invoking build request...");

    if (jobUrl != null && !jobUrl.startsWith(this.jenkins.url)) {
      throw new CloudBeesException("Unexpected job url provided! Service url: " + this.jenkins.url + "; job url: "
          + jobUrl);
    }

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr;
    if (props == null || props.isEmpty()) {
      reqStr = reqUrl + "build";
    } else {
      reqStr = reqUrl + "buildWithParameters";
    }

    List<NameValuePair> params = null;
    if (props != null) {
      for (Map.Entry<String, String> entry : props.entrySet()) {
        if (params == null) {
          params = new ArrayList<NameValuePair>();
        }
        params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
      }
    }

    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();

      HttpPost post = new HttpPost(reqStr);

      retrieveWithLogin(httpclient, post, params, true, new SubProgressMonitor(monitor, 10));
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get invoke Build for '" + jobUrl + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr, e);
    }
  }

  public JenkinsScmConfig getJenkinsScmConfig(final String jobUrl, final IProgressMonitor monitor)
  throws CloudBeesException {
    // TODO invalidate old items
    JenkinsScmConfig scm = this.scms.get(jobUrl);
    if (scm == null) {
      scm = getJobScmConfig(jobUrl, monitor);
      if (scm != null) {
        this.scms.put(jobUrl, scm);
      }
    }
    return scm;
  }

  public String getTestReport(final String url, final IProgressMonitor monitor) throws CloudBeesException {
    monitor.setTaskName("Fetching Jenkins build Test Report...");

    if (url != null && !url.startsWith(this.jenkins.url)) {
      throw new CloudBeesException("Unexpected job url provided! Service url: " + this.jenkins.url + "; job url: "
          + url);
    }

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = url;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "testReport/api/xml";

    String bodyResponse = null;
    try {
      DefaultHttpClient httpclient = Utils.getAPIClient();

      HttpGet post = new HttpGet(reqStr);
      post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml");

      bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

      return bodyResponse;
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins job test report for '" + url + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr + " - Response: "
          + bodyResponse, e);
    }
  }

  public void createJenkinsJob(final String jobName, final File configXML, final IProgressMonitor monitor)
  throws CloudBeesException {
    try {
      monitor.setTaskName("Preparing new job request");

      String encodedJobName = URLEncoder.encode(jobName, "UTF-8");
      String url = this.jenkins.url.endsWith("/") ? this.jenkins.url : this.jenkins.url + "/";

      HttpPost post = new HttpPost(url + "createItem?name=" + encodedJobName);
      FileEntity fileEntity = new FileEntity(configXML, "application/xml");
      post.setEntity(fileEntity);

      DefaultHttpClient httpClient = Utils.getAPIClient();
      monitor.setTaskName("Creating new Jenkins job...");

      retrieveWithLogin(httpClient, post, null, false, new SubProgressMonitor(monitor, 10));

    } catch (Exception e) {
      throw new CloudBeesException("Failed to create new Jenkins job", e);
    }
  }

  public void deleteJenkinsJob(final String joburl, final IProgressMonitor monitor)
  throws CloudBeesException {
    try {
      monitor.setTaskName("Preparing delete request");


      String url = joburl.endsWith("/") ? joburl : joburl + "/";

      HttpPost post = new HttpPost(url + "doDelete");

      post.setEntity(new StringEntity(""));

      DefaultHttpClient httpClient = Utils.getAPIClient();
      monitor.setTaskName("Deleting Jenkins job...");

      retrieveWithLogin(httpClient, post, null, false, new SubProgressMonitor(monitor, 10));

    } catch (Exception e) {
      throw new CloudBeesException("Failed to delete Jenkins job", e);
    }
  }

}
