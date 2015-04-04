package com.antutilities.salesforce.metadata;

import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

/**
 * Created by sjgeosits on 4/3/15.
 */
public class RestService {

    static Log log = LogFactory.getLog(RestService.class);

    static Header oauthHeader = null;
    static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");

    protected Gson gson = new Gson();

    private SSLSocketFactory getDummySSLSocketFactory(){
        try {
            return new SSLSocketFactory(new TrustStrategy() {

                public boolean isTrusted(
                        final X509Certificate[] chain, String authType) throws CertificateException {
                    // Accept all certs
                    return true;
                }

            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String restGet(String uri) {
        String result = null;
        printBanner("GET", uri);
        try {
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, getDummySSLSocketFactory()));
            HttpGet httpGet = new HttpGet(uri);
            if (oauthHeader != null) {
                httpGet.addHeader(oauthHeader);
            }
            if (prettyPrintHeader != null) {
                httpGet.addHeader(prettyPrintHeader);
            }
            httpGet.addHeader("Accept-Encoding", "utf-8");
            HttpResponse response = httpClient.execute(httpGet);
            result = getBody(response.getEntity().getContent());
        } catch (IOException ioe) {
            log.error("Could not send GET request:");
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        } catch (NullPointerException npe) {
            log.error("Could not send GET request:");
            StringWriter sw = new StringWriter();
            npe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        return result;
    }

    public <T> T restGet(String url, Class<T> targetClazz) {
        return gson.fromJson(restGet(url), targetClazz);
    }

    public String restPut(String uri) {
        String result = null;
        printBanner("PUT", uri);
        try {
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, getDummySSLSocketFactory()));
            HttpPut httpPut = new HttpPut(uri);
            if (oauthHeader != null) {
                httpPut.addHeader(oauthHeader);
            }
            if (prettyPrintHeader != null) {
                httpPut.addHeader(prettyPrintHeader);
            }
            HttpResponse response = httpClient.execute(httpPut);
            result = getBody(response.getEntity().getContent());
        } catch (IOException ioe) {
            log.error("Could not send PUT request:");
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        } catch (NullPointerException npe) {
            log.error("Could not send PUT request:");
            StringWriter sw = new StringWriter();
            npe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        return result;
    }

    public String restDelete(String uri) {
        String result = null;
        try {
            HttpResponse response = restDeleteForResponse(uri);
            result = getBody(response.getEntity().getContent());
        } catch (IOException ioe) {
            log.error("Could not send DELETE request:");
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        } catch (NullPointerException npe) {
            log.error("Could not send DELETE request:");
            StringWriter sw = new StringWriter();
            npe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        return result;
    }

    public static boolean isSuccessfulStatus(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return 200 <= status && status < 300;
    }

    public HttpResponse restDeleteForResponse(String uri) throws IOException {
        printBanner("DELETE", uri);
        HttpClient httpClient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(uri);
        if (oauthHeader != null) {
            httpDelete.addHeader(oauthHeader);
        }
        if (prettyPrintHeader != null) {
            httpDelete.addHeader(prettyPrintHeader);
        }
        return httpClient.execute(httpDelete);
    }

    public HttpResponse restPatchForResponse(String uri, String requestBody) throws IOException {
        printBanner("PATCH", uri);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPatch httpPatch = new HttpPatch(uri);
        if (oauthHeader != null) {
            httpPatch.addHeader(oauthHeader);
        }
        if (prettyPrintHeader != null) {
            httpPatch.addHeader(prettyPrintHeader);
        }
        StringEntity body = new StringEntity(requestBody);
        body.setContentType("application/json");
        httpPatch.setEntity(body);
        return httpClient.execute(httpPatch);
    }

    public HttpResponse restPostForResponse(String uri, String requestBody) throws IOException {
        printBanner("POST", uri);
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, getDummySSLSocketFactory()));
        HttpPost httpPost = new HttpPost(uri);
        if (oauthHeader != null) {
            httpPost.addHeader(oauthHeader);
        }
        if (prettyPrintHeader != null) {
            httpPost.addHeader(prettyPrintHeader);
        }
        StringEntity body = new StringEntity(requestBody);
        body.setContentType("application/json");
        httpPost.setEntity(body);
        return httpClient.execute(httpPost);
    }

    public String restPatch(String uri, String requestBody) {
        String result = null;
        try {
            HttpResponse response = restPatchForResponse(uri, requestBody);
            result = response.getEntity() != null ?
                    getBody(response.getEntity().getContent()) : "";
        } catch (IOException ioe) {
            log.error("Could not send PATCH request:");
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        } catch (NullPointerException npe) {
            log.error("Could not send PATCH request:");
            StringWriter sw = new StringWriter();
            npe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        return result;
    }

    public String restPost(String uri, String requestBody) {
        String result = null;
        try {
            HttpResponse response = restPostForResponse(uri, requestBody);
            result = getBody(response.getEntity().getContent());
        } catch (IOException ioe) {
            log.error("Could not send POST request:");
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        } catch (NullPointerException npe) {
            log.error("Could not send POST request:");
            StringWriter sw = new StringWriter();
            npe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        return result;
    }

    /**
     * Extend the Apache HttpPost method to implement an HttpPost
     * method.
     */
    public static class HttpPatch extends HttpPost {
        public HttpPatch(String uri) {
            super(uri);
        }

        public String getMethod() {
            return "PATCH";
        }
    }

    // private methods
    protected void printBanner(String method, String uri) {
        log.debug("HTTP Method: " + method + ".  REST URI: " + uri);
    }

    public static String bodyOf(HttpResponse response) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntity().getContent(), writer);
        return writer.toString();
    }

    public static String getBody(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        try {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream)
            );
            String temp;
            try{
                while ((temp = in.readLine()) != null) {
                    result.append(temp).append("\n");
                }
            } catch(java.net.SocketException e){
                log.error(e);
            }

            in.close();
        } catch (IOException ioe) {
            log.error("Could not retrieve body content from InputStream.  Reason: " + ioe.getMessage());
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        return result.toString();
    }

    /**
     * Convert a http header into a string for logging
     */
    public static String getHeader(Header[] headers) {
        StringBuilder builder = new StringBuilder(30);
        for (Header head : headers) {
            builder.append("Header:: ").append(System.getProperty("line.separator"));
            for (HeaderElement element : head.getElements()) {
                builder.append(element.getName()).append(": ");
                builder.append(element.getValue()).append(System.getProperty("line.separator"));
            }
            builder.append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }

}
