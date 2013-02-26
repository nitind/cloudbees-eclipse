package com.cloudbees.eclipse.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.gc.api.ClickStartCreateProgress;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;
import com.cloudbees.eclipse.core.gc.api.ClickStartResponseBase;
import com.cloudbees.eclipse.core.gc.api.ClickStartTemplate;
import com.cloudbees.eclipse.core.gc.api.ClickStartTemplateDetailsResponse;
import com.cloudbees.eclipse.core.util.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ClickStartService {

  private static final String CS_API_URL = "https://api-staging.cloudbees.com/v2/clickstart/";
  private String apiKey;
  private String secretKey;

  public ClickStartService() {
  }

  public void setAuth(String apiKey, String secretKey) {
    this.apiKey = apiKey;
    this.secretKey = secretKey;    
  }
  
  public Collection<ClickStartTemplate> loadTemplates(IProgressMonitor monitor) throws CloudBeesException {

    
    StringBuffer errMsg = new StringBuffer();

    try {
      String url = CS_API_URL + "templates";
      HttpClient httpclient = Utils.getAPIClient(url);
      HttpGet get = Utils.jsonGetRequest(url);
      applyAuth(get);
      HttpResponse resp = httpclient.execute(get);
      String bodyResponse = Utils.getResponseBody(resp);
      Gson g = Utils.createGson();
      Utils.checkResponseCode(resp);
      Map<String, ClickStartTemplate> services = g.fromJson(bodyResponse,
          new TypeToken<Map<String, ClickStartTemplate>>() {
          }.getType());

      return services.values();

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get account services info"
          + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }

  }

  /**
   * @param templateId
   * @param account
   * @param name
   * @return
   * @throws CloudBeesException
   */
  public ClickStartCreateResponse create(String template, String account, String name) throws CloudBeesException {
    //* curl -i -X POST
    //"http://localhost:8080/api/apps/json/launch?account=michaelnealeclickstart2&name=nodecli&template=https://raw.github.com/CloudBees-community/nodejs-clickstart/master/clickstart.json"
    StringBuffer errMsg = new StringBuffer();

    try {
      String url = CS_API_URL + "launch?account=" + account + "&name=" + name + "&template=" + template;
      HttpClient httpclient = Utils.getAPIClient(url);
      
      HttpParams params = httpclient.getParams();
      // Override timeouts, this request can be long..
      HttpConnectionParams.setConnectionTimeout(params, 100000);
      HttpConnectionParams.setSoTimeout(params, 100000);

      
      HttpPost get = Utils.jsonRequest(url, "");
      applyAuth(get);
      System.out.println("Request URL: "+url);
      System.out.println("curl --header \"Authorization: Basic NzgxNUI0MUQzRjREOTk2ODpVUDQzM1NURjFOQjlZRElFRytWSzk4RFhNQjBDSExPRko2WFlFQlRIMENBPQ==\" -i -X POST \""+url+"\"");
      HttpResponse resp = httpclient.execute(get);
      String bodyResponse = Utils.getResponseBody(resp);
      System.out.println("JSON RESPONSE for the create command:\n"+bodyResponse);
      Gson g = Utils.createGson();
      ClickStartCreateResponse r = g.fromJson(bodyResponse, ClickStartCreateResponse.class);

      checkForErrors(resp, r);

      return r;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to create from template " + template + " for account " + account
          + " using name " + name + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }

  }

  // curl -i "http://localhost:8080/api/apps/json/template?template=https://raw.github.com/CloudBees-community/lift_template/master/clickstart.json"  
  public ClickStartTemplateDetailsResponse loadTemplateDetails(String templateId) throws CloudBeesException {
    StringBuffer errMsg = new StringBuffer();

    String url = CS_API_URL + "template?template=" + templateId;
    HttpClient httpclient = Utils.getAPIClient(url);
    HttpGet get = Utils.jsonGetRequest(url);
    applyAuth(get);
    HttpResponse resp = null;
    Reader reader = null;

    try {
      resp = httpclient.execute(get);
      reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
    } catch (Exception e) {
      errMsg = new StringBuffer(e.getMessage());
      throw new CloudBeesException("Failed to invoke ClickStart!" + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
    
    try {
      ClickStartResponseBase r = Utils.createGson().fromJson(reader, ClickStartTemplateDetailsResponse.class);
      checkForErrors(resp, r);
      return (ClickStartTemplateDetailsResponse) r;
    } finally {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
          // safe to fail.
        }
    }

  }

  /**
   * @param reservationId
   * @return 0..100 where 100 means 100% as in "template creation finished!". -1 if error occurred!
   * @throws CloudBeesException
   */
  public int getCreateProgress(String reservationId) throws CloudBeesException {
    //http://localhost:8080/api/apps/json/progress?reservation_id=http://nodejspn1.michaelnealeclickstart2.cloudbees.net
    StringBuffer errMsg = new StringBuffer();

    try {
      String url = CS_API_URL + "progress?reservation_id=" + reservationId;
      HttpClient httpclient = Utils.getAPIClient(url);
      HttpGet get = Utils.jsonGetRequest(url);
      applyAuth(get);
      HttpResponse resp = httpclient.execute(get);

      String bodyResponse = Utils.getResponseBody(resp);
      Gson g = Utils.createGson();
      Utils.checkResponseCode(resp);

      ClickStartCreateProgress ret = g.fromJson(bodyResponse, ClickStartCreateProgress.class);
      return Integer.parseInt(ret.progress);

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get progress for the template creation with reservation id: "
          + reservationId + "; " + (errMsg.length() > 0 ? " (" + errMsg + ")" : ""), e);
    }
  }

  private void applyAuth(HttpRequestBase get) {
    get.addHeader("Authorization", "Basic " + Utils.toB64(apiKey + ":" + secretKey));
  }

  private void checkForErrors(HttpResponse resp, ClickStartResponseBase r) throws CloudBeesException {
    if (r != null && r.errorCode != null && r.errorCode.length() > 0) {
      //throw new CloudBeesException("Failed to invoke ClickStart API! " + r.errorCode + ":" + r.message);
      CloudBeesException t = new CloudBeesException(r.message);
      throw t;
    }
    Utils.checkResponseCode(resp);
  }

  public void stop() {
    // TODO Auto-generated method stub
    
  }

}
