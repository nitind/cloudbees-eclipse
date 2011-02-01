package com.cloudbees.eclipse.core;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;
import com.cloudbees.eclipse.util.Utils;
import com.google.gson.Gson;

/**
 * Service to access Nectar instances
 * 
 * @author ahti
 */
public class NectarService {

  private String url;

  public NectarService(String url) {
    this.url = url;
  }

  /**
   * @param viewUrl
   *          full url for the view. <code>null</code> if all jobs from this instance.
   * @return
   * @throws CloudBeesException
   */
  public NectarJobsResponse getJobs(String viewUrl) throws CloudBeesException {

    if (viewUrl != null && !viewUrl.startsWith(url)) {
      throw new CloudBeesException("Unexpected view url provided! Service url: " + url + "; view url: " + viewUrl);
    }

    StringBuffer errMsg = new StringBuffer();

    try {
      HttpClient httpclient = Utils.getAPIClient();

      Gson g = Utils.createGson();

      String reqUrl = viewUrl != null ? viewUrl : url;

      HttpPost post = new HttpPost(reqUrl + "api/json?tree=" + NectarJobsResponse.getTreeQuery());
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      NectarJobsResponse views = g.fromJson(bodyResponse, NectarJobsResponse.class);

      if (views != null) {
        views.serviceUrl = url;
      }

      Utils.checkResponseCode(resp);

      return views;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get account services info"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }


  }

  public NectarInstanceResponse getViews() throws CloudBeesException {

    StringBuffer errMsg = new StringBuffer();

    try {
      HttpClient httpclient = Utils.getAPIClient();

      Gson g = Utils.createGson();


      HttpPost post = new HttpPost(url + "api/json?tree=" + NectarInstanceResponse.getTreeQuery());

      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

      NectarInstanceResponse views = g.fromJson(bodyResponse, NectarInstanceResponse.class);

      if (views != null) {
        views.serviceUrl = url;
      }

      Utils.checkResponseCode(resp);

      return views;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get views"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
  }

  public static void main(String[] args) throws CloudBeesException {
    NectarService s = new NectarService("http://deadlock.netbeans.org/hudson/");

    System.out.println("Query tree: " + NectarInstanceResponse.getTreeQuery());

    NectarInstanceResponse vs = s.getViews();
    NectarInstanceResponse.View[] views = vs.views;
    System.out.println("Primary view: " + vs.primaryView.name + ";" + vs.primaryView.url);
    System.out.println("Received views:");

    for (NectarInstanceResponse.View v : views) {
      System.out.println("view: " + v.name + "; " + v.url);
    }

    System.err.println("Jobs:");
    NectarJobsResponse jb = s.getJobs(null);
    NectarJobsResponse.Job[] jobs= jb.jobs;
    System.out.println("Received builds:");

    for (NectarJobsResponse.Job j : jobs) {
      System.out.println("job: " + j.displayName);
    }

  }
}
