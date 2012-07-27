package com.cloudbees.eclipse.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsConfigParser;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsConsoleLogResponse;
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

  enum ResponseType {
    STRING, STREAM, HTTP
  }

  /*private String url;
  private String label;*/
  private final JenkinsInstance jenkins;

  private final Map<String, JenkinsScmConfig> scms = new HashMap<String, JenkinsScmConfig>();

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
    assertCorrectUrl(viewUrl);

    try {
      monitor.beginTask("Fetching Job list for '" + this.jenkins.label + "'...", 10);

      Gson g = Utils.createGson();

      String reqUrl = viewUrl; // != null ? viewUrl : jenkins.url;

      if (!reqUrl.endsWith("/")) {
        reqUrl += "/";
      }

      String uri = reqUrl + "api/json";

      HttpPost post = new HttpPost(uri);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("tree", JenkinsJobsResponse.QTREE));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      monitor.worked(1);

      DefaultHttpClient httpclient = Utils.getAPIClient(uri);

      String bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 5));

      JenkinsJobsResponse views = null;
      try {
        views = g.fromJson(bodyResponse, JenkinsJobsResponse.class);
      } catch (Exception e) {
        String body = bodyResponse;
        if (body != null && body.length() > 1000) {
          body = body.substring(0, 1000);
        }
        throw new CloudBeesException("Illegal JSON response for '" + uri + "': " + body, e);
      }

      if (views != null) {
        views.viewUrl = viewUrl;
      }

      if (views.jobs == null && views.primaryView.jobs != null) {
        views.jobs = views.primaryView.jobs;
      }

      monitor.worked(4);

      return views;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins jobs for '" + this.jenkins.url + "'.", e);
    } finally {
      monitor.done();
    }

  }

  private void assertCorrectUrl(String viewUrl) throws CloudBeesException {
    if (viewUrl != null && !viewUrl.startsWith(this.jenkins.url)) {
      if (viewUrl != null && this.jenkins.alternativeUrl != null && !viewUrl.startsWith(this.jenkins.alternativeUrl)) {
        throw new CloudBeesException("Unexpected url provided! Service url: " + this.jenkins.url + "; view url: "
            + viewUrl);
      }
    }
  }

  public JenkinsInstanceResponse getInstance(final IProgressMonitor monitor) throws CloudBeesException {
    StringBuffer errMsg = new StringBuffer();

    String reqUrl = this.jenkins.url;
    if (!reqUrl.endsWith("/")) {
      reqUrl += "/";
    }
    reqUrl += "api/json";

    try {
      Gson g = Utils.createGson();

      HttpPost post = new HttpPost(reqUrl);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("tree", JenkinsInstanceResponse.QTREE));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      DefaultHttpClient httpclient = Utils.getAPIClient(reqUrl);

      String bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

      if (bodyResponse == null) {
        throw new CloudBeesException("Failed to receive response from server");
      }

      JenkinsInstanceResponse response = g.fromJson(bodyResponse, JenkinsInstanceResponse.class);

      if (response != null) {
        response.viewUrl = this.jenkins.url;
        response.atCloud = this.jenkins.atCloud;

        jenkins.alternativeUrl = response.primaryView.url; // Initializing jenkins instance alternativeUrl for lookups. Not perfectly nice solution in terms of reverse logic but looks like most feasible atm.

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
    return (String) retrieveWithLogin(httpclient, post, params, expectRedirect, monitor, ResponseType.STRING);
  }

  synchronized private Object retrieveWithLogin(final DefaultHttpClient httpclient, final HttpRequestBase post,
      final List<NameValuePair> params, final boolean expectRedirect, final SubProgressMonitor monitor,
      final ResponseType responseType) throws UnsupportedEncodingException, IOException, ClientProtocolException,
      CloudBeesException, Exception {
    Object bodyResponse = null;

    if (this.jenkins.username != null && this.jenkins.username.trim().length() > 0 && this.jenkins.password != null
        && this.jenkins.password.trim().length() > 0) {
      post.addHeader("Authorization", "Basic " + Utils.toB64(this.jenkins.username + ":" + this.jenkins.password));
    }

    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    if (params != null) {
      nvps.addAll(params);
    }

    if (post instanceof HttpEntityEnclosingRequest) {
      if (((HttpEntityEnclosingRequest) post).getEntity() == null) {
        ((HttpEntityEnclosingRequest) post).setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
      }
    }

    CloudBeesCorePlugin.getDefault().getLogger().info("Jenkins request: " + post.getURI());

    if (post instanceof HttpPost) {
      HttpPost pp = (HttpPost) post;

      String s;
      try {        
        s = new Scanner(pp.getEntity().getContent()).useDelimiter("\\A").next();
      } catch (java.util.NoSuchElementException e) {
        s = "";
      }
      CloudBeesCorePlugin.getDefault().getLogger().info("Jenkins request post params: " + s);
      
    }

    HttpResponse resp = httpclient.execute(post);
    switch (responseType) {
    case STRING:
      bodyResponse = Utils.getResponseBody(resp);
      break;
    case STREAM:
      bodyResponse = resp.getEntity().getContent();
      break;
    case HTTP:
      bodyResponse = resp;
      break;
    }

    Utils.checkResponseCode(resp, expectRedirect, jenkins.atCloud);

    return bodyResponse;
  }

  public String getLabel() {
    return this.jenkins.label;
  }

  public String getUrl() {
    return this.jenkins.url;
  }

  /**
   * Returns url that was assigned by the JSON request by jenkins
   * 
   * @return
   */
  public String getAlternativeUrl() {
    return this.jenkins.alternativeUrl;
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

    assertCorrectUrl(jobUrl);

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "api/json";

    try {

      Gson g = Utils.createGson();

      HttpPost post = new HttpPost(reqStr);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("tree", JenkinsBuildDetailsResponse.QTREE));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      DefaultHttpClient httpclient = Utils.getAPIClient(reqStr);

      String bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

      JenkinsBuildDetailsResponse details = g.fromJson(bodyResponse, JenkinsBuildDetailsResponse.class);

      if (details.getDisplayName() == null) {
        throw new CloudBeesException("Response does not contain required fields!");
      }

      if (details.viewUrl == null) {
        details.viewUrl = jobUrl;
      }

      return details;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins jobs for '" + jobUrl + "'. "
          + (errMsg.length() > 0 && errMsg.length() < 1000 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr, e);
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

    assertCorrectUrl(jobUrl);

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "api/json";

    try {

      Gson g = Utils.createGson();

      HttpPost post = new HttpPost(reqStr);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("tree", JenkinsJobAndBuildsResponse.QTREE));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      DefaultHttpClient httpclient = Utils.getAPIClient(reqStr);
      String bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

      JenkinsJobAndBuildsResponse details = g.fromJson(bodyResponse, JenkinsJobAndBuildsResponse.class);

      if (details.name == null) {
        throw new CloudBeesException("Response does not contain required fields!");
      }

      details.viewUrl = jobUrl; // this.jenkins.url;

      return details;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins jobs for '" + jobUrl + "'. "
          + (errMsg.length() > 0 && errMsg.length() < 1000 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr, e);
    }
  }

  private JenkinsScmConfig getJobScmConfig(final String jobUrl, final IProgressMonitor monitor)
      throws CloudBeesException {
    monitor.setTaskName("Fetching Job SCM config...");

    assertCorrectUrl(jobUrl);

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = jobUrl;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "config.xml";
    String bodyResponse = null;
    try {
      DefaultHttpClient httpclient = Utils.getAPIClient(reqStr);

      HttpGet post = new HttpGet(reqStr);
      post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml");

      bodyResponse = retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(monitor, 10));

      JenkinsScmConfig config = JenkinsConfigParser.parse(bodyResponse);

      return config;
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins SCM config from '" + reqStr + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "")
          + (bodyResponse == null ? "" : " - Response: " + bodyResponse), e);
    }
  }

  public void invokeBuild(final String jobUrl, final Map<String, String> props, final IProgressMonitor monitor)
      throws CloudBeesException {
    monitor.setTaskName("Invoking build request...");

    assertCorrectUrl(jobUrl);

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
      DefaultHttpClient httpclient = Utils.getAPIClient(reqStr);

      HttpPost post = new HttpPost(reqStr);

      retrieveWithLogin(httpclient, post, params, true, new SubProgressMonitor(monitor, 10));
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get invoke JenkinsBuild for '" + jobUrl + "'. "
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

  public InputStream getTestReport(final String url, final IProgressMonitor monitor) throws CloudBeesException {
    monitor.setTaskName("Fetching Jenkins build Test Report...");

    assertCorrectUrl(url);

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = url;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "testReport/api/xml";

    InputStream bodyResponse = null;
    try {
      DefaultHttpClient httpclient = Utils.getAPIClient(reqStr);

      HttpGet post = new HttpGet(reqStr);
      post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml");

      bodyResponse = (InputStream) retrieveWithLogin(httpclient, post, null, false,
          new SubProgressMonitor(monitor, 10), ResponseType.STREAM);

      return bodyResponse;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins job test report for '" + url + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + "Request string: " + reqStr + " - Response: "
          + Utils.readString(bodyResponse), e);
    }
  }

  public void createJenkinsJob(final String jobName, final String configXML, final IProgressMonitor monitor)
      throws CloudBeesException {
    try {
      monitor.setTaskName("Preparing new job request");

      String encodedJobName = URLEncoder.encode(jobName, "UTF-8");
      String url = this.jenkins.url.endsWith("/") ? this.jenkins.url : this.jenkins.url + "/";

      String reqUrl = url + "createItem?name=" + encodedJobName;

      HttpPost post = new HttpPost(reqUrl);
      StringEntity strEntity = new StringEntity(configXML, "application/xml", "UTF-8");
      post.setEntity(strEntity);

      DefaultHttpClient httpClient = Utils.getAPIClient(reqUrl);

      monitor.setTaskName("Creating new Jenkins job...");

      retrieveWithLogin(httpClient, post, null, false, new SubProgressMonitor(monitor, 10));

    } catch (Exception e) {
      throw new CloudBeesException("Failed to create new Jenkins job", e);
    }
  }

  public void deleteJenkinsJob(final String joburl, final IProgressMonitor monitor) throws CloudBeesException {
    try {
      monitor.setTaskName("Preparing delete request");

      String url = joburl.endsWith("/") ? joburl : joburl + "/";

      String reqUrl = url + "doDelete";

      HttpPost post = new HttpPost(reqUrl);

      post.setEntity(new StringEntity(""));

      DefaultHttpClient httpClient = Utils.getAPIClient(reqUrl);

      monitor.setTaskName("Deleting Jenkins job...");

      retrieveWithLogin(httpClient, post, null, true, new SubProgressMonitor(monitor, 10));

    } catch (Exception e) {
      throw new CloudBeesException("Failed to delete Jenkins job", e);
    }
  }

  public JenkinsConsoleLogResponse getBuildLog(final JenkinsConsoleLogResponse request, final IProgressMonitor monitor)
      throws CloudBeesException {
    monitor.setTaskName("Fetching Job build log...");

    String url = request.viewUrl;
    assertCorrectUrl(url);

    StringBuffer errMsg = new StringBuffer();

    String reqUrl = url;

    if (!reqUrl.endsWith("/")) {
      reqUrl = reqUrl + "/";
    }

    String reqStr = reqUrl + "logText/progressiveText?start=" + request.start;

    try {
      DefaultHttpClient httpclient = Utils.getAPIClient(reqStr);

      HttpPost post = new HttpPost(reqStr);
      if (request.annotator != null) {
        post.setHeader("X-ConsoleAnnotator", request.annotator);
      }

      HttpResponse response = (HttpResponse) retrieveWithLogin(httpclient, post, null, false, new SubProgressMonitor(
          monitor, 10), ResponseType.HTTP);

      //      for (Header head : response.getAllHeaders()) {
      //        System.out.println("header: " + head);
      //      }

      request.logPart = response.getEntity().getContent();
      try {
        Header moreData = response.getLastHeader("X-More-Data");
        request.hasMore = moreData != null ? "true".equalsIgnoreCase(moreData.getValue()) : false;
        Header textSize = response.getLastHeader("X-Text-Size");
        request.start = textSize != null ? Long.parseLong(textSize.getValue()) : Long.MAX_VALUE;
        Header annotator = response.getLastHeader("X-ConsoleAnnotator");
        if (annotator != null) {
          request.annotator = annotator.getValue();
        }
      } catch (Exception e) {
        e.printStackTrace();
        request.hasMore = false;
      }

      return request;
    } catch (Exception e) {
      throw new CloudBeesException("Failed to get Jenkins build log for '" + url + "'. "
          + (errMsg.length() > 0 && errMsg.length() < 1000 ? " (" + errMsg + ")" : "") + "Request string:" + reqStr, e);
    }
  }

  public InputStream getArtifact(final String url, final IProgressMonitor monitor) throws CloudBeesException {
    monitor.setTaskName("Fetching Jenkins artifact...");

    assertCorrectUrl(url);

    StringBuffer errMsg = new StringBuffer();

    InputStream bodyResponse = null;
    try {
      DefaultHttpClient httpclient = Utils.getAPIClient(url);

      HttpGet post = new HttpGet(url);

      bodyResponse = (InputStream) retrieveWithLogin(httpclient, post, null, false,
          new SubProgressMonitor(monitor, 10), ResponseType.STREAM);

      return bodyResponse;

    } catch (Exception e) {
      String readString = Utils.readString(bodyResponse);
      if (readString != null && readString.length() > 100) {
        readString = readString.substring(0, 100);
      }
      throw new CloudBeesException("Failed to get Jenkins artifact '" + url + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : "") + " - Response: " + readString, e);
    }
  }

}
