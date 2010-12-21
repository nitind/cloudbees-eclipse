package com.cloudbees.eclipse.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.internal.forge.ForgeSync;
import com.cloudbees.eclipse.core.json.AccountServicesResponse;
import com.cloudbees.eclipse.core.json.DomainIdRequest;
import com.cloudbees.eclipse.core.json.DomainIdResponse;
import com.cloudbees.eclipse.core.json.ForgeService;
import com.cloudbees.eclipse.core.json.HaasService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Central access to CloudBees Grand Central API
 * 
 * @author ahtik
 */
public class GrandCentralService {

  private static final String BASE_URL = "https://grandcentral.cloudbees.com/api/";

  private ForgeSyncService forgeSyncService = new ForgeSyncService();

  /**
   * Validates user credential against CloudBees SSO authentication server.
   * 
   * @param email
   * @param password
   * @param monitor
   * @return
   * @throws CloudBeesException
   */
  public boolean remoteValidateUser(String email, String password, IProgressMonitor monitor) throws CloudBeesException {
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

    // String url = "https://sso.cloudbees.com/sso-gateway/signon/login.do";

  }

  public String remoteGetDomainId(String email, String password) throws CloudBeesException {
    try {
      HttpClient httpclient = getAPIClient();

      DomainIdRequest req = new DomainIdRequest();
      req.email = email;
      req.api_key = password;

      Gson g = createGson();
      String json = g.toJson(req);

      String url = BASE_URL + "domain_id";
      HttpPost post = new HttpPost(url);
      post.getParams().setParameter("json", json);
      HttpResponse resp = httpclient.execute(post);

      checkResponseCode(resp);
      String bodyResponse = getResponseBody(resp);

      DomainIdResponse domainId = g.fromJson(bodyResponse, DomainIdResponse.class);

      return domainId.domain_id;

    } catch (Exception e) {
      throw new CloudBeesException("Failed to get domain_id", e);
    }
  }

  private String getResponseBody(HttpResponse resp) throws CloudBeesException {
    try {
      return readString(resp.getEntity().getContent());
    } catch (IllegalStateException e) {
      throw new CloudBeesException("Failed to read response", e);
    } catch (IOException e) {
      throw new CloudBeesException("Failed to read response", e);
    }
  }

  private void checkResponseCode(HttpResponse resp) throws CloudBeesException {
    int responseStatus = resp.getStatusLine().getStatusCode();
    if (responseStatus != 200) {
      throw new CloudBeesException("Unexpected response code:" + responseStatus);
    }
  }

  private String readString(InputStream is) throws CloudBeesException {
    try {
      Writer writer = new StringWriter();
      char[] buffer = new char[1024];
      try {
        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        int n;
        while ((n = reader.read(buffer)) != -1) {
          writer.write(buffer, 0, n);
        }
      } finally {
        is.close();
      }
      return writer.toString();
    } catch (Exception e) {
      throw new CloudBeesException("Failed to read inputstream", e);
    }
  }

  private HttpClient getAPIClient() throws CloudBeesException {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    try {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

      URL truststore = CloudBeesCorePlugin.getDefault().getBundle().getResource("truststore");
      InputStream instream = truststore.openStream();

      try {
        trustStore.load(instream, "123456".toCharArray());
      } finally {
        instream.close();
      }

      SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
      // Override https handling to use provided truststore
      Scheme sch = new Scheme("https", socketFactory, 443);
      httpclient.getConnectionManager().getSchemeRegistry().register(sch);
      return httpclient;

    } catch (Exception e) {
      throw new CloudBeesException("Error while initiating access to CloudBees GrandCentral API!", e);
    }
  }

  public static Gson createGson() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.serializeSpecialFloatingPointValues();
    gsonBuilder.serializeNulls();
    // gsonBuilder.setPrettyPrinting(); // temporary
    // gsonBuilder.excludeFieldsWithoutExposeAnnotation();
    Gson g = gsonBuilder.create();
    return g;
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
    r.api_version = "1.0";

    ForgeService forge1 = new ForgeService();
    forge1.type = "SVN";
    forge1.url = "http://anonsvn.jboss.org/repos/jbpm/jbpm4/trunk/";

    ForgeService forge2 = new ForgeService();
    forge2.type = "GIT";
    forge2.url = "https://github.com/vivek/hudson.git";

    r.forge = new ForgeService[] { forge1, forge2 };

    r.haas = new HaasService[] {};

    return r;
  }

  public void reloadForgeRepos(String email, String password, IProgressMonitor monitor) throws CloudBeesException {
    AccountServicesResponse services = remoteGetAccountServices(email, password);
    for (ForgeService forgeService : services.forge) {
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
