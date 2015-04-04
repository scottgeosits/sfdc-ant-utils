package com.antutilities.salesforce.metadata;

import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Base service for connecting to Salesforce REST API.
 *
 * User: scott.geosits
 * Date: 9/12/12
 * Time: 11:08 AM
 */
public class SalesforceService extends RestService {

    static Log log = LogFactory.getLog(SalesforceService.class);

    protected static DateFormat dateFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    private static String TIME_ZONE = "America/Chicago";

    protected UserCredentials userCredentials;
    protected String oAuthEndpoint;
    private OAuth2Response oauth2Response;
    protected String restEndpoint;

    protected String salesforceRestUri;

    public SalesforceService() {
        userCredentials = new UserCredentials();
    }

    public void setSfClientId(String clientId) { this.userCredentials.consumerKey = clientId; }
    public void setSfClientSecret(String clientSecret) { this.userCredentials.consumerSecret = clientSecret; }
    public void setSfUsername(String username) { this.userCredentials.userName = username; }
    public void setSfPassword(String password) { this.userCredentials.password = password; }
    public void setSfServer(String server) { this.userCredentials.loginInstanceDomain = server; }
    public void setApiVersion(String apiVersion) { this.userCredentials.apiVersion = apiVersion; }
    public void setSfOauthEndpoint(String oauthEndpoint) { this.oAuthEndpoint = oauthEndpoint; }
    public void setSfRestEndpoint(String restEndpoint) { this.restEndpoint = restEndpoint; }

    private boolean forceNewLogin() {

        this.userCredentials.grantType = "password";

        String tzString = System.getenv("TIME_ZONE");
        if (tzString != null) {
            TIME_ZONE = tzString;
        }
        TimeZone tz = TimeZone.getTimeZone(TIME_ZONE);
        dateFormatter.setTimeZone(tz);

        HttpResponse response;
        String loginHostUri = userCredentials.loginInstanceDomain + oAuthEndpoint;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(loginHostUri);

            StringBuilder requestBodyText = new StringBuilder("grant_type=password");
            requestBodyText.append("&username=");
            requestBodyText.append(userCredentials.userName);
            requestBodyText.append("&password=");
            requestBodyText.append(userCredentials.password);
            requestBodyText.append("&client_id=");
            requestBodyText.append(userCredentials.consumerKey);
            requestBodyText.append("&client_secret=");
            requestBodyText.append(userCredentials.consumerSecret);

            StringEntity requestBody = new StringEntity(requestBodyText.toString());

            requestBody.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(requestBody);
            httpPost.addHeader(prettyPrintHeader);

            response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 200) {
                InputStreamReader inputStream = new InputStreamReader(response.getEntity().getContent());

                OAuth2Response oauth2Response = gson.fromJson(inputStream, OAuth2Response.class);

                salesforceRestUri = oauth2Response.instance_url + restEndpoint + "/v" + this.userCredentials.apiVersion;

                log.info("Salesforce login SUCCESS - logged in to instance: " + salesforceRestUri);
                oauthHeader = new BasicHeader("Authorization", "OAuth " + oauth2Response.access_token);
            }
            else {
                log.error("Login to Salesforce.com failed.  Response code = " + response.getStatusLine().getStatusCode());
                log.error("Response: " + response.toString());
                return false;
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        return true;
    }

    /**
     * Login to Salesforce using OAuth 2.0
     */
    public boolean login() {

        if (oauthHeader == null) {
            return forceNewLogin();
        }
        return true;
    }

    public boolean checkSalesforceSessionStatus() {

        synchronized (oauthHeader) {
            boolean status = false;
            if (oauthHeader != null) {
                try {
                    String sql = "SELECT Id FROM User";
                    String responseBody = restGet(salesforceRestUri + "/query/?q=" + urlEncode(sql));
                    JSONObject response = response = new JSONObject(new JSONTokener(responseBody));
                    try {
                        String ec = response.getString("errorCode");
                        log.error("Salesforce session status error code:  " + ec);
                    } catch (JSONException ex) {
                        // No errorCode found.
                        status = true;
                    }
                } catch (Exception ex) {
                    // Will return false by default.
                    log.debug("Caught exception.");
                }
            }
            return status;
        }
    }

    private void invalidateSession() {
        oauthHeader = null;
    }

    protected boolean isInvalidSession(String response) {
        if ( StringUtils.contains(response, "INVALID_SESSION_ID")) {
            invalidateSession();
            return true;
        }
        else {
            return false;
        }
    }

    protected String getInstanceUrl() {
        return oauth2Response.instance_url;
    }

    protected String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Could not encode string: '" + str + "'");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
            return null;
        }
    }

    static class OAuth2Response {
        public OAuth2Response() {
        }

        String id;
        String issued_at;
        String instance_url;
        String signature;
        String access_token;
    }

    protected class UserCredentials {
        String grantType;
        String userName;
        String password;
        String consumerKey;
        String apiVersion;
        String consumerSecret;
        String loginInstanceDomain;
    }

    public String getAttachmentXML(String id) {

        if (login()) {
            StringBuffer soql = new StringBuffer();
            soql.append("SELECT Body FROM Attachment ");
            soql.append("WHERE Id = ");
            soql.append("'").append(id).append("'");

            String responseBody = restGet(salesforceRestUri + "/query/?q=" + urlEncode(soql.toString()));
            try {
                JSONObject jsonItems = new JSONObject(new JSONTokener(responseBody));
                JSONArray sizeList;
                sizeList = jsonItems.getJSONArray("records");
                if (sizeList  != null && sizeList.length() > 0) {
                    for (int i=0; i < sizeList.length(); i++) {
                        JSONObject item = sizeList.getJSONObject(i);
                        String body = item.getString("Body");
                        String xml = restGet(getInstanceUrl() + body);
                        return xml;
                    }
                }
            } catch (JSONException e) {
                log.error("JSON Exception creating Salesforce object list from JSON string.");
                log.error("Bad JSON string:  " + responseBody);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                log.error(sw.toString());
            }
        }
        return "";
    }

    protected static List<String> getItemIds(String json) throws JSONException {
        JSONObject obj = new JSONObject(new JSONTokener(json));
        List<String> list = new ArrayList<String>();
        JSONArray itemList;
        itemList = obj.getJSONArray("records");
        if (itemList != null && itemList.length() > 0) {
            for (int i = 0; i < itemList.length(); i++) {
                JSONObject item = itemList.getJSONObject(i);
                list.add(item.getString("Id"));
            }
        }
        return list;
    }

    public boolean deleteObject(String objectName, String id) throws JSONException {
        if (login()) {
            String uri = salesforceRestUri + "/sobjects/" + objectName + "/" + id;

            try {
                return isSuccessfulStatus(restDeleteForResponse(uri));
            } catch (IOException e) {
                log.error("Error while attempting to DELETE "+uri, e);
            }
        } else {
            log.error("ERROR logging in to DATABASE.COM!!");
        }

        return false;
    }
}
