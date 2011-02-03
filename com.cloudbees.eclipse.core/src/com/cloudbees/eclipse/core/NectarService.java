package com.cloudbees.eclipse.core;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

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

    try {
      HttpClient httpclient = Utils.getAPIClient();

      Gson g = Utils.createGson();

      String reqUrl = nectar.url;
      if (!reqUrl.endsWith("/")) {
        reqUrl = reqUrl + "/";
      }

      HttpPost post = new HttpPost(reqUrl + "api/json?tree=" + NectarInstanceResponse.getTreeQuery());

      System.out.println("Nectar: " + nectar);

      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      HttpResponse resp = httpclient.execute(post);
      String bodyResponse = Utils.getResponseBody(resp);

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
      throw new CloudBeesException("Failed to get Nectar views for '" + nectar.url + "'. "
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
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
}
