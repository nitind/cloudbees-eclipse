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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

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
  public static String toB64(final String str) {
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
  public static String fromB64(final String str) {
    try {
      if (str == null || str.length() == 0) {
        return new String(new byte[0], "UTF-8");
      }
      return new String(Base64.decodeBase64(str.getBytes("US-ASCII")));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public final static String readString(final InputStream is) throws CloudBeesException {
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

  public final static DefaultHttpClient getAPIClient() throws CloudBeesException {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    try {
      HttpClientParams.setCookiePolicy(httpclient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);

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

      TrustStrategy trustAllStrategy = new TrustStrategy() {
        public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
          return true;
        }
      };

      SSLSocketFactory socketFactory = new SSLSocketFactory(SSLSocketFactory.TLS, null, null, trustStore, null,
          trustAllStrategy,
          SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
      // Override https handling to use provided truststore
      @SuppressWarnings("deprecation")
      Scheme sch = new Scheme("https", socketFactory, 443);
      httpclient.getConnectionManager().getSchemeRegistry().register(sch);

      HttpParams params = httpclient.getParams();

      //TODO Make configurable from the UI?
      HttpConnectionParams.setConnectionTimeout(params, 10000);
      HttpConnectionParams.setSoTimeout(params, 10000);

      return httpclient;

    } catch (Exception e) {
      throw new CloudBeesException("Error while initiating access to JSON APIs!", e);
    }
  }

  public static HttpPost jsonRequest(final String url, final Object req) throws UnsupportedEncodingException {
    HttpPost post = new HttpPost(url);
    Gson g = Utils.createGson();
    String json = g.toJson(req);
    post.setHeader("Accept", "application/json");
    post.setHeader("Content-type", "application/json");

    //System.out.println("JSON REQUEST STRING " + json);
    StringEntity se = new StringEntity(json);
    post.setEntity(se);
    return post;
  }

  public final static String getResponseBody(final HttpResponse resp) throws CloudBeesException {
    try {
      return Utils.readString(resp.getEntity().getContent());
    } catch (IllegalStateException e) {
      throw new CloudBeesException("Failed to read response", e);
    } catch (IOException e) {
      throw new CloudBeesException("Failed to read response", e);
    }
  }

  public final static void checkResponseCode(final HttpResponse resp) throws CloudBeesException {
    checkResponseCode(resp, false);
  }

  public final static void checkResponseCode(final HttpResponse resp, final boolean expectCIRedirect) throws CloudBeesException {
    int responseStatus = resp.getStatusLine().getStatusCode();

    Header firstHeader = resp.getFirstHeader("Location");

    if (expectCIRedirect && (responseStatus == 302 || responseStatus == 301) && firstHeader != null
        && firstHeader.getValue() != null) {
      //FIXME ugly way to detect a normal redirect within the site that does not redirect to signon but no good idea for better implementation
      if (firstHeader.getValue().indexOf(".ci.") > 0) {
        return;
      }
    }

    if (responseStatus == 302 || responseStatus == 301) {
      throw new CloudBeesException("Authentication required! Either wrong or no credentials were provided! Reason:"
          + resp.getStatusLine().getReasonPhrase());
    }
    if (responseStatus != 200) {
      throw new CloudBeesException("Unexpected response code:" + responseStatus + ". Message:"
          + resp.getStatusLine().getReasonPhrase());
    }
  }

  public static String humanReadableTime(final long duration) {
    String unit = "";
    long mins = duration / (60L * 1000);
    long hr = mins / 60;
    long yrs = hr / 24 / 365;

    if (mins < 60) {
      unit = mins + " min";
      if (mins < 1) {
        unit = unit + "s";
      }
    } else if (mins < 60 * 24) {
      //long newmins = mins - (hr * 60);
      unit = hr + " hr" + (hr > 1 ? "s" : "");/* + " " + newmins + " min" + (newmins > 1 ? "s" : "");*/
    } else if (yrs < 1) {
      long days = hr / 24L;
      unit = days + " day" + (days > 1 ? "s" : "")/* + ", " + hr + " hr" + (hr > 1 ? "s" : "")*/;
    } else {
      unit = yrs + " year" + (yrs > 1 ? "s" : "");
    }

    return unit;
  }

  public static <T> T createInstance(final Class<T> clazz, final Class<?>[] types, final Object[] params) {
    try {
      Constructor<T> cnst;
      cnst = clazz.getConstructor(types);
      return cnst.newInstance(params);
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    } catch (IllegalArgumentException e) {
    } catch (InstantiationException e) {
    } catch (IllegalAccessException e) {
    } catch (InvocationTargetException e) {
    }
    return null;
  }

}
