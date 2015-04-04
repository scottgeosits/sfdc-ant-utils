package com.antutilities.salesforce.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjgeosits on 4/3/15.
 */
public class CreateFoldersBuildfile extends SalesforceService {

    private String buildFilename;

    static Log log = LogFactory.getLog(CreateFoldersBuildfile.class);

    public CreateFoldersBuildfile() {
        super();
    }

    public void execute() {
        try {
            generateBuildFile();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setBuildFilename(String filename) { this.buildFilename = filename; }

    public List<String> getMetadataFolders(String metadataType) throws JSONException {

        String query = "SELECT+Id,DeveloperName+FROM+Folder+WHERE+Type='" + metadataType + "'+AND+DeveloperName!=''";
        if (login()) {
            String responseBody = restGet(salesforceRestUri + "/query/?q=" + query);
            JSONObject foldersJson = new JSONObject(new JSONTokener(responseBody));
            JSONArray foldersJsonArray = foldersJson.getJSONArray("records");

            List<String> list = new ArrayList<String>();

            if (foldersJsonArray!=null && foldersJsonArray.length()>0) {
                for (int j=0; j<foldersJsonArray.length(); j++) {
                    JSONObject folderJson = foldersJsonArray.getJSONObject(j);
                    String Id = folderJson.getString("Id");
                    list.add(folderJson.getString("DeveloperName"));
                }
            }
            return list;
        }
        else {
            log.error("ERROR: could not log into salesforce.");
            return null;
        }
    }

    public List<String> getReportFolders() throws JSONException {
        return getMetadataFolders("Report");
    }
    public List<String> getDocumentFolders() throws JSONException {
        return getMetadataFolders("Document");
    }
    public List<String> getDashboardFolders() throws JSONException {
        return getMetadataFolders("Dashboard");
    }
    public List<String> getEmailTemplateFolders() throws JSONException {
        return getMetadataFolders("Email");
    }

    public String buildBulkRetrieve(String metadataType, List<String> list) {

        StringBuffer sb = new StringBuffer();
        for (String value : list) {
            sb.append("\t\t");
            sb.append("<sf:bulkRetrieve username=\"${sf.username}\" ");
            sb.append("password=\"${sf.password}\" serverurl=\"${sf.server}\" ");
            sb.append("metadataType=\"").append(metadataType).append("\" ");
            sb.append("containingFolder=\"").append(value).append("\" ");
            sb.append("retrieveTarget=\"${basedir}/metadata/${orgname}/src\"/>");
            sb.append("\r\n");
        }

        return sb.toString();
    }

    public void writeBuildFile(String s1, String s2, String s3, String s4) throws IOException {

        FileWriter fw = new FileWriter(this.buildFilename);

        fw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        fw.append("<project name=\"Bulk Retrieve\" default=\"\" basedir=\".\" xmlns:sf=\"antlib:com.salesforce\">\r\n");
        fw.append("\t<property environment=\"env\"/>\r\n");
        fw.append("\r\n");
        fw.append("\t<!-- Define required jars-->\r\n");
        fw.append("\t<path id=\"taskDependencies\">\r\n");
        fw.append("\t\t<pathelement location=\"./lib/ant.jar\"/>\r\n");
        fw.append("\t\t<pathelement location=\"./lib/commons-io-2.4.jar\"/>\r\n");
        fw.append("\t\t<pathelement location=\"./lib/SfApexDoc.jar\"/>\r\n");
        fw.append("\t</path>\r\n");
        fw.append("\r\n");
        fw.append("\t<taskdef uri=\"antlib:com.salesforce\" resource=\"com/salesforce/antlib.xml\" classpath=\"./lib/ant-salesforce.jar\"/>\r\n");
        fw.append("\r\n");
        fw.append("\t<target name=\"bulkRetrieveFolders\">\r\n");
        fw.append(s1);
        fw.append(s2);
        fw.append(s3);
        fw.append(s4);
        fw.append("\t</target>\r\n");
        fw.append("</project>\r\n");
        fw.close();
    }

    public void generateBuildFile() throws Exception {

        List<String> reportList = getReportFolders();
        List<String> documentList = getDocumentFolders();
        List<String> dashboardList = getDashboardFolders();
        List<String> emailList = getEmailTemplateFolders();

        String s1 = buildBulkRetrieve("Report", reportList);
        String s2 = buildBulkRetrieve("Document", documentList);
        String s3 = buildBulkRetrieve("Dashboard", dashboardList);
        String s4 = buildBulkRetrieve("EmailTemplate", emailList);

        writeBuildFile(s1, s2, s3, s4);
    }


    public static void main(String[] args) throws Exception {
        Environment.setupEnvironment(Environment.SCOTTDEV);

        CreateFoldersBuildfile gf = new CreateFoldersBuildfile();

        gf.setSfClientId((String) System.getenv("sf.clientId"));
        gf.setSfClientSecret((String) System.getenv("sf.clientSecret"));
        gf.setSfServer((String) System.getenv("sf.server"));
        gf.setSfUsername((String) System.getenv("sf.username"));
        gf.setSfPassword((String) System.getenv("sf.password"));
        gf.setApiVersion((String) System.getenv("apiVersion"));
        gf.setSfOauthEndpoint((String) System.getenv("sf.oauthEndpoint"));
        gf.setSfRestEndpoint((String) System.getenv("sf.restEndpoint"));
        gf.setBuildFilename((String) System.getenv("buildFilename"));

        gf.generateBuildFile();
    }
}
