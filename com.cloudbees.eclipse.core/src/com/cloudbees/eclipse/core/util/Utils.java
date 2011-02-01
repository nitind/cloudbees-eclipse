package com.cloudbees.eclipse.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.security.KeyStore;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {

  public static Gson createGson() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.serializeSpecialFloatingPointValues();
    gsonBuilder.serializeNulls();
    // gsonBuilder.setPrettyPrinting(); // temporary
    // gsonBuilder.excludeFieldsWithoutExposeAnnotation();
    Gson g = gsonBuilder.create();
    return g;
  }

  /**
   * Converts string to US-ASCII base64 string.
   * 
   * @param str
   * @return
   */
  public static String toB64(String str) {
    try {
      if (str == null || str.length() == 0) {
        return new String(new byte[0], "US-ASCII");
      }
      return new String(Base64.encodeBase64(str.getBytes("UTF-8")), "US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts string from base64 string.
   * 
   * @param str
   * @return
   */
  public static String fromB64(String str) {
    try {
      if (str == null || str.length() == 0) {
        return new String(new byte[0], "UTF-8");
      }
      return new String(Base64.decodeBase64(str.getBytes("US-ASCII")));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public final static String readString(InputStream is) throws CloudBeesException {
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

  public final static HttpClient getAPIClient() throws CloudBeesException {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    try {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

      CloudBeesCorePlugin plugin = CloudBeesCorePlugin.getDefault();

      URL truststore;

      if (plugin == null) {
        //Outside the OSGI environment, try to open the stream from the current dir.
        truststore = new File("truststore").toURI().toURL();
      } else {
        truststore = plugin.getBundle().getResource("truststore");
      }

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
      throw new CloudBeesException("Error while initiating access to JSON APIs!", e);
    }
  }


  public static HttpPost jsonRequest(String url, Object req) throws UnsupportedEncodingException {
    HttpPost post = new HttpPost(url);
    Gson g = Utils.createGson();
    String json = g.toJson(req);
    post.setHeader("Accept", "application/json");
    post.setHeader("Content-type", "application/json");
    StringEntity se = new StringEntity(json);
    post.setEntity(se);
    return post;
  }

  public final static String getResponseBody(HttpResponse resp) throws CloudBeesException {
    try {
      return Utils.readString(resp.getEntity().getContent());
    } catch (IllegalStateException e) {
      throw new CloudBeesException("Failed to read response", e);
    } catch (IOException e) {
      throw new CloudBeesException("Failed to read response", e);
    }
  }

  public final static void checkResponseCode(HttpResponse resp) throws CloudBeesException {
    int responseStatus = resp.getStatusLine().getStatusCode();
    if (responseStatus != 200) {
      throw new CloudBeesException("Unexpected response code:" + responseStatus);
    }
  }

}
